package mm.memkek.repository;

import lombok.RequiredArgsConstructor;
import mm.memkek.dao.entity.Channel;
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
public class ChannelQueryRepository {

    private final DatabaseClient databaseClient;

    public Flux<Channel> findAll(Pageable pageable) {
        String sql = "SELECT * FROM channels " + orderBy(pageable)
                + " LIMIT :limit OFFSET :offset";
        return databaseClient.sql(sql)
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map((row, meta) -> mapChannel(row))
                .all();
    }

    public Mono<Long> countAll() {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM channels")
                .map((row, meta) -> Objects.requireNonNull(row.get("cnt", Long.class)))
                .one()
                .defaultIfEmpty(0L);
    }

    public Flux<Channel> findActive(Pageable pageable) {
        String sql = "SELECT * FROM channels WHERE is_active = true " + orderBy(pageable)
                + " LIMIT :limit OFFSET :offset";
        return databaseClient.sql(sql)
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map((row, meta) -> mapChannel(row))
                .all();
    }

    public Mono<Long> countActive() {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM channels WHERE is_active = true")
                .map((row, meta) -> Objects.requireNonNull(row.get("cnt", Long.class)))
                .one()
                .defaultIfEmpty(0L);
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
            case "telegramChannelId", "telegram_channel_id" -> "telegram_channel_id";
            case "channelName", "channel_name" -> "channel_name";
            case "isActive", "is_active" -> "is_active";
            case "createdAt", "created_at" -> "created_at";
            default -> "created_at";
        };
    }

    private Channel mapChannel(io.r2dbc.spi.Row row) {
        Channel channel = new Channel();
        channel.setId(row.get("id", UUID.class));
        channel.setTelegramChannelId(Objects.requireNonNull(row.get("telegram_channel_id", String.class)));
        channel.setChannelName(Objects.requireNonNull(row.get("channel_name", String.class)));
        channel.setIsActive(Objects.requireNonNull(row.get("is_active", Boolean.class)));
        channel.setCreatedAt(Objects.requireNonNull(row.get("created_at", LocalDateTime.class)));
        return channel;
    }
}
