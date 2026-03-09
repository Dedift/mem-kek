import { type ReactNode } from 'react';

type BadgeVariant = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

interface BadgeProps {
    variant?: BadgeVariant;
    children: ReactNode;
    dot?: boolean;
    className?: string;
}

const variantClasses: Record<BadgeVariant, string> = {
    success: 'bg-emerald-500/15 text-emerald-400 border-emerald-500/25',
    warning: 'bg-amber-500/15 text-amber-400 border-amber-500/25',
    danger: 'bg-red-500/15 text-red-400 border-red-500/25',
    info: 'bg-indigo-500/15 text-indigo-400 border-indigo-500/25',
    neutral: 'bg-zinc-500/15 text-zinc-400 border-zinc-500/25',
};

const dotColors: Record<BadgeVariant, string> = {
    success: 'bg-emerald-400',
    warning: 'bg-amber-400',
    danger: 'bg-red-400',
    info: 'bg-indigo-400',
    neutral: 'bg-zinc-400',
};

export default function Badge({ variant = 'neutral', children, dot = false, className = '' }: BadgeProps) {
    return (
        <span
            className={`
        inline-flex items-center gap-1.5
        px-2.5 py-1 text-xs font-medium
        rounded-full border
        ${variantClasses[variant]}
        ${className}
      `}
        >
            {dot && (
                <span className={`w-1.5 h-1.5 rounded-full ${dotColors[variant]} animate-pulse`} />
            )}
            {children}
        </span>
    );
}
