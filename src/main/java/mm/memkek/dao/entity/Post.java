package mm.memkek.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import mm.memkek.dao.enums.ContentType;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dao.enums.PostStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NullMarked
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Nullable
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "telegram_message_id", nullable = false)
    private Long telegramMessageId;

    @Column(name = "telegram_date", nullable = false)
    private LocalDateTime telegramDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 20, nullable = false)
    private ContentType contentType;

    @Nullable
    @Size(max = 10000, message = "Content text cannot exceed 10000 characters")
    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Nullable
    @Size(max = 1000, message = "Media URL cannot exceed 1000 characters")
    @Column(name = "media_url", length = 1000)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PostStatus status = PostStatus.REVIEW;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private MemeCategory category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Nullable
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Nullable
    @Column(name = "telegram_file_id", length = 512)
    private String telegramFileId;

    @Nullable
    @Column(name = "telegram_file_unique_id", length = 512)
    private String telegramFileUniqueId;

    /**
     * Одобрение поста
     * @throws IllegalStateException если пост не в статусе REVIEW
     */
    public void approve() {
        if (this.status != PostStatus.REVIEW) {
            throw new IllegalStateException("Only posts in REVIEW status can be approved");
        }
        this.status = PostStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Отклонение поста
     * @throws IllegalStateException если пост не в статусе REVIEW
     */
    public void reject() {
        if (this.status != PostStatus.REVIEW) {
            throw new IllegalStateException("Only posts in REVIEW status can be rejected");
        }
        this.status = PostStatus.REJECTED;
    }

    /**
     * Получить текст контента (безопасный метод)
     */
    public @Nullable String getSafeContentText() {
        return contentText;
    }

    /**
     * Получить URL медиа (безопасный метод)
     */
    public @Nullable String getSafeMediaUrl() {
        return mediaUrl;
    }
}
