import { type ReactNode } from 'react';
import { motion } from 'framer-motion';

interface CardProps {
    children: ReactNode;
    className?: string;
    hover?: boolean;
    onClick?: () => void;
}

export default function Card({ children, className = '', hover = false, onClick }: CardProps) {
    return (
        <motion.div
            whileHover={hover ? { y: -4, scale: 1.01 } : undefined}
            transition={{ type: 'spring', stiffness: 300, damping: 20 }}
            onClick={onClick}
            className={`
        glass rounded-2xl overflow-hidden
        ${hover ? 'cursor-pointer hover:shadow-2xl hover:shadow-indigo-500/10 transition-shadow duration-300' : ''}
        ${onClick ? 'cursor-pointer' : ''}
        ${className}
      `}
        >
            {children}
        </motion.div>
    );
}
