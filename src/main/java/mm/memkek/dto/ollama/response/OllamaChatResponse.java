package mm.memkek.dto.ollama.response;

import mm.memkek.dto.ollama.OllamaMessageContent;

public record OllamaChatResponse(OllamaMessageContent message) {}