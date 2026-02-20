package mm.memkek.dao.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "text_posts")
@Data
@NoArgsConstructor
public class TextPost {

    @Id
    @Column("post_id")
    private UUID postId;

    @NotBlank
    @Column("content_text")
    private String contentText;
}