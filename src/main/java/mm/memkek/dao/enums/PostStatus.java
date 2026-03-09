package mm.memkek.dao.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {
    REVIEW("На ревью", "Ожидает модерации"),
    APPROVED("Одобрено", "Золотые мемы/анекдоты"),
    REJECTED("Отклонено", "Не прошло модерацию");

    private final String displayName;
    private final String description;
}