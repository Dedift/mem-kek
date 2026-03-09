import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Link as LinkIcon, Loader2, Radio, Pencil, Trash2, Power, PowerOff } from 'lucide-react';
import { mockApi } from '../api/mockData';
import { type Channel } from '../types';
import Button from '../shared/ui/Button';
import Badge from '../shared/ui/Badge';

export default function CrmPage() {
    const [channels, setChannels] = useState<Channel[]>([]);
    const [loading, setLoading] = useState(true);
    const [inputValue, setInputValue] = useState('');
    const [creating, setCreating] = useState(false);
    const [hoveredId, setHoveredId] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const fetchChannels = useCallback(async () => {
        setLoading(true);
        try {
            const res = await mockApi.getChannels();
            setChannels(res.content);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchChannels();
    }, [fetchChannels]);

    const handleAdd = async () => {
        if (!inputValue.trim()) return;
        setError(null);
        setCreating(true);
        try {
            await mockApi.createChannel(inputValue.trim());
            setInputValue('');
            await fetchChannels();
        } catch {
            setError('Не удалось добавить канал');
        } finally {
            setCreating(false);
        }
    };

    const handleToggle = async (channel: Channel) => {
        if (channel.isActive) {
            await mockApi.deactivateChannel(channel.id);
        } else {
            await mockApi.activateChannel(channel.id);
        }
        await fetchChannels();
    };

    const handleDelete = async (id: string) => {
        await mockApi.deleteChannel(id);
        await fetchChannels();
    };

    return (
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {/* Header */}
            <div className="mb-8">
                <div className="flex items-center gap-3 mb-2">
                    <Radio className="text-indigo-400" size={24} />
                    <h1 className="text-2xl font-extrabold tracking-tight">
                        Управление источниками
                    </h1>
                </div>
                <p className="text-zinc-500 text-sm">
                    Добавляйте Telegram-каналы для автоматического сбора мемов
                </p>
            </div>

            {/* Add channel block */}
            <div className="glass-strong rounded-2xl p-6 mb-8">
                <h2 className="text-sm font-semibold text-zinc-300 mb-4 uppercase tracking-wider">
                    Добавить канал
                </h2>
                <div className="flex gap-3">
                    <div className="relative flex-1">
                        <LinkIcon
                            size={16}
                            className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500"
                        />
                        <input
                            type="text"
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
                            placeholder="@channel_name или channel_name"
                            className="
                w-full pl-11 pr-4 py-3 rounded-xl
                bg-white/5 border border-white/10
                text-zinc-200 placeholder:text-zinc-600
                text-sm
                transition-all duration-200
                focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50
                hover:bg-white/[0.07]
              "
                        />
                    </div>
                    <Button
                        variant="neon"
                        onClick={handleAdd}
                        loading={creating}
                        disabled={!inputValue.trim()}
                    >
                        <Plus size={18} />
                        Добавить канал
                    </Button>
                </div>
                {error && (
                    <motion.p
                        initial={{ opacity: 0, y: -8 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="text-red-400 text-xs mt-3"
                    >
                        {error}
                    </motion.p>
                )}
            </div>

            {/* Channels list */}
            <div className="space-y-3">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-sm font-semibold text-zinc-300 uppercase tracking-wider">
                        Каналы ({channels.length})
                    </h2>
                </div>

                {loading ? (
                    <div className="flex items-center justify-center py-16">
                        <Loader2 className="w-6 h-6 text-indigo-400 animate-spin" />
                    </div>
                ) : channels.length > 0 ? (
                    <AnimatePresence>
                        {channels.map((channel, index) => (
                            <motion.div
                                key={channel.id}
                                initial={{ opacity: 0, y: 12 }}
                                animate={{ opacity: 1, y: 0 }}
                                exit={{ opacity: 0, x: -100, height: 0 }}
                                transition={{ delay: index * 0.04, duration: 0.3 }}
                                onMouseEnter={() => setHoveredId(channel.id)}
                                onMouseLeave={() => setHoveredId(null)}
                                className="
                  glass rounded-xl px-5 py-4
                  flex items-center gap-4
                  transition-all duration-200
                  hover:bg-white/[0.07] hover:border-white/15
                  group
                "
                            >
                                {/* Avatar */}
                                <div className="
                  w-10 h-10 rounded-xl
                  bg-gradient-to-br from-indigo-500/20 to-purple-500/20
                  border border-indigo-500/20
                  flex items-center justify-center
                  text-indigo-400
                  flex-shrink-0
                ">
                                    <Radio size={18} />
                                </div>

                                {/* Info */}
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-semibold text-zinc-200 truncate">
                                        {channel.channelName}
                                    </p>
                                    <p className="text-xs text-zinc-500 mt-0.5">
                                        Добавлен: {channel.createdAt}
                                    </p>
                                </div>

                                {/* Status badge */}
                                <Badge
                                    variant={channel.isActive ? 'success' : 'neutral'}
                                    dot
                                >
                                    {channel.isActive ? 'Активен' : 'Неактивен'}
                                </Badge>

                                {/* Actions (show on hover) */}
                                <AnimatePresence>
                                    {hoveredId === channel.id && (
                                        <motion.div
                                            initial={{ opacity: 0, x: 10 }}
                                            animate={{ opacity: 1, x: 0 }}
                                            exit={{ opacity: 0, x: 10 }}
                                            className="flex items-center gap-1"
                                        >
                                            <button
                                                onClick={() => handleToggle(channel)}
                                                className="
                          p-2 rounded-lg text-zinc-400 cursor-pointer
                          hover:bg-white/10 hover:text-amber-400
                          transition-all duration-150
                        "
                                                title={channel.isActive ? 'Деактивировать' : 'Активировать'}
                                            >
                                                {channel.isActive ? <PowerOff size={16} /> : <Power size={16} />}
                                            </button>
                                            <button
                                                onClick={() => { }}
                                                className="
                          p-2 rounded-lg text-zinc-400 cursor-pointer
                          hover:bg-white/10 hover:text-indigo-400
                          transition-all duration-150
                        "
                                                title="Редактировать"
                                            >
                                                <Pencil size={16} />
                                            </button>
                                            <button
                                                onClick={() => handleDelete(channel.id)}
                                                className="
                          p-2 rounded-lg text-zinc-400 cursor-pointer
                          hover:bg-red-500/10 hover:text-red-400
                          transition-all duration-150
                        "
                                                title="Удалить"
                                            >
                                                <Trash2 size={16} />
                                            </button>
                                        </motion.div>
                                    )}
                                </AnimatePresence>
                            </motion.div>
                        ))}
                    </AnimatePresence>
                ) : (
                    <div className="text-center py-16">
                        <p className="text-4xl mb-4">📡</p>
                        <p className="text-zinc-400 font-medium">Нет добавленных каналов</p>
                        <p className="text-zinc-600 text-sm mt-2">Добавьте первый канал выше</p>
                    </div>
                )}
            </div>
        </div>
    );
}
