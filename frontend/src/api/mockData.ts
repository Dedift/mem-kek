import {
    type Post,
    type Channel,
    type PageResponse,
    PostStatus,
    MemeCategory,
    ContentType,
} from '../types';

// ===== Мок-картинки (Picsum для демо) =====

const MEME_IMAGES = [
    'https://picsum.photos/seed/meme1/600/600',
    'https://picsum.photos/seed/meme2/600/600',
    'https://picsum.photos/seed/meme3/600/500',
    'https://picsum.photos/seed/meme4/600/700',
    'https://picsum.photos/seed/meme5/600/550',
    'https://picsum.photos/seed/meme6/600/650',
    'https://picsum.photos/seed/meme7/600/600',
    'https://picsum.photos/seed/meme8/600/500',
    'https://picsum.photos/seed/meme9/600/700',
    'https://picsum.photos/seed/meme10/600/600',
    'https://picsum.photos/seed/meme11/600/550',
    'https://picsum.photos/seed/meme12/600/650',
];

const MEME_TEXTS = [
    '— Доктор, меня все игнорируют.\n— Следующий!',
    'Программист — это человек, который решает проблемы, о которых вы не знали, способами, которых вы не понимаете.',
    '— Сколько программистов нужно, чтобы вкрутить лампочку?\n— Ни одного. Это аппаратная проблема.',
    'Жена — мужу-программисту:\n— Сходи в магазин, купи батон хлеба. Если будут яйца — возьми десяток.\nПрограммист вернулся с 10 батонами хлеба.\n— Яйца были.',
    'Дедлайн — лучшая мотивация. Особенно когда он был вчера.',
    '— Как дела на работе?\n— Пишу код.\n— На каком языке?\n— На русском матерном.',
    'Два друга:\n— Ты чем занят?\n— Оптимизирую жизнь.\n— Как?\n— Лежу на диване и ничего не делаю. Нулевые затраты энергии.',
    'IT-рекрутер: "Мы ищем джуниора с 10 годами опыта в React 19"',
];

const CATEGORIES = Object.values(MemeCategory);

function randomId(): string {
    return crypto.randomUUID();
}

function randomDate(daysBack: number): string {
    const d = new Date();
    d.setDate(d.getDate() - Math.floor(Math.random() * daysBack));
    return d.toISOString();
}

function randomCategory(): MemeCategory {
    return CATEGORIES[Math.floor(Math.random() * CATEGORIES.length)];
}

// ===== Генерация мок-постов =====

function generateMockPost(index: number, status: PostStatus): Post {
    const isImage = index % 3 !== 0; // 2/3 картинки, 1/3 текст
    return {
        id: randomId(),
        contentType: isImage ? ContentType.IMAGE : ContentType.TEXT,
        status,
        category: randomCategory(),
        telegramDate: randomDate(30),
        createdAt: randomDate(14),
        approvedAt: status === PostStatus.APPROVED ? randomDate(7) : null,
        textContent: isImage ? null : MEME_TEXTS[index % MEME_TEXTS.length],
        mediaUrl: isImage ? MEME_IMAGES[index % MEME_IMAGES.length] : null,
        caption: isImage && Math.random() > 0.5 ? 'Когда всё идёт по плану 😂' : null,
    };
}

// генерируем по 50 постов каждого типа
const MOCK_REVIEW_POSTS: Post[] = Array.from({ length: 50 }, (_, i) =>
    generateMockPost(i, PostStatus.REVIEW)
);

const MOCK_APPROVED_POSTS: Post[] = Array.from({ length: 80 }, (_, i) =>
    generateMockPost(i, PostStatus.APPROVED)
);

// ===== Мок-каналов =====

