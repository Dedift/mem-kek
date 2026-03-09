package mm.memkek.dao.enums;

import lombok.Getter;

@Getter
public enum MemeCategory {
    ANIMALS("Животные", "Котики, собачки и другие животные"),
    POLITICS("Политика", "Политические мемы и шутки"),
    GAMES("Игры", "Игровые мемы и приколы"),
    SCIENCE("Наука", "Научные мемы"),
    SPORTS("Спорт", "Спортивные мемы"),
    MOVIES("Кино", "Мемы про фильмы и сериалы"),
    PROGRAMMING("Программирование", "Мемы для IT-шников"),
    RELATIONSHIPS("Отношения", "Мемы про любовь и отношения"),
    LIFE("Жизнь", "Житейские ситуации и зарисовки"),
    FANTASY("Фантастика", "Вымышленные существа, миры или персонажи"),
    OTHER("Другое", "Всё остальное");

    private final String displayName;
    private final String description;

    MemeCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}