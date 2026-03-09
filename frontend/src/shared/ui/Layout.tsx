import { type ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Flame, Crown, Radio } from 'lucide-react';
import { motion } from 'framer-motion';

const NAV_ITEMS = [
    { path: '/', label: 'Ревью', icon: Flame, golden: false },
    { path: '/collection', label: 'Коллекция', icon: Crown, golden: true },
    { path: '/crm', label: 'CRM', icon: Radio, golden: false },
];

export default function Layout({ children }: { children: ReactNode }) {
    const location = useLocation();

    return (
        <div className="min-h-screen flex flex-col">
            {/* Header */}
            <header className="sticky top-0 z-50 glass-strong flex-shrink-0">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center justify-between h-16">
                        {/* Logo */}
                        <Link to="/" className="flex items-center gap-3 group">
                            <motion.div
                                animate={{ rotate: [0, -10, 10, 0] }}
                                transition={{ duration: 2, repeat: Infinity, repeatDelay: 3 }}
                                className="text-2xl"
                            >
                                🔥
                            </motion.div>
                            <span className="text-xl font-extrabold tracking-tight bg-gradient-to-r from-orange-400 to-red-400 bg-clip-text text-transparent">
                                mem-kek
                            </span>
                        </Link>

                        {/* Nav */}
                        <nav className="flex items-center gap-1">
                            {NAV_ITEMS.map(({ path, label, icon: Icon, golden }) => {
                                const isActive = location.pathname === path;
                                return (
                                    <Link
                                        key={path}
                                        to={path}
                                        className={`
                                            relative flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium
                                            transition-all duration-200
                                            ${golden && !isActive
                                                ? 'text-amber-400 hover:text-amber-300 hover:bg-amber-500/10'
                                                : isActive
                                                    ? 'text-white'
                                                    : 'text-zinc-400 hover:text-zinc-200 hover:bg-white/5'
                                            }
                                        `}
                                    >
                                        {/* Active indicator */}
                                        {isActive && !golden && (
                                            <motion.div
                                                layoutId="activeNav"
                                                className="absolute inset-0 bg-white/10 rounded-xl border border-white/10"
                                                transition={{ type: 'spring', stiffness: 350, damping: 30 }}
                                            />
                                        )}
                                        {/* Golden active indicator */}
                                        {isActive && golden && (
                                            <motion.div
                                                layoutId="activeNav"
                                                className="absolute inset-0 bg-gradient-to-r from-amber-500/15 to-yellow-500/15 rounded-xl border border-amber-500/25 animate-shimmer"
                                                style={{ backgroundSize: '200% 100%' }}
                                                transition={{ type: 'spring', stiffness: 350, damping: 30 }}
                                            />
                                        )}
                                        <span className={`relative z-10 flex items-center gap-2 ${golden && !isActive ? 'animate-pulse-glow' : ''}`}>
                                            <Icon size={16} className={golden ? 'text-amber-400' : ''} />
                                            {label}
                                        </span>
                                        {/* Shimmer overlay for golden nav (when not active) */}
                                        {golden && !isActive && (
                                            <div className="absolute inset-0 rounded-xl bg-gradient-to-r from-transparent via-amber-400/5 to-transparent animate-shimmer pointer-events-none" />
                                        )}
                                    </Link>
                                );
                            })}
                        </nav>
                    </div>
                </div>
            </header>

            {/* Main content */}
            <main className="flex-1 flex flex-col">
                <motion.div
                    key={location.pathname}
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.3, ease: 'easeOut' }}
                    className="flex-1 flex flex-col"
                >
                    {children}
                </motion.div>
            </main>
        </div>
    );
}
