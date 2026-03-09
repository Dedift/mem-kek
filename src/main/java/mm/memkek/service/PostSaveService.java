package mm.memkek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.entity.ImagePost;
import mm.memkek.dao.entity.Post;
import mm.memkek.dao.entity.TextPost;
import mm.memkek.dao.enums.ContentType;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dao.enums.PostStatus;
import mm.memkek.repository.ChannelRepository;
import mm.memkek.repository.PostRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostSaveService {

    private final PostRepository postRepository;
    private final ChannelRepository channelRepository;
    private final R2dbcEntityTemplate entityTemplate;
    private final MinioService minioService;
    private final LlmCategoryService llmCategoryService;

    public Mono<Post> saveTextPost(String channelId, Long messageId, Integer date, String text) {
        return channelRepository.findByTelegramChannelId(channelId)
                .flatMap(channel -> postRepository.existsByChannelIdAndTelegramMessageId(channel.getId(), messageId)
                        .flatMap(exists -> {
                            if (exists) {
                                log.info("Post {} already exists in channel {}", messageId, channelId);
                                return Mono.empty();
                            }

                            Post post = new Post();
                            assert channel.getId() != null;
                            post.setChannelId(channel.getId());
                            post.setTelegramMessageId(messageId);
                            post.setTelegramDate(convertUnixToLocalDateTime(date));
                            post.setContentType(ContentType.TEXT);
                            post.setStatus(PostStatus.REVIEW);

                            // Параллельно классифицируем и сохраняем пост
                            return Mono.zip(
                                            postRepository.save(post),
                                            llmCategoryService.classifyText(text)
                                                    .subscribeOn(Schedulers.boundedElastic()), // Выносим LLM вызов в отдельный поток
                                            (savedPost, category) -> {
                                                savedPost.setCategory(category);
                                                return savedPost;
                                            }
                                    )
                                    .flatMap(savedPost ->
                                            postRepository.save(savedPost)
                                                    .flatMap(updatedPost -> {
                                                        TextPost textPost = new TextPost();
                                                        textPost.setPostId(updatedPost.getId());
                                                        textPost.setContentText(text);
                                                        return entityTemplate.insert(TextPost.class)
                                                                .using(textPost)
                                                                .thenReturn(updatedPost);
                                                    })
                                    )
                                    .doOnNext(saved -> log.info("Saved text post: {} with category {}", saved.getId(), saved.getCategory()));
                        }));
    }

    public Mono<Post> saveMediaPost(String channelId, Long messageId, Integer date,
                                    String telegramFileUniqueId, byte[] mediaData,
                                    String fileName, String caption) {
        return channelRepository.findByTelegramChannelId(channelId)
                .flatMap(channel -> postRepository.existsByChannelIdAndTelegramMessageId(channel.getId(), messageId)
                        .flatMap(exists -> {
                            if (exists) {
                                log.info("Post {} already exists in channel {}", messageId, channelId);
                                return Mono.empty();
                            }

                            ContentType contentType = determineContentType(fileName);
                            if (contentType != ContentType.IMAGE) {
                                log.info("Skipping non-image media for message {}: {}", messageId, fileName);
                                return Mono.empty();
                            }

                            // Сохраняем пост сразу, а категорию проставляем асинхронно
                            Post post = new Post();
                            assert channel.getId() != null;
                            post.setChannelId(channel.getId());
                            post.setTelegramMessageId(messageId);
                            post.setTelegramDate(convertUnixToLocalDateTime(date));
                            post.setContentType(contentType);
                            post.setStatus(PostStatus.REVIEW);
                            post.setCategory(MemeCategory.OTHER); // Временная категория

                            return postRepository.save(post)
                                    .flatMap(savedPost ->
                                            Mono.zip(
                                                            // Сохраняем файл в MinIO
                                                            minioService.uploadFile(mediaData, fileName, getMimeType(fileName))
                                                                    .subscribeOn(Schedulers.boundedElastic()),
                                                            // Сохраняем метаданные изображения
                                                            Mono.just(savedPost),
                                                            (objectName, postForImage) -> {
                                                                ImagePost imagePost = new ImagePost();
                                                                imagePost.setPostId(postForImage.getId());
                                                                imagePost.setMediaObjectName(objectName);
                                                                imagePost.setTelegramFileUniqueId(telegramFileUniqueId);
                                                                imagePost.setCaption(caption);
                                                                return imagePost;
                                                            }
                                                    )
                                                    .flatMap(imagePost ->
                                                            entityTemplate.insert(ImagePost.class).using(imagePost)
                                                                    .thenReturn(savedPost)
                                                    )
                                                    .flatMap(_ ->
                                                            // Асинхронно классифицируем и обновляем категорию
                                                            llmCategoryService.classifyImage(caption, mediaData, channel.getChannelName())
                                                                    .subscribeOn(Schedulers.boundedElastic())
                                                                    .flatMap(category -> {
                                                                        savedPost.setCategory(category);
                                                                        return postRepository.save(savedPost);
                                                                    })
                                                                    .doOnNext(updated ->
                                                                            log.info("Updated category for post {}: {}", updated.getId(), updated.getCategory()))
                                                                    .onErrorResume(error -> {
                                                                        log.error("Failed to classify image for post {}, keeping default", savedPost.getId(), error);
                                                                        return Mono.just(savedPost);
                                                                    })
                                                    )
                                    )
                                    .doOnNext(saved -> log.info("Saved media post: {}, file: {}", saved.getId(), fileName));
                        }));
    }

    private LocalDateTime convertUnixToLocalDateTime(Integer unixTime) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(unixTime),
                ZoneId.systemDefault());
    }

    private ContentType determineContentType(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg", "png", "webp", "gif" -> ContentType.IMAGE;
            default -> ContentType.OTHER;
        };
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private String getMimeType(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}