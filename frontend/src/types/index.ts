// ===== Enums (строго по бэкенду) =====

export const PostStatus = {
    REVIEW: 'REVIEW',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED',
    PROCESSING: 'PROCESSING',
} as const;
export type PostStatus = (typeof PostStatus)[keyof typeof PostStatus];

export const MemeCategory = {
    HUMOR: 'HUMOR',
    ANIMALS: 'ANIMALS',
    POLITICS: 'POLITICS',
    GAMES: 'GAMES',
    SCIENCE: 'SCIENCE',
    SPORTS: 'SPORTS',
    MOVIES: 'MOVIES',
    PROGRAMMING: 'PROGRAMMING',
    RELATIONSHIPS: 'RELATIONSHIPS',
    LIFE: 'LIFE',
    OTHER: 'OTHER',
} as const;
export type MemeCategory = (typeof MemeCategory)[keyof typeof MemeCategory];

export const MemeCategoryLabels: Record<MemeCategory, string> = {
    [MemeCategory.HUMOR]: 'Юмор',
    [MemeCategory.ANIMALS]: 'Животные',
    [MemeCategory.POLITICS]: 'Политика',
    [MemeCategory.GAMES]: 'Игры',
    [MemeCategory.SCIENCE]: 'Наука',
    [MemeCategory.SPORTS]: 'Спорт',
    [MemeCategory.MOVIES]: 'Кино',
    [MemeCategory.PROGRAMMING]: 'Программирование',
    [MemeCategory.RELATIONSHIPS]: 'Отношения',
    [MemeCategory.LIFE]: 'Жизнь',
    [MemeCategory.OTHER]: 'Другое',
};

export const ContentType = {
    TEXT: 'TEXT',
    IMAGE: 'IMAGE',
    VIDEO: 'VIDEO',
    ANIMATION: 'ANIMATION',
    OTHER: 'OTHER',
} as const;
export type ContentType = (typeof ContentType)[keyof typeof ContentType];

// ===== Response DTOs =====

export interface Post {
    id: string;
    contentType: ContentType;
    status: PostStatus;
    category: MemeCategory | null;
    telegramDate: string;
    createdAt: string;
    approvedAt: string | null;
    textContent: string | null;
    mediaUrl: string | null;
    caption: string | null;
}

export interface Channel {
    id: string;
    channelName: string;
    isActive: boolean;
    createdAt: string;
}

export interface MediaUrlResponse {
    url: string;
}

// ===== Pagination (Spring Page) =====

export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;       // current page (0-based)
    first: boolean;
    last: boolean;
    empty: boolean;
}

// ===== Request DTOs =====

export interface ChannelCreateRequest {
    telegramChannelId: string;
}

// ===== Query params =====

export interface PostsQuery {
    page: number;
    size: number;
    sort: 'createdAt,desc' | 'createdAt,asc';
    category?: MemeCategory;
}
