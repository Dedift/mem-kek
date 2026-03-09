package mm.memkek.dto.response;

import mm.memkek.dao.enums.ContentType;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dao.enums.PostStatus;

/**
 * DTO для ответа с данными поста (включая контент)
 */
public record PostResponse(
        String id,
        ContentType contentType,
        PostStatus status,
        MemeCategory category,
        String telegramDate,
        String createdAt,
        String approvedAt,
        String textContent,
        String mediaUrl,
        String caption) {
}
