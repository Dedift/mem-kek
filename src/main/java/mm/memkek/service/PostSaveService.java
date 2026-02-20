package mm.memkek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.entity.Channel;
import mm.memkek.dao.entity.Post;
import mm.memkek.dao.enums.ContentType;
import mm.memkek.dao.enums.PostStatus;
import mm.memkek.repository.ChannelRepository;
import mm.memkek.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostSaveService {

    private final PostRepository postRepository;
    private final ChannelRepository channelRepository;
    private final MinioService minioService;

    /**
     * Сохранить текстовый пост (анекдот)
     */
    @Transactional
    public Post saveTextPost(String channelId, Long messageId, Integer date, String text) {
        Channel channel = getOrCreateChannel(channelId);

        // Проверяем, не сохранен ли уже этот пост
        if (postRepository.existsByChannelAndTelegramMessageId(channel, messageId)) {
            log.info("Post {} already exists in channel {}", messageId, channelId);
            return null;
        }

        Post post = new Post();
        post.setChannel(channel);
        post.setTelegramMessageId(messageId);
        post.setTelegramDate(convertUnixToLocalDateTime(date));
        post.setContentType(ContentType.TEXT);
        post.setContentText(text);
        post.setStatus(PostStatus.REVIEW);

        Post savedPost = postRepository.save(post);
        log.info("Saved text post: {}", savedPost.getId());
        return savedPost;
    }

    /**
     * Сохранить медиа-пост (мем с картинкой)
     */
    @Transactional
    public Post saveMediaPost(String channelId, Long messageId, Integer date,
                              String telegramFileId, String telegramFileUniqueId,
                              byte[] mediaData, String fileName, String caption) {
        Channel channel = getOrCreateChannel(channelId);

        // Проверяем, не сохранен ли уже этот пост
        if (postRepository.existsByChannelAndTelegramMessageId(channel, messageId)) {
            log.info("Post {} already exists in channel {}", messageId, channelId);
            return null;
        }

        // Определяем тип контента по расширению
        ContentType contentType = determineContentType(fileName);

        // Загружаем в MinIO
        String mediaUrl = minioService.uploadFile(mediaData, fileName, getMimeType(fileName));

        Post post = new Post();
        post.setChannel(channel);
        post.setTelegramMessageId(messageId);
        post.setTelegramDate(convertUnixToLocalDateTime(date));
        post.setContentType(contentType);
        post.setContentText(caption); // подпись к мему
        post.setMediaUrl(mediaUrl);
        post.setTelegramFileId(telegramFileId);
        post.setTelegramFileUniqueId(telegramFileUniqueId);
        post.setStatus(PostStatus.REVIEW);

        Post savedPost = postRepository.save(post);
        log.info("Saved media post: {}, URL: {}", savedPost.getId(), mediaUrl);
        return savedPost;
    }

    private Channel getOrCreateChannel(String channelId) {
        return channelRepository.findByTelegramChannelId(channelId)
                .orElseGet(() -> {
                    Channel newChannel = new Channel();
                    newChannel.setTelegramChannelId(channelId);
                    newChannel.setChannelName("Unknown Channel"); // можно потом обновить
                    newChannel.setIsActive(true);
                    return channelRepository.save(newChannel);
                });
    }

    private LocalDateTime convertUnixToLocalDateTime(Integer unixTime) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(unixTime),
                ZoneId.systemDefault()
        );
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
