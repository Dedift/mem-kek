package mm.memkek.repository;

import mm.memkek.dao.entity.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {

    Optional<Channel> findByTelegramChannelId(String telegramChannelId);

    List<Channel> findByIsActiveTrue();

    Page<Channel> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT c FROM Channel c WHERE c.isActive = true AND SIZE(c.posts) > 0")
    List<Channel> findActiveChannelsWithPosts();

    boolean existsByTelegramChannelId(String telegramChannelId);
}