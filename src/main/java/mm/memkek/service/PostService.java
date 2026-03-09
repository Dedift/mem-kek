package mm.memkek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.entity.Post;
import mm.memkek.dao.enums.ContentType;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dao.enums.PostStatus;
import mm.memkek.dto.response.PostResponse;
import mm.memkek.mapper.PostMapper;
import mm.memkek.repository.ImagePostRepository;
import mm.memkek.repository.PostQueryRepository;
import mm.memkek.repository.PostRepository;
import mm.memkek.repository.TextPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final int URL_EXPIRY_SECONDS = 3600;

    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    private final TextPostRepository textPostRepository;
    private final ImagePostRepository imagePostRepository;
    private final MinioService minioService;
    private final PostMapper postMapper;

    /**
     * Получить следующий пост для ревью
     */
    public Mono<PostResponse> getNextForReview() {
        return postQueryRepository.findNextForReview()
                .flatMap(this::enrichPost)
                .doOnNext(p -> log.debug("Next review post: {}", p.id()));
    }

    /**
     * Одобрить пост
     */
    public Mono<PostResponse> approvePost(UUID id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found: " + id)))
                .flatMap(post -> {
                    post.setStatus(PostStatus.APPROVED);
                    post.setApprovedAt(LocalDateTime.now());
                    return postRepository.save(post);
                })
                .flatMap(this::enrichPost)
                .doOnNext(p -> log.info("Approved post: {}", id));
    }

    /**
     * Отклонить пост
     */
    public Mono<PostResponse> rejectPost(UUID id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found: " + id)))
                .flatMap(post -> {
                    post.setStatus(PostStatus.REJECTED);
                    return postRepository.save(post);
                })
                .flatMap(this::enrichPost)
                .doOnNext(p -> log.info("Rejected post: {}", id));
    }

    /**
     * Получить одобренные посты с пагинацией и опциональным фильтром по категории
     */
    public Mono<Page<PostResponse>> getApprovedPosts(Pageable pageable, MemeCategory category) {
        Mono<List<PostResponse>> items = postQueryRepository.findApproved(pageable, category)
                .flatMap(this::enrichPost)
                .collectList();
        Mono<Long> total = postQueryRepository.countApproved(category);

        return items.zipWith(total)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    /**
     * Получить пост по id
     */
    public Mono<PostResponse> getPostById(UUID id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Post not found: " + id)))
                .flatMap(this::enrichPost);
    }

    /**
     * Обогатить пост контентом (текст или медиа URL)
     */
    private Mono<PostResponse> enrichPost(Post post) {
        if (post.getContentType() == ContentType.TEXT) {
            return textPostRepository.findByPostId(post.getId())
                    .map(textPost -> postMapper.toResponse(post, textPost.getContentText(), null, null))
                    .defaultIfEmpty(postMapper.toResponse(post, null, null, null));
        } else {
            return imagePostRepository.findByPostId(post.getId())
                    .flatMap(imagePost -> minioService
                            .getPresignedUrlIfPresent(imagePost.getMediaObjectName(), URL_EXPIRY_SECONDS)
                            .map(url -> postMapper.toResponse(post, null, url, imagePost.getCaption()))
                            .defaultIfEmpty(postMapper.toResponse(post, null, null, imagePost.getCaption())))
                    .defaultIfEmpty(postMapper.toResponse(post, null, null, null));
        }
    }
}
