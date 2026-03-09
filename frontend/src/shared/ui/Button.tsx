import { type ButtonHTMLAttributes, type ReactNode } from 'react';
import { motion } from 'framer-motion';

type Variant = 'flame' | 'reject' | 'neon' | 'gold' | 'ghost' | 'danger';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: Variant;
    size?: 'sm' | 'md' | 'lg' | 'icon';
    children: ReactNode;
    loading?: boolean;
}

const variantClasses: Record<Variant, string> = {
    flame:
        'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-lg shadow-orange-500/25 hover:shadow-orange-500/40',
    reject:
        'bg-zinc-800 text-zinc-300 border border-zinc-700 hover:bg-zinc-700 hover:text-white',
    neon:
        'bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-lg shadow-indigo-500/25 hover:shadow-indigo-500/40',
    gold:
        'bg-gradient-to-r from-amber-500 to-yellow-500 text-black font-bold shadow-lg shadow-amber-500/25 hover:shadow-amber-500/40',
    ghost:
        'bg-white/5 text-zinc-300 border border-white/10 hover:bg-white/10 hover:text-white',
    danger:
        'bg-red-500/10 text-red-400 border border-red-500/20 hover:bg-red-500/20 hover:text-red-300',
};

const sizeClasses: Record<string, string> = {
    sm: 'px-3 py-1.5 text-sm rounded-lg',
    md: 'px-5 py-2.5 text-sm rounded-xl',
    lg: 'px-8 py-4 text-base rounded-2xl',
    icon: 'p-4 rounded-full',
};

export default function Button({
    variant = 'ghost',
    size = 'md',
    children,
    loading = false,
    className = '',
    disabled,
    ...props
}: ButtonProps) {
    return (
        <motion.button
            whileHover={{ scale: disabled || loading ? 1 : 1.05 }}
            whileTap={{ scale: disabled || loading ? 1 : 0.95 }}
            transition={{ type: 'spring', stiffness: 400, damping: 17 }}
            className={`
        inline-flex items-center justify-center gap-2 font-semibold
        transition-all duration-200 cursor-pointer
        disabled:opacity-50 disabled:cursor-not-allowed
        ${variantClasses[variant]}
        ${sizeClasses[size]}
        ${className}
      `}
            disabled={disabled || loading}
            {...(props as any)}
        >
            {loading ? (
                <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                    <circle
                        className="opacity-25"
                        cx="12" cy="12" r="10"
                        stroke="currentColor" strokeWidth="4" fill="none"
                    />
                    <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                    />
                </svg>
            ) : null}
            {children}
        </motion.button>
    );
}
