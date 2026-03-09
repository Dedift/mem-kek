package mm.memkek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.config.LlmProperties;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dto.ollama.OllamaMessage;
import mm.memkek.dto.ollama.OllamaOptions;
import mm.memkek.dto.ollama.request.OllamaChatRequest;
import mm.memkek.dto.ollama.response.OllamaChatResponse;
import mm.memkek.util.LlmPromptBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmCategoryService {

    private static final MemeCategory DEFAULT_CATEGORY = MemeCategory.OTHER;

    private final WebClient.Builder webClientBuilder;
    private final LlmProperties llmProperties;
    private final LlmResponseParser responseParser;

    /**
     * Классификация текстового мема
     */
    public Mono<MemeCategory> classifyText(String text) {
        if (text == null || text.isBlank()) {
            return Mono.just(DEFAULT_CATEGORY);
        }

        MemeCategory quickCategory = responseParser.quickClassify(text);
        if (quickCategory != DEFAULT_CATEGORY) {
            log.debug("Quick classification result: {} for text: {}", quickCategory, truncate(text));
            return Mono.just(quickCategory);
        }

        return callLlmWithText(text);
    }

    /**
     * Классификация мема с изображением
     */
    public Mono<MemeCategory> classifyImage(String caption, byte[] imageData, String channelContext) {
        if (imageData == null || imageData.length == 0) {
            return Mono.just(DEFAULT_CATEGORY);
        }

        if (caption != null && !caption.isBlank()) {
            MemeCategory quickFromCaption = responseParser.quickClassify(caption);
            if (quickFromCaption != DEFAULT_CATEGORY) {
                log.debug("Quick classification from caption: {} for: {}", quickFromCaption, truncate(caption));
                return Mono.just(quickFromCaption);
            }
        }

        String base64Image = encodeImageOptimized(imageData);
        return callLlmWithImage(caption, base64Image, channelContext);
    }

    /**
     * Вызов LLM для текстовой классификации
     */
    private Mono<MemeCategory> callLlmWithText(String text) {
        List<OllamaMessage> messages = Arrays.asList(
                new OllamaMessage("system", LlmPromptBuilder.buildSystemPrompt(), null),
                new OllamaMessage("user", LlmPromptBuilder.buildTextPrompt(text), null)
        );

        return callOllama(messages, llmProperties.getTextModel())
                .doOnSubscribe(_ -> log.debug("Calling LLM for text classification"))
                .doOnSuccess(category -> log.debug("LLM text classification completed: {}", category))
                .doOnError(error -> log.error("LLM text classification failed", error));
    }

    /**
     * Вызов LLM для классификации изображения
     */
    private Mono<MemeCategory> callLlmWithImage(String caption, String base64Image, String channelContext) {
        List<OllamaMessage> messages = Arrays.asList(
                new OllamaMessage("system", LlmPromptBuilder.buildSystemPrompt(), null),
                new OllamaMessage("user", LlmPromptBuilder.buildImagePrompt(caption, channelContext),
                        Collections.singletonList(base64Image))
        );

        return callOllama(messages, llmProperties.getVisionModel())
                .doOnSubscribe(_ -> log.debug("Calling LLM for image classification"))
                .doOnSuccess(category -> log.debug("LLM image classification completed: {}", category))
                .doOnError(error -> log.error("LLM image classification failed", error));
    }

    /**
     * Базовый метод вызова Ollama API
     */
    private Mono<MemeCategory> callOllama(List<OllamaMessage> messages, String model) {
        if (!llmProperties.isEnabled()) {
            return Mono.just(DEFAULT_CATEGORY);
        }

        OllamaChatRequest request = new OllamaChatRequest(
                model,
                false,
                "json",
                new OllamaOptions(0.1),
                messages
        );

        return webClientBuilder.baseUrl(llmProperties.getBaseUrl())
                .build()
                .post()
                .uri("/api/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaChatResponse.class)
                .timeout(Duration.ofSeconds(llmProperties.getReadTimeoutSeconds()))
                .retryWhen(Retry.backoff(llmProperties.getMaxRetries(), Duration.ofSeconds(2))
                        .filter(this::shouldRetry)
                )
                .map(responseParser::parseCategory)
                .onErrorResume(this::handleError);
    }

    /**
     * Проверяет, нужно ли повторить запрос
     */
    private boolean shouldRetry(Throwable throwable) {
        return throwable instanceof TimeoutException ||
                throwable instanceof WebClientRequestException ||
                (throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
    }

    /**
     * Обработка ошибок
     */
    private Mono<MemeCategory> handleError(Throwable error) {
        if (error instanceof TimeoutException) {
            log.error("LLM timeout after {} seconds", llmProperties.getReadTimeoutSeconds());
        } else if (error instanceof WebClientRequestException) {
            log.error("Connection error to Ollama. Is Ollama running at {}? Error: {}",
                    llmProperties.getBaseUrl(), error.getMessage());
        } else {
            log.error("LLM call failed: {}", error.getMessage());
        }
        return Mono.just(DEFAULT_CATEGORY);
    }

    /**
     * Оптимизация размера изображения для отправки
     */
    private String encodeImageOptimized(byte[] imageData) {
        if (imageData.length > 5 * 1024 * 1024) {
            log.warn("Large image detected ({} MB), this may cause timeout",
                    imageData.length / (1024 * 1024));
        }
        return Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * Обрезка длинного текста для логирования
     */
    private String truncate(String text) {
        if (text == null) return "";
        return text.length() <= 50 ? text : text.substring(0, 50) + "...";
    }
}