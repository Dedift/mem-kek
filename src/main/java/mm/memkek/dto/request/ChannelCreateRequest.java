package mm.memkek.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO для создания/обновления канала
 */
public record ChannelCreateRequest(

        @NotBlank(message = "Telegram channel ID is required")
        @Pattern(regexp = "^@?[a-zA-Z0-9_]{5,32}$",
                message = "Channel ID must be a valid Telegram channel username (e.g., @channel or channel)")
        String telegramChannelId) {
}