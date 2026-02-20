package mm.memkek.dto.response;

import mm.memkek.dao.entity.Channel;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * DTO для ответа с данными канала
 */
public record ChannelResponse(
        UUID id,
        String telegramChannelId,
        String channelName,
        Boolean isActive,
        String createdAt,
        long postsCount
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static ChannelResponse fromEntity(Channel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getTelegramChannelId(),
                channel.getChannelName(),
                channel.getIsActive(),
                channel.getCreatedAt().format(FORMATTER),
                channel.getPosts().size()
        );
    }
}