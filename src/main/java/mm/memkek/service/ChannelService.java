package mm.memkek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.entity.Channel;
import mm.memkek.dto.request.ChannelCreateRequest;
import mm.memkek.dto.response.ChannelResponse;
import mm.memkek.mapper.ChannelMapper;
import mm.memkek.repository.ChannelQueryRepository;
import mm.memkek.repository.ChannelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelQueryRepository channelQueryRepository;
    private final ChannelMapper channelMapper;
    private final TelegramApiService telegramApiService;

    public Mono<ChannelResponse> createChannel(ChannelCreateRequest request) {
        return channelRepository.existsByTelegramChannelId(request.telegramChannelId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Channel with ID " + request.telegramChannelId() + " already exists"
                        ));
                    }
                    return telegramApiService.getChannelTitle(request.telegramChannelId())
                            .flatMap(title -> {
                                Channel channel = new Channel();
                                channel.setTelegramChannelId(request.telegramChannelId());
                                channel.setChannelName(title);
                                return channelRepository.save(channel);
                            });
                })
                .doOnNext(saved -> log.info("Created new channel: {} ({})",
                        saved.getChannelName(), saved.getTelegramChannelId()))
                .map(channelMapper::toResponse);
    }

    public Mono<Page<ChannelResponse>> getAllChannels(Pageable pageable) {
        Mono<List<ChannelResponse>> items = channelQueryRepository.findAll(pageable)
                .map(channelMapper::toResponse)
                .collectList();
        Mono<Long> total = channelQueryRepository.countAll();
        return items.zipWith(total)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    public Mono<Page<ChannelResponse>> getActiveChannels(Pageable pageable) {
        Mono<List<ChannelResponse>> items = channelQueryRepository.findActive(pageable)
                .map(channelMapper::toResponse)
                .collectList();
        Mono<Long> total = channelQueryRepository.countActive();
        return items.zipWith(total)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    public Mono<ChannelResponse> getChannelById(UUID id) {
        return channelRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Channel not found with id: " + id)))
                .map(channelMapper::toResponse);
    }

    public Mono<ChannelResponse> deactivateChannel(UUID id) {
        return channelRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Channel not found with id: " + id)))
                .flatMap(channel -> {
                    channel.setIsActive(false);
                    return channelRepository.save(channel);
                })
                .doOnNext(updated -> log.info("Deactivated channel {}",
                        updated.getTelegramChannelId()))
                .map(channelMapper::toResponse);
    }

    public Mono<ChannelResponse> activateChannel(UUID id) {
        return channelRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Channel not found with id: " + id)))
                .flatMap(channel -> {
                    channel.setIsActive(true);
                    return channelRepository.save(channel);
                })
                .doOnNext(updated -> log.info("Activated channel {}",
                        updated.getTelegramChannelId()))
                .map(channelMapper::toResponse);
    }

    public Mono<Boolean> isChannelActive(String channelId) {
        log.debug("Checking if channel is active: {}", channelId);

        Mono<Channel> direct = channelRepository.findByTelegramChannelId(channelId);
        Mono<Channel> withAt = !channelId.startsWith("@")
                ? channelRepository.findByTelegramChannelId("@" + channelId)
                : Mono.empty();
        Mono<Channel> withoutAt = channelId.startsWith("@")
                ? channelRepository.findByTelegramChannelId(channelId.substring(1))
                : Mono.empty();

        return direct.switchIfEmpty(withAt).switchIfEmpty(withoutAt)
                .map(Channel::getIsActive)
                .defaultIfEmpty(false)
                .doOnNext(isActive -> log.debug("Channel {} active: {}", channelId, isActive));
    }

    public Mono<Void> deleteChannel(UUID id) {
        return channelRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Channel not found with id: " + id)))
                .flatMap(channel -> {
                    log.info("Deleting channel: {} ({})", channel.getChannelName(),
                            channel.getTelegramChannelId());
                    return channelRepository.delete(channel);
                });
    }
}
