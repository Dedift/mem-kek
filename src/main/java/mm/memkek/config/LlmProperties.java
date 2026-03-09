package mm.memkek.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Ollama API connection.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /** Enable/disable LLM integration */
    private boolean enabled = true;

    /** Ollama API base URL */
    private String baseUrl = "http://localhost:11434";

    /** Model for text classification */
    private String textModel = "qwen2.5:3b";

    /** Model for image classification */
    private String visionModel = "llava:7b";

    /** Connection timeout in seconds */
    private long connectTimeoutSeconds = 30;

    /** Read timeout in seconds */
    private long readTimeoutSeconds = 240;

    /** Number of retry attempts on failures */
    private int maxRetries = 3;
}