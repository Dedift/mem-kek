package mm.memkek.dao.enums;

import lombok.Getter;

@Getter
public enum PostStatus {
    REVIEW("На ревью", "Ожидает модерации"),
    APPROVED("Одобрено", "Золотые мемы/анекдоты"),
    REJECTED("Отклонено", "Не прошло модерацию"),
    PROCESSING("В обработке", "Загружается или категоризируется");

    private final String displayName;
    private final String description;

    PostStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}