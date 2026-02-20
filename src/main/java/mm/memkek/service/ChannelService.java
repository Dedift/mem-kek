package mm.memkek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.entity.Channel;
import mm.memkek.dto.request.ChannelCreateRequest;
import mm.memkek.dto.response.ChannelResponse;
import mm.memkek.repository.ChannelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    /**
     * Создание нового канала
     */
    @Transactional
    public ChannelResponse createChannel(ChannelCreateRequest request) {
        // Проверяем, не существует ли уже такой канал
        if (channelRepository.existsByTelegramChannelId(request.telegramChannelId())) {
            throw new IllegalArgumentException(
                    "Channel with ID " + request.telegramChannelId() + " already exists"
            );
        }

        // Создаем новый канал
        Channel channel = new Channel();
        channel.setTelegramChannelId(request.telegramChannelId());
        channel.setChannelName(request.channelName());
        channel.setIsActive(request.isActive());

        Channel savedChannel = channelRepository.save(channel);

        // !!! ВАЖНО: принудительно сбрасываем и обновляем из БД, чтобы получить createdAt
        channelRepository.flush();

        log.info("Created new channel: {} ({})", savedChannel.getChannelName(), savedChannel.getTelegramChannelId());
        log.debug("Channel createdAt: {}", savedChannel.getCreatedAt());

        return ChannelResponse.fromEntity(savedChannel);
    }

    /**
     * Получение всех каналов с пагинацией
     */
    @Transactional(readOnly = true)
    public Page<ChannelResponse> getAllChannels(Pageable pageable) {
        return channelRepository.findAll(pageable)
                .map(ChannelResponse::fromEntity);
    }

    /**
     * Получение только активных каналов
     */
    @Transactional(readOnly = true)
    public Page<ChannelResponse> getActiveChannels(Pageable pageable) {
        return channelRepository.findByIsActiveTrue(pageable)
                .map(ChannelResponse::fromEntity);
    }

    /**
     * Получение канала по ID
     */
    @Transactional(readOnly = true)
    public ChannelResponse getChannelById(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found with id: " + id));
        return ChannelResponse.fromEntity(channel);
    }

    /**
     * Получение сущности канала по ID (для внутреннего использования)
     */
    @Transactional(readOnly = true)
    public Channel getChannelEntityById(UUID id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found with id: " + id));
    }

    /**
     * Обновление канала
     */
    @Transactional
    public ChannelResponse updateChannel(UUID id, ChannelCreateRequest request) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found with id: " + id));

        channel.setChannelName(request.channelName());
        channel.setIsActive(request.isActive());
        // telegramChannelId обычно не обновляем, но если нужно:
        if (request.telegramChannelId() != null && !request.telegramChannelId().equals(channel.getTelegramChannelId())) {
            // Проверяем, что новый ID не занят
            if (channelRepository.existsByTelegramChannelId(request.telegramChannelId())) {
                throw new IllegalArgumentException("Channel with ID " + request.telegramChannelId() + " already exists");
            }
            channel.setTelegramChannelId(request.telegramChannelId());
        }

        Channel updatedChannel = channelRepository.save(channel);
        log.info("Updated channel: {}", updatedChannel.getTelegramChannelId());

        return ChannelResponse.fromEntity(updatedChannel);
    }

    /**
     * Переключение статуса канала (активен/неактивен)
     */
    @Transactional
    public ChannelResponse toggleChannelActive(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found with id: " + id));

        channel.setIsActive(!channel.getIsActive());
        Channel updatedChannel = channelRepository.save(channel);

        log.info("Toggled channel {} active status to: {}",
                channel.getTelegramChannelId(), channel.getIsActive());

        return ChannelResponse.fromEntity(updatedChannel);
    }

    /**
     * Удаление канала
     */
    @Transactional
    public void deleteChannel(UUID id) {
        if (!channelRepository.existsById(id)) {
            throw new IllegalArgumentException("Channel not found with id: " + id);
        }
        channelRepository.deleteById(id);
        log.info("Deleted channel with id: {}", id);
    }

    /**
     * Получить канал по Telegram ID
     */
    @Transactional(readOnly = true)
    public Channel getChannelByTelegramId(String telegramChannelId) {
        return channelRepository.findByTelegramChannelId(telegramChannelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + telegramChannelId));
    }

    /**
     * Проверить, активен ли канал
     */
    @Transactional(readOnly = true)
    public boolean isChannelActive(String channelId) {
        log.debug("Checking if channel is active: {}", channelId);

        // Пробуем найти по точному совпадению
        Optional<Channel> channelOpt = channelRepository.findByTelegramChannelId(channelId);

        if (channelOpt.isEmpty() && !channelId.startsWith("@")) {
            // Если не нашли и это не username, пробуем добавить @
            channelOpt = channelRepository.findByTelegramChannelId("@" + channelId);
        }

        if (channelOpt.isEmpty() && channelId.startsWith("@")) {
            // Если не нашли с @, пробуем без @
            channelOpt = channelRepository.findByTelegramChannelId(channelId.substring(1));
        }

        boolean isActive = channelOpt.map(Channel::getIsActive).orElse(false);
        log.debug("Channel {} active: {}", channelId, isActive);

        return isActive;
    }
}