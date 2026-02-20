package mm.memkek.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/crm/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public Mono<ResponseEntity<ChannelResponse>> createChannel(
            @Valid @RequestBody ChannelCreateRequest request) {
        log.info("Creating new channel: {}", request.telegramChannelId());
        return channelService.createChannel(request)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED));
    }

    @GetMapping
    public Mono<ResponseEntity<Page<ChannelResponse>>> getAllChannels(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return channelService.getAllChannels(pageable)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/active")
    public Mono<ResponseEntity<Page<ChannelResponse>>> getActiveChannels(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return channelService.getActiveChannels(pageable)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ChannelResponse>> getChannelById(@PathVariable UUID id) {
        return channelService.getChannelById(id)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/deactivate")
    public Mono<ResponseEntity<ChannelResponse>> deactivateChannel(@PathVariable UUID id) {
        return channelService.deactivateChannel(id)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/activate")
    public Mono<ResponseEntity<ChannelResponse>> activateChannel(@PathVariable UUID id) {
        return channelService.activateChannel(id)
                .map(ResponseEntity::ok);
    }
}