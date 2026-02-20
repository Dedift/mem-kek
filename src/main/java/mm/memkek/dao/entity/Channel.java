package mm.memkek.dao.entity;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "channels")
@Data
@NoArgsConstructor
@NullMarked
public class Channel {

    @Id
    @Nullable
    private UUID id;

    @NotBlank(message = "Telegram channel ID cannot be blank")
    @Column("telegram_channel_id")
    private String telegramChannelId;

    @NotBlank(message = "Channel name cannot be blank")
    @Column("channel_name")
    private String channelName;

    @Column("is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}