package mm.memkek.mapper;

import mm.memkek.dao.entity.Post;
import mm.memkek.dto.response.PostResponse;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class PostMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Маппинг Post -> PostResponse.
     * textContent и mediaUrl заполняются отдельно в сервисе.
     */
    public PostResponse toResponse(Post post, String textContent, String mediaUrl, String caption) {
        return new PostResponse(
                post.getId() != null ? post.getId().toString() : null,
                post.getContentType(),
                post.getStatus(),
                post.getCategory(),
                post.getTelegramDate() != null ? post.getTelegramDate().format(FORMATTER) : null,
                post.getCreatedAt() != null ? post.getCreatedAt().format(FORMATTER) : null,
                post.getApprovedAt() != null ? post.getApprovedAt().format(FORMATTER) : null,
                textContent,
                mediaUrl,
                caption);
    }
}
