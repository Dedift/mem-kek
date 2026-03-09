import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Loader2, Crown, Image as ImageIcon } from 'lucide-react';
import { mockApi } from '../api/mockData';
import { type Post, type PageResponse, MemeCategory, MemeCategoryLabels, ContentType } from '../types';
import CustomSelect from '../shared/ui/CustomSelect';
import Pagination from '../shared/ui/Pagination';
import Card from '../shared/ui/Card';

export default function CollectionPage() {
    const [data, setData] = useState<PageResponse<Post> | null>(null);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(20);
    const [sort, setSort] = useState<string>('createdAt,desc');
    const [category, setCategory] = useState<string>('');

    const fetchData = useCallback(async () => {
        setLoading(true);
        try {
            const result = await mockApi.getApprovedPosts({
                page,
                size,
                sort,
                category: category ? (category as MemeCategory) : undefined,
            });
            setData(result);
        } finally {
            setLoading(false);
        }
    }, [page, size, sort, category]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handlePageChange = (newPage: number) => {
        setPage(newPage);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const categoryOptions = [
        { value: '', label: 'Все категории' },
        ...Object.entries(MemeCategoryLabels).map(([value, label]) => ({ value, label })),
    ];

    const sortOptions = [
        { value: 'createdAt,desc', label: 'Сначала новые' },
        { value: 'createdAt,asc', label: 'Сначала старые' },
    ];

    const sizeOptions = [
        { value: '20', label: '20 на странице' },
        { value: '30', label: '30 на странице' },
        { value: '50', label: '50 на странице' },
    ];

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {/* Header */}
            <div className="flex items-center justify-between mb-8">
                <div className="flex items-center gap-4">
                    <Link
                        to="/"
                        className="flex items-center gap-2 text-zinc-400 hover:text-white transition-colors text-sm"
                    >
                        <ArrowLeft size={16} />
                        К модерации
                    </Link>
                    <div className="w-px h-6 bg-zinc-800" />
                    <div className="flex items-center gap-3">
                        <Crown className="text-amber-400" size={24} />
                        <h1 className="text-2xl font-extrabold tracking-tight">
                            Золотые мемы
                        </h1>
                    </div>
                </div>

                {data && (
                    <span className="text-sm text-zinc-500">
                        Всего: <span className="text-zinc-300 font-mono">{data.totalElements}</span>
                    </span>
                )}
            </div>

            {/* Filters bar */}
            <div className="sticky top-16 z-40 glass-strong rounded-2xl px-6 py-4 mb-8">
                <div className="flex flex-wrap items-end gap-4">
                    <CustomSelect
                        label="Сортировка"
                        options={sortOptions}
                        value={sort}
                        onChange={(v) => { setSort(v); setPage(0); }}
                    />
                    <CustomSelect
                        label="Категория"
                        options={categoryOptions}
                        value={category}
                        onChange={(v) => { setCategory(v); setPage(0); }}
                    />
                    <CustomSelect
                        label="На странице"
                        options={sizeOptions}
                        value={String(size)}
                        onChange={(v) => { setSize(Number(v)); setPage(0); }}
                    />

                    {/* Active filter count */}
                    {category && (
                        <button
                            onClick={() => { setCategory(''); setPage(0); }}
                            className="
                flex items-center gap-1.5 px-3 py-2 rounded-xl
                bg-indigo-500/10 text-indigo-400 text-xs font-medium
                border border-indigo-500/20
                hover:bg-indigo-500/20 transition-colors cursor-pointer
              "
                        >
                            × Сбросить фильтры
                        </button>
                    )}
                </div>
            </div>

            {/* Grid */}
            <div className="min-h-[70vh]">
                {loading ? (
                    <div className="flex items-center justify-center py-32">
                        <Loader2 className="w-8 h-8 text-indigo-400 animate-spin" />
                    </div>
                ) : data && data.content.length > 0 ? (
                    <>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
                            {data.content.map((post, index) => (
                                <motion.div
                                    key={post.id}
                                    initial={{ opacity: 0, y: 20 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    transition={{ delay: index * 0.03, duration: 0.3 }}
                                >
                                    <Card hover className="h-full flex flex-col">
                                        {/* Image */}
                                        {post.contentType !== ContentType.TEXT && post.mediaUrl ? (
                                            <div className="relative overflow-hidden">
                                                <img
                                                    src={post.mediaUrl}
                                                    alt="Мем"
                                                    className="w-full h-48 object-cover transition-transform duration-500 group-hover:scale-105"
                                                    loading="lazy"
                                                />
                                                <div className="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent" />
                                            </div>
                                        ) : (
                                            <div className="p-5 min-h-[192px] flex items-center justify-center bg-gradient-to-br from-zinc-900 to-zinc-800">
                                                <p className="text-sm text-zinc-300 whitespace-pre-line text-center leading-relaxed line-clamp-6">
                                                    {post.textContent}
                                                </p>
                                            </div>
                                        )}

                                        {/* Footer */}
                                        <div className="p-4 flex items-center justify-between mt-auto">
                                            {post.category ? (
                                                <span className="text-xs font-medium text-indigo-400 bg-indigo-500/10 px-2.5 py-1 rounded-full">
                                                    {MemeCategoryLabels[post.category]}
                                                </span>
                                            ) : (
                                                <span />
                                            )}
                                            <div className="flex items-center gap-1.5 text-xs text-zinc-600">
                                                {post.contentType === ContentType.TEXT ? (
                                                    <span className="font-mono">TXT</span>
                                                ) : (
                                                    <ImageIcon size={12} />
                                                )}
                                            </div>
                                        </div>
                                    </Card>
                                </motion.div>
                            ))}
                        </div>

                        {/* Pagination */}
                        <div className="mt-10 pb-8">
                            <Pagination
                                currentPage={data.number}
                                totalPages={data.totalPages}
                                onPageChange={handlePageChange}
                            />
                        </div>
                    </>
                ) : (
                    <div className="flex flex-col items-center justify-center py-24 text-center">
                        <p className="text-5xl mb-4">🏜️</p>
                        <p className="text-zinc-400 font-medium text-lg">Пока пусто</p>
                        <p className="text-zinc-600 text-sm mt-2">Одобрите мемы на странице ревью</p>
                        <Link
                            to="/"
                            className="mt-6 px-5 py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-purple-500 text-white text-sm font-semibold hover:shadow-lg hover:shadow-indigo-500/25 transition-all"
                        >
                            Перейти к ревью
                        </Link>
                    </div>
                )}
            </div>
        </div>
    );
}
