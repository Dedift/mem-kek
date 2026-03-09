package mm.memkek.service;

import lombok.extern.slf4j.Slf4j;
import mm.memkek.dao.enums.MemeCategory;
import mm.memkek.dto.ollama.response.OllamaChatResponse;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class LlmResponseParser {

    private static final MemeCategory DEFAULT_CATEGORY = MemeCategory.OTHER;

    private static final Pattern CATEGORY_PATTERN_JSON = Pattern.compile("\"category\"\\s*:\\s*\"([A-Za-z_]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern CATEGORY_PATTERN_ID = Pattern.compile("\"id\"\\s*:\\s*\"([A-Za-z_]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern CATEGORY_PATTERN_VALUE = Pattern.compile("\"value\"\\s*:\\s*\"([A-Za-z_]+)\"", Pattern.CASE_INSENSITIVE);

    private static final Map<MemeCategory, Set<String>> KEYWORD_MAP = new EnumMap<>(MemeCategory.class);

    static {
        KEYWORD_MAP.put(MemeCategory.ANIMALS, Set.of(
                "кот", "кошка", "собака", "пёс", "животн", "cat", "dog", "kitten", "puppy", "animal", "pet"
        ));
        KEYWORD_MAP.put(MemeCategory.PROGRAMMING, Set.of(
                "программ", "код", "джава", "питон", "айти", "developer", "code", "java", "python", "bug", "programming"
        ));
        KEYWORD_MAP.put(MemeCategory.POLITICS, Set.of(
                "политик", "путин", "байден", "выбор", "президент", "government", "polit", "putin", "biden"
        ));
        KEYWORD_MAP.put(MemeCategory.SPORTS, Set.of(
                "спорт", "футбол", "хоккей", "баскетбол", "sport", "football", "soccer", "game", "match"
        ));
        KEYWORD_MAP.put(MemeCategory.GAMES, Set.of(
                "игр", "гейм", "play", "game", "gaming", "видеоигра"
        ));
        KEYWORD_MAP.put(MemeCategory.MOVIES, Set.of(
                "кино", "фильм", "сериал", "movie", "film", "cinema", "series"
        ));
        KEYWORD_MAP.put(MemeCategory.SCIENCE, Set.of(
                "наук", "физик", "хими", "science", "scientist", "physics", "chemistry", "biology"
        ));
        KEYWORD_MAP.put(MemeCategory.RELATIONSHIPS, Set.of(
                "любов", "отношен", "девушк", "парн", "love", "relationship", "girlfriend", "boyfriend"
        ));
        KEYWORD_MAP.put(MemeCategory.LIFE, Set.of(
                "жизн", "быт", "повседнев", "life", "everyday", "daily", "жиза"
        ));
    }

    /**
     * Быстрая классификация по ключевым словам
     */
    public MemeCategory quickClassify(String text) {
        if (text == null || text.isBlank()) {
            return DEFAULT_CATEGORY;
        }

        String lowerText = text.toLowerCase();

        for (Map.Entry<MemeCategory, Set<String>> entry : KEYWORD_MAP.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return DEFAULT_CATEGORY;
    }

    /**
     * Парсит категорию из ответа LLM
     */
    public MemeCategory parseCategory(OllamaChatResponse response) {
        if (response == null || response.message() == null || response.message().content() == null) {
            log.warn("Empty response from LLM");
            return DEFAULT_CATEGORY;
        }

        String content = response.message().content().trim();
        log.debug("LLM raw response: {}", content);

        String categoryValue = extractCategoryFromContent(content);

        if (categoryValue != null) {
            return mapToMemeCategory(categoryValue);
        }

        log.warn("Could not parse category from: {}", content);
        return DEFAULT_CATEGORY;
    }

    /**
     * Извлекает значение категории из JSON
     */
    private String extractCategoryFromContent(String content) {
        if (content == null) return null;

        Matcher jsonMatcher = CATEGORY_PATTERN_JSON.matcher(content);
        if (jsonMatcher.find()) {
            return jsonMatcher.group(1);
        }

        Matcher idMatcher = CATEGORY_PATTERN_ID.matcher(content);
        if (idMatcher.find()) {
            return idMatcher.group(1);
        }

        Matcher valueMatcher = CATEGORY_PATTERN_VALUE.matcher(content);
        if (valueMatcher.find()) {
            return valueMatcher.group(1);
        }

        return content;
    }

    /**
     * Маппит строковое значение в enum категории
     */
    private MemeCategory mapToMemeCategory(String value) {
        if (value == null) return DEFAULT_CATEGORY;

        String upperValue = value.toUpperCase();

        try {
            return MemeCategory.valueOf(upperValue);
        } catch (IllegalArgumentException e) {
            for (MemeCategory category : MemeCategory.values()) {
                if (upperValue.contains(category.name())) {
                    return category;
                }
            }

            return quickClassify(value);
        }
    }
}