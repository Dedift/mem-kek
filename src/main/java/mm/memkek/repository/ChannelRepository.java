package mm.memkek.repository;

import mm.memkek.dao.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByTelegramChannelId(String telegramChannelId);

    List<Channel> findByIsActiveTrue();

    @Query("SELECT c FROM Channel c WHERE c.isActive = true AND SIZE(c.posts) > 0")
    List<Channel> findActiveChannelsWithPosts();

    boolean existsByTelegramChannelId(String telegramChannelId);
}