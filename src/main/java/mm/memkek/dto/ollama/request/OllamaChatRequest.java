package mm.memkek.dto.ollama.request;

import mm.memkek.dto.ollama.OllamaMessage;
import mm.memkek.dto.ollama.OllamaOptions;

import java.util.List;

public record OllamaChatRequest(
        String model,
        boolean stream,
        String format,
        OllamaOptions options,
        List<OllamaMessage> messages
) {}