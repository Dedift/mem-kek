package mm.memkek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dto.response.PostResponse;
import mm.memkek.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Следующий пост для ревью (Tinder-стиль)
     */
    @GetMapping("/review/next")
    public Mono<ResponseEntity<PostResponse>> getNextForReview() {
        return postService.getNextForReview()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * Одобрить пост
     */
    @PatchMapping("/{id}/approve")
    public Mono<ResponseEntity<PostResponse>> approvePost(@PathVariable UUID id) {
        return postService.approvePost(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Отклонить пост
     */
    @PatchMapping("/{id}/reject")
    public Mono<ResponseEntity<PostResponse>> rejectPost(@PathVariable UUID id) {
        return postService.rejectPost(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Одобренные посты (для Золотой коллекции)
     * Поддерживает: пагинацию (page/size), сортировку (sort), фильтр по категории
     * (?category=HUMOR)
     */
    @GetMapping("/approved")
    public Mono<ResponseEntity<Page<PostResponse>>> getApprovedPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) MemeCategory category) {
        return postService.getApprovedPosts(pageable, category)
                .map(ResponseEntity::ok);
    }

    /**
     * Получить пост по id
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PostResponse>> getPostById(@PathVariable UUID id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok);
    }
}
