package mm.memkek.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for WebClient used to communicate with Ollama API.
 * Provides optimized HTTP client with connection pooling and timeouts.
 */
@Configuration
public class WebClientConfig {

    private static final int MAX_CONNECTIONS = 20;
    private static final int PENDING_ACQUIRE_MAX_COUNT = 50;
    private static final int MAX_IDLE_TIME_SECONDS = 30;

    /**
     * Creates a WebClient.Builder with pre-configured settings for Ollama API calls.
     *
     * @param llmProperties configuration properties for timeouts and connection settings
     * @return configured WebClient.Builder instance
     */
    @Bean
    public WebClient.Builder webClientBuilder(LlmProperties llmProperties) {
        ConnectionProvider provider = ConnectionProvider.builder("ollama-pool")
                .maxConnections(MAX_CONNECTIONS)
                .pendingAcquireMaxCount(PENDING_ACQUIRE_MAX_COUNT)
                .maxIdleTime(Duration.ofSeconds(MAX_IDLE_TIME_SECONDS))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) llmProperties.getConnectTimeoutSeconds() * 1000)
                .responseTimeout(Duration.ofSeconds(llmProperties.getReadTimeoutSeconds()))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(llmProperties.getReadTimeoutSeconds(), TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(llmProperties.getReadTimeoutSeconds(), TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}