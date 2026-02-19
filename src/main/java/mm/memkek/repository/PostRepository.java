package mm.memkek.repository;

import mm.memkek.dao.entity.Post;
import mm.memkek.dao.enums.ContentType;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dao.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatusAndCategory(
            PostStatus status,
            MemeCategory category,
            Pageable pageable
    );

    Page<Post> findByStatusAndContentType(
            PostStatus status,
            ContentType contentType,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p WHERE " +
            "p.status = :status " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:contentType IS NULL OR p.contentType = :contentType)")
    Page<Post> findGoldenMemes(
            @Param("status") PostStatus status,
            @Param("category") MemeCategory category,
            @Param("contentType") ContentType contentType,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p WHERE p.category IS NULL AND p.status = 'REVIEW'")
    Page<Post> findPostsWithoutCategory(Pageable pageable);
}