package mm.memkek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.entity.ImagePost;
import mm.memkek.dao.entity.Post;
import mm.memkek.dao.entity.TextPost;
import mm.memkek.dao.enums.ContentType;
import mm.memkek.dao.enums.PostStatus;
import mm.memkek.repository.ChannelRepository;
import mm.memkek.repository.PostRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    public Mono<Post> saveTextPost(String channelId, Long messageId, Integer date, String text) {
        return channelRepository.findByTelegramChannelId(channelId)
                .flatMap(channel ->
                        postRepository.existsByChannelIdAndTelegramMessageId(channel.getId(), messageId)
                                .flatMap(exists -> {
                                    if (exists) {
                                        log.info("Post {} already exists in channel {}", messageId, channelId);
                                        return Mono.empty();
                                    }
                                    Post post = new Post();
                                    post.setChannelId(channel.getId());
                                    post.setTelegramMessageId(messageId);
                                    post.setTelegramDate(convertUnixToLocalDateTime(date));
                                    post.setContentType(ContentType.TEXT);
                                    post.setStatus(PostStatus.REVIEW);
                                    return postRepository.save(post)
                                            .flatMap(saved -> {
                                                TextPost textPost = new TextPost();
                                                textPost.setPostId(saved.getId());
                                                textPost.setContentText(text);
                                                return entityTemplate.insert(TextPost.class)
                                                        .using(textPost)
                                                        .thenReturn(saved);
                                            })
                                            .doOnNext(saved -> log.info("Saved text post: {}", saved.getId()));
                                }));
    }

    public Mono<Post> saveMediaPost(String channelId, Long messageId, Integer date,
                                    String telegramFileUniqueId,
                                    byte[] mediaData, String fileName, String caption) {
        return channelRepository.findByTelegramChannelId(channelId)
                .flatMap(channel ->
                        postRepository.existsByChannelIdAndTelegramMessageId(channel.getId(), messageId)
                                .flatMap(exists -> {
                                    if (exists) {
                                        log.info("Post {} already exists in channel {}", messageId, channelId);
                                        return Mono.empty();
                                    }
                                    ContentType contentType = determineContentType(fileName);
                                    return minioService.uploadFile(mediaData, fileName, getMimeType(fileName))
                                            .flatMap(objectName -> {
                                                Post post = new Post();
                                                post.setChannelId(channel.getId());
                                                post.setTelegramMessageId(messageId);
                                                post.setTelegramDate(convertUnixToLocalDateTime(date));
                                                post.setContentType(contentType);
                                                post.setStatus(PostStatus.REVIEW);
                                                return postRepository.save(post)
                                                        .flatMap(saved -> {
                                                            ImagePost imagePost = new ImagePost();
                                                            imagePost.setPostId(saved.getId());
                                                            imagePost.setMediaObjectName(objectName);
                                                            imagePost.setTelegramFileUniqueId(telegramFileUniqueId);
                                                            imagePost.setCaption(caption);
                                                            return entityTemplate.insert(ImagePost.class)
                                                                    .using(imagePost)
                                                                    .thenReturn(saved);
                                                        })
                                                        .doOnNext(saved -> log.info("Saved media post: {}, object: {}",
                                                                saved.getId(), objectName));
                                            });
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
            case "jpg", "jpeg", "png", "webp" -> ContentType.IMAGE;
            case "mp4", "mov", "avi" -> ContentType.VIDEO;
            case "gif" -> ContentType.ANIMATION;
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
            case "gif" -> "image/gif";
            case "mp4" -> "video/mp4";
            default -> "application/octet-stream";
        };
    }
}