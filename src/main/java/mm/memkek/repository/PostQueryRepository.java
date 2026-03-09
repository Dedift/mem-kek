package mm.memkek.repository;

import lombok.RequiredArgsConstructor;
import mm.memkek.dao.entity.Post;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dao.enums.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final DatabaseClient databaseClient;

    /**
     * Следующий пост для ревью (самый старый со статусом REVIEW)
     */
    public Mono<Post> findNextForReview() {
        String sql = "SELECT * FROM posts WHERE status = 'REVIEW' ORDER BY created_at ASC LIMIT 1";
        return databaseClient.sql(sql)
                .map((row, meta) -> mapPost(row))
                .one();
    }

    /**
     * Одобренные посты с пагинацией и опциональной фильтрацией по категории
     */
    public Flux<Post> findApproved(Pageable pageable, MemeCategory category) {
        StringBuilder sql = new StringBuilder("SELECT * FROM posts WHERE status = 'APPROVED'");

        if (category != null) {
            sql.append(" AND category = :category");
        }

        sql.append(' ').append(orderBy(pageable));
        sql.append(" LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset());

        if (category != null) {
            spec = spec.bind("category", category.name());
        }

        return spec.map((row, meta) -> mapPost(row)).all();
    }

    /**
     * Количество одобренных постов (с опциональным фильтром по категории)
     */
    public Mono<Long> countApproved(MemeCategory category) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM posts WHERE status = 'APPROVED'");

        if (category != null) {
            sql.append(" AND category = :category");
        }

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());

        if (category != null) {
            spec = spec.bind("category", category.name());
        }

        return spec.map((row, meta) -> Objects.requireNonNull(row.get("cnt", Long.class)))
                .one()
                .defaultIfEmpty(0L);
    }

    /**
     * Найти пост по id
     */
    public Mono<Post> findById(UUID id) {
        String sql = "SELECT * FROM posts WHERE id = :id";
        return databaseClient.sql(sql)
                .bind("id", id)
                .map((row, meta) -> mapPost(row))
                .one();
    }

    private String orderBy(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return "ORDER BY created_at DESC";
        }
        StringBuilder builder = new StringBuilder("ORDER BY ");
        pageable.getSort().forEach(order -> {
            builder.append(toColumn(order.getProperty()))
                    .append(' ')
                    .append(order.getDirection().name())
                    .append(',');
        });
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private String toColumn(String property) {
        return switch (property) {
            case "id" -> "id";
            case "channelId", "channel_id" -> "channel_id";
            case "contentType", "content_type" -> "content_type";
            case "status" -> "status";
            case "category" -> "category";
            case "telegramDate", "telegram_date" -> "telegram_date";
            case "createdAt", "created_at" -> "created_at";
            case "approvedAt", "approved_at" -> "approved_at";
            default -> "created_at";
        };
    }

    private Post mapPost(io.r2dbc.spi.Row row) {
        Post post = new Post();
        post.setId(row.get("id", UUID.class));
        post.setChannelId(Objects.requireNonNull(row.get("channel_id", UUID.class)));
        post.setTelegramMessageId(Objects.requireNonNull(row.get("telegram_message_id", Long.class)));
        post.setTelegramDate(Objects.requireNonNull(row.get("telegram_date", LocalDateTime.class)));

        String contentTypeStr = Objects.requireNonNull(row.get("content_type", String.class));
        post.setContentType(mm.memkek.dao.enums.ContentType.valueOf(contentTypeStr));

        String statusStr = Objects.requireNonNull(row.get("status", String.class));
        post.setStatus(mm.memkek.dao.enums.PostStatus.valueOf(statusStr));

        String categoryStr = row.get("category", String.class);
        if (categoryStr != null) {
            post.setCategory(mm.memkek.dao.enums.MemeCategory.valueOf(categoryStr));
        }

        post.setCreatedAt(Objects.requireNonNull(row.get("created_at", LocalDateTime.class)));
        post.setApprovedAt(row.get("approved_at", LocalDateTime.class));
        return post;
    }
}
