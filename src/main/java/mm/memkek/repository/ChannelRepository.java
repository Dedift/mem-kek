package mm.memkek.repository;

import mm.memkek.dao.entity.Channel;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ChannelRepository extends R2dbcRepository<Channel, UUID> {

    Mono<Channel> findByTelegramChannelId(String telegramChannelId);

    Mono<Boolean> existsByTelegramChannelId(String telegramChannelId);
}