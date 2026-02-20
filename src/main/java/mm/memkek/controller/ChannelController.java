package mm.memkek.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.entity.Channel;
import mm.memkek.dto.request.ChannelCreateRequest;
import mm.memkek.dto.response.ChannelResponse;
import mm.memkek.service.ChannelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/crm/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    /**
     * Добавление нового канала для отслеживания
     * POST /api/crm/channels
     */
    @PostMapping
    public ResponseEntity<ChannelResponse> createChannel(
            @Valid @RequestBody ChannelCreateRequest request) {
        log.info("Creating new channel: {}", request.telegramChannelId());
        ChannelResponse created = channelService.createChannel(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Получение списка всех каналов с пагинацией
     * GET /api/crm/channels?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<ChannelResponse>> getAllChannels(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<ChannelResponse> channels = channelService.getAllChannels(pageable);
        return ResponseEntity.ok(channels);
    }

    /**
     * Получение только активных каналов
     * GET /api/crm/channels/active
     */
    @GetMapping("/active")
    public ResponseEntity<Page<ChannelResponse>> getActiveChannels(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ChannelResponse> channels = channelService.getActiveChannels(pageable);
        return ResponseEntity.ok(channels);
    }

    /**
     * Получение канала по ID
     * GET /api/crm/channels/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChannelResponse> getChannelById(@PathVariable UUID id) {
        ChannelResponse channel = channelService.getChannelById(id);
        return ResponseEntity.ok(channel);
    }

    /**
     * Получение канала по Telegram ID
     * GET /api/crm/channels/by-telegram/{telegramId}
     */
    @GetMapping("/by-telegram/{telegramId}")
    public ResponseEntity<ChannelResponse> getChannelByTelegramId(@PathVariable String telegramId) {
        // Добавляем @ если его нет
        String normalizedId = telegramId.startsWith("@") ? telegramId : "@" + telegramId;
        Channel channel = channelService.getChannelByTelegramId(normalizedId);
        return ResponseEntity.ok(ChannelResponse.fromEntity(channel));
    }

    /**
     * Обновление канала
     * PUT /api/crm/channels/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChannelResponse> updateChannel(
            @PathVariable UUID id,
            @Valid @RequestBody ChannelCreateRequest request) {
        ChannelResponse updated = channelService.updateChannel(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Активация/деактивация канала
     * PATCH /api/crm/channels/{id}/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ChannelResponse> toggleChannelActive(@PathVariable UUID id) {
        ChannelResponse toggled = channelService.toggleChannelActive(id);
        return ResponseEntity.ok(toggled);
    }

    /**
     * Удаление канала
     * DELETE /api/crm/channels/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
        channelService.deleteChannel(id);
        return ResponseEntity.noContent().build();
    }
}