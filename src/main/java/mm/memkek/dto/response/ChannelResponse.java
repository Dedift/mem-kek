package mm.memkek.dto.response;

import java.util.UUID;

/**
 * DTO для ответа с данными канала
 */
public record ChannelResponse(
        UUID id,
        String channelName,
        Boolean isActive,
        String createdAt) {
}