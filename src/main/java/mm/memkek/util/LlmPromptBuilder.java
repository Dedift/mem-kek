package mm.memkek.util;

import lombok.experimental.UtilityClass;
import mm.memkek.dao.enums.MemeCategory;

import java.util.Arrays;
import java.util.stream.Collectors;

@UtilityClass
public class LlmPromptBuilder {

    public static String buildSystemPrompt() {
        String categories = Arrays.stream(MemeCategory.values())
                .map(cat -> String.format("  %s - %s", cat.name(), cat.getDisplayName()))
                .collect(Collectors.joining("\n"));

        return String.format("""
            Ты эксперт по классификации мемов. Определи категорию мема.
            
            Доступные категории (используй ТОЛЬКО английские названия из списка):
            %s
            
            ВАЖНО: Верни ТОЛЬКО JSON в формате {"category": "CATEGORY_NAME"}
            Где CATEGORY_NAME - одно из: %s
            
            Никаких пояснений, никакого дополнительного текста, только JSON!
            
            Примеры правильных ответов:
            {"category": "PROGRAMMING"}
            {"category": "ANIMALS"}
            {"category": "SPORTS"}
            
            Не используй русские названия! Только английские имена категорий из списка.
            """,
                categories,
                Arrays.stream(MemeCategory.values()).map(Enum::name).collect(Collectors.joining(", "))
        );
    }

    public static String buildTextPrompt(String text) {
        return String.format("""
            Классифицируй этот мем:
            
            Текст: %s
            
            Ответ должен быть только JSON: {"category": "CATEGORY_NAME"}
            """, text);
    }

    public static String buildImagePrompt(String caption, String channelContext) {
        StringBuilder prompt = new StringBuilder("Классифицируй этот мем-изображение");

        if (caption != null && !caption.isBlank()) {
            prompt.append("\n\nПодпись: ").append(caption);
        }

        if (channelContext != null && !channelContext.isBlank()) {
            prompt.append("\nКонтекст канала: ").append(channelContext);
        }

        prompt.append("\n\nОтвет должен быть только JSON: {\"category\": \"CATEGORY_NAME\"}");

        return prompt.toString();
    }
}