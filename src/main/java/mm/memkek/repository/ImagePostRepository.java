package mm.memkek.repository;

import mm.memkek.dao.entity.ImagePost;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ImagePostRepository extends R2dbcRepository<ImagePost, UUID> {

    Mono<ImagePost> findByPostId(UUID postId);
}