const MOCK_CHANNELS: Channel[] = [
    { id: randomId(), channelName: 'Мемы и Котики 🐱', isActive: true, createdAt: '01.01.2026 12:00' },
    { id: randomId(), channelName: 'IT Humor 💻', isActive: true, createdAt: '15.01.2026 09:30' },
    { id: randomId(), channelName: 'Дикие Анекдоты', isActive: true, createdAt: '20.01.2026 14:15' },
    { id: randomId(), channelName: 'Мемасики Pro', isActive: false, createdAt: '05.02.2026 18:45' },
    { id: randomId(), channelName: 'Программистские Байки', isActive: true, createdAt: '28.02.2026 11:00' },
    { id: randomId(), channelName: 'Funny Animals 🐕', isActive: true, createdAt: '01.03.2026 10:00' },
];

// ===== Simulated delay =====

function delay(ms: number = 300): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

// ===== MOCK API =====

let reviewIndex = 0;

export const mockApi = {
    // --- Posts ---
    async getNextReviewPost(): Promise<Post | null> {
        await delay(400);
        if (reviewIndex >= MOCK_REVIEW_POSTS.length) {
            reviewIndex = 0; // cycle
        }
        return MOCK_REVIEW_POSTS[reviewIndex++];
    },

    async approvePost(id: string): Promise<Post> {
        await delay(200);
        const post = MOCK_REVIEW_POSTS.find((p) => p.id === id);
        if (post) {
            post.status = PostStatus.APPROVED;
            post.approvedAt = new Date().toISOString();
        }
        return post ?? MOCK_REVIEW_POSTS[0];
    },

    async rejectPost(id: string): Promise<Post> {
        await delay(200);
        const post = MOCK_REVIEW_POSTS.find((p) => p.id === id);
        if (post) {
            post.status = PostStatus.REJECTED;
        }
        return post ?? MOCK_REVIEW_POSTS[0];
    },

    async getApprovedPosts(params: {
        page: number;
        size: number;
        sort: string;
        category?: MemeCategory;
    }): Promise<PageResponse<Post>> {
        await delay(500);
        let filtered = [...MOCK_APPROVED_POSTS];

        if (params.category) {
            filtered = filtered.filter((p) => p.category === params.category);
        }

        if (params.sort === 'createdAt,asc') {
            filtered.sort((a, b) => a.createdAt.localeCompare(b.createdAt));
        } else {
            filtered.sort((a, b) => b.createdAt.localeCompare(a.createdAt));
        }

        const totalElements = filtered.length;
        const totalPages = Math.ceil(totalElements / params.size);
        const start = params.page * params.size;
        const content = filtered.slice(start, start + params.size);

        return {
            content,
            totalPages,
            totalElements,
            size: params.size,
            number: params.page,
            first: params.page === 0,
            last: params.page >= totalPages - 1,
            empty: content.length === 0,
        };
    },

    // --- Channels ---
    async getChannels(): Promise<PageResponse<Channel>> {
        await delay(400);
        return {
            content: [...MOCK_CHANNELS],
            totalPages: 1,
            totalElements: MOCK_CHANNELS.length,
            size: 20,
            number: 0,
            first: true,
            last: true,
            empty: false,
        };
    },

    async createChannel(telegramChannelId: string): Promise<Channel> {
        await delay(600);
        const newChannel: Channel = {
            id: randomId(),
            channelName: telegramChannelId.replace('@', '') + ' Channel',
            isActive: true,
            createdAt: new Date().toLocaleString('ru-RU'),
        };
        MOCK_CHANNELS.unshift(newChannel);
        return newChannel;
    },

    async activateChannel(id: string): Promise<Channel> {
        await delay(300);
        const ch = MOCK_CHANNELS.find((c) => c.id === id);
        if (ch) ch.isActive = true;
        return ch ?? MOCK_CHANNELS[0];
    },

    async deactivateChannel(id: string): Promise<Channel> {
        await delay(300);
        const ch = MOCK_CHANNELS.find((c) => c.id === id);
        if (ch) ch.isActive = false;
        return ch ?? MOCK_CHANNELS[0];
    },

    async deleteChannel(id: string): Promise<void> {
        await delay(300);
        const idx = MOCK_CHANNELS.findIndex((c) => c.id === id);
        if (idx !== -1) MOCK_CHANNELS.splice(idx, 1);
    },
};
