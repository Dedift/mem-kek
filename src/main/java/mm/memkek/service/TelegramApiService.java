package mm.memkek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.memkek.config.TelegramBotConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramApiService {

    private final TelegramBotConfig config;
    private final WebClient.Builder webClientBuilder;

    public Mono<String> getChannelTitle(String chatId) {
        String token = config.getToken();
        return webClientBuilder.baseUrl("https://api.telegram.org")
                .build()
                .get()
                .uri("/bot{token}/getChat?chat_id={chatId}", token, chatId)
                .retrieve()
                .bodyToMono(TelegramGetChatResponse.class)
                .flatMap(response -> {
                    if (response == null || !response.ok() || response.result() == null
                            || response.result().title() == null || response.result().title().isBlank()) {
                        return Mono.error(new IllegalArgumentException(
                                "Cannot resolve channel title for " + chatId));
                    }
                    return Mono.just(response.result().title());
                });
    }

    public record TelegramGetChatResponse(boolean ok, TelegramChat result, String description) {
    }

    public record TelegramChat(String title) {
    }
}
