package mm.memkek.dao.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "channels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NullMarked
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Nullable
    private UUID id;

    @NotBlank(message = "Telegram channel ID cannot be blank")
    @Column(name = "telegram_channel_id", nullable = false, unique = true)
    private String telegramChannelId;

    @NotBlank(message = "Channel name cannot be blank")
    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();
}