package mm.memkek.repository;

import mm.memkek.dao.entity.Post;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PostRepository extends R2dbcRepository<Post, UUID> {

    Mono<Boolean> existsByChannelIdAndTelegramMessageId(UUID channelId, Long telegramMessageId);
}