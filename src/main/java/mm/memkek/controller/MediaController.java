package mm.memkek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dto.response.MediaUrlResponse;
import mm.memkek.repository.ImagePostRepository;
import mm.memkek.service.MinioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private static final int URL_EXPIRY_SECONDS = 3600;

    private final ImagePostRepository imagePostRepository;
    private final MinioService minioService;

    @GetMapping("/{postId}")
    public Mono<ResponseEntity<MediaUrlResponse>> getMediaUrl(@PathVariable UUID postId) {
        return imagePostRepository.findByPostId(postId)
                .flatMap(imagePost -> minioService.getPresignedUrlIfPresent(
                        imagePost.getMediaObjectName(),
                        URL_EXPIRY_SECONDS
                ))
                .map(url -> ResponseEntity.ok(new MediaUrlResponse(url)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}