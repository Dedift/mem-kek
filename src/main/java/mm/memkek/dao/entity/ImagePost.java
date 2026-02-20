package mm.memkek.dao.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "image_posts")
@Data
@NoArgsConstructor
public class ImagePost {

    @Id
    @Column("post_id")
    private UUID postId;

    @Column("media_object_name")
    private String mediaObjectName;

    @Nullable
    @Column("telegram_file_unique_id")
    private String telegramFileUniqueId;

    @Nullable
    @Column("caption")
    private String caption;
}