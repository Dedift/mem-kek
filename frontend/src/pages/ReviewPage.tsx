import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Flame, X, Loader2 } from 'lucide-react';
import { mockApi } from '../api/mockData';
import { type Post, ContentType } from '../types';
import Button from '../shared/ui/Button';

export default function ReviewPage() {
    const [currentPost, setCurrentPost] = useState<Post | null>(null);
    const [loading, setLoading] = useState(true);
    const [direction, setDirection] = useState<'left' | 'right' | null>(null);
    const [isAnimating, setIsAnimating] = useState(false);

    const loadNext = useCallback(async () => {
        setLoading(true);
        try {
            const post = await mockApi.getNextReviewPost();
            setCurrentPost(post);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadNext();
    }, [loadNext]);

    const handleAction = async (action: 'approve' | 'reject') => {
        if (!currentPost || isAnimating) return;
        setIsAnimating(true);
        setDirection(action === 'reject' ? 'left' : 'right');

        if (action === 'approve') {
            await mockApi.approvePost(currentPost.id);
        } else {
            await mockApi.rejectPost(currentPost.id);
        }

        setTimeout(async () => {
            setDirection(null);
            await loadNext();
            setIsAnimating(false);
        }, 300);
    };

    const cardVariants = {
        enter: { opacity: 0, scale: 0.9, y: 20 },
        center: { opacity: 1, scale: 1, y: 0 },
        exitLeft: { opacity: 0, x: -300, rotate: -15, scale: 0.8 },
        exitRight: { opacity: 0, x: 300, rotate: 15, scale: 0.8 },
    };

    return (
        <div className="h-[calc(100vh-4rem)] flex flex-col items-center justify-center px-4 relative overflow-hidden">
            {/* Background gradient orbs */}
            <div className="absolute top-20 left-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
            <div className="absolute bottom-20 right-1/4 w-96 h-96 bg-orange-500/10 rounded-full blur-3xl pointer-events-none" />

            {/* Remaining badge */}
            <div className="mb-3 flex-shrink-0">
                <span
                    className="
                        inline-flex items-center gap-2
                        px-4 py-1.5 rounded-full
                        bg-indigo-500/10 border border-indigo-500/20
                        text-indigo-400 text-xs font-medium
                    "
                >
                    <span className="w-1.5 h-1.5 rounded-full bg-indigo-400 animate-pulse" />
                    Осталось на ревью: <span className="font-mono font-bold">42</span>
                </span>
            </div>

            {/* Card area */}
            <div className="relative w-full max-w-md flex-1 max-h-[60vh] flex items-center justify-center">
                <AnimatePresence mode="wait">
                    {loading ? (
                        <motion.div
                            key="loader"
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                            className="flex flex-col items-center gap-3"
                        >
                            <Loader2 className="w-7 h-7 text-indigo-400 animate-spin" />
                            <p className="text-zinc-500 text-sm">Загрузка мема...</p>
                        </motion.div>
                    ) : currentPost ? (
                        <motion.div
                            key={currentPost.id}
                            variants={cardVariants}
                            initial="enter"
                            animate="center"
                            exit={direction === 'left' ? 'exitLeft' : direction === 'right' ? 'exitRight' : 'exitLeft'}
                            transition={{ type: 'spring', stiffness: 300, damping: 25 }}
                            className="
                                absolute inset-0
                                glass-strong rounded-3xl overflow-hidden
                                shadow-2xl shadow-black/50
                                flex flex-col
                            "
                        >
                            {/* Media content */}
                            {currentPost.contentType !== ContentType.TEXT && currentPost.mediaUrl ? (
                                <div className="relative flex-1 min-h-0">
                                    <img
                                        src={currentPost.mediaUrl}
                                        alt="Мем"
                                        className="w-full h-full object-cover"
                                        loading="eager"
                                    />
                                    <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
                                    {currentPost.caption && (
                                        <p className="absolute bottom-3 left-4 right-4 text-white text-sm font-medium drop-shadow-lg">
                                            {currentPost.caption}
                                        </p>
                                    )}
                                </div>
                            ) : (
                                <div className="flex-1 min-h-0 p-6 flex items-center justify-center bg-gradient-to-br from-zinc-900/50 to-zinc-800/50">
                                    <p className="text-lg leading-relaxed text-zinc-200 whitespace-pre-line text-center font-medium">
                                        {currentPost.textContent}
                                    </p>
                                </div>
                            )}

                            {/* Category badge */}
                            <div className="px-5 py-3 flex items-center justify-between flex-shrink-0">
                                {currentPost.category ? (
                                    <span className="text-xs font-medium text-zinc-500 bg-white/5 px-2.5 py-0.5 rounded-full">
                                        {currentPost.category}
                                    </span>
                                ) : <span />}
                                <span className="text-xs text-zinc-600">
                                    {new Date(currentPost.telegramDate).toLocaleDateString('ru-RU')}
                                </span>
                            </div>
                        </motion.div>
                    ) : (
                        <motion.div
                            key="empty"
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="text-center"
                        >
                            <p className="text-4xl mb-3">🎉</p>
                            <p className="text-zinc-400 font-medium">Все мемы просмотрены!</p>
                            <p className="text-zinc-600 text-sm mt-1">Новые появятся позже</p>
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>

            {/* Action buttons */}
            <div className="flex items-center gap-8 mt-4 mb-2 flex-shrink-0">
                <Button
                    variant="reject"
                    size="icon"
                    onClick={() => handleAction('reject')}
                    disabled={loading || isAnimating || !currentPost}
                    className="!w-16 !h-16 !rounded-full shadow-xl shadow-black/30"
                >
                    <X size={28} strokeWidth={2.5} />
                </Button>

                <Button
                    variant="flame"
                    size="icon"
                    onClick={() => handleAction('approve')}
                    disabled={loading || isAnimating || !currentPost}
                    className="!w-20 !h-20 !rounded-full glow-flame"
                >
                    <Flame size={32} strokeWidth={2.5} />
                </Button>
            </div>
        </div>
    );
}
