package mm.memkek.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO для создания/обновления канала
 */
public record ChannelCreateRequest(

        @NotBlank(message = "Telegram channel ID is required")
        @Pattern(regexp = "^@?[a-zA-Z0-9_]{5,32}$",
                message = "Channel ID must be a valid Telegram channel username (e.g., @channel or channel)")
        String telegramChannelId,

        @NotBlank(message = "Channel name is required")
        @Size(min = 1, max = 255, message = "Channel name must be between 1 and 255 characters")
        String channelName,

        Boolean isActive
) {
    public ChannelCreateRequest {
        if (isActive == null) {
            isActive = true;
        }
    }
}