package mm.memkek.dao.enums;

import lombok.Getter;

@Getter
public enum ContentType {
    TEXT("Текстовый пост", "Анекдот, история"),
    IMAGE("Изображение", "Мем, картинка"),
    OTHER("Другое", "Другой тип контента");

    private final String displayName;
    private final String description;

    ContentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}