package mm.memkek.dao.entity;

import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.memkek.dao.enums.ContentType;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dao.enums.PostStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "posts")
@Data
@NoArgsConstructor
@NullMarked
public class Post {

    @Id
    @Nullable
    private UUID id;

    @Column("channel_id")
    private UUID channelId;

    @Column("telegram_message_id")
    private Long telegramMessageId;

    @Column("telegram_date")
    private LocalDateTime telegramDate;

    @Column("content_type")
    private ContentType contentType;

    @Nullable
    @Size(max = 10000, message = "Content text cannot exceed 10000 characters")
    @Column("content_text")
    private String contentText;

    @Nullable
    @Size(max = 1000, message = "Media object name cannot exceed 1000 characters")
    @Column("media_object_name")
    private String mediaObjectName;

    @Column("status")
    private PostStatus status = PostStatus.REVIEW;

    @Nullable
    @Column("category")
    private MemeCategory category;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @Nullable
    @Column("approved_at")
    private LocalDateTime approvedAt;

    @Nullable
    @Column("telegram_file_unique_id")
    private String telegramFileUniqueId;
}
