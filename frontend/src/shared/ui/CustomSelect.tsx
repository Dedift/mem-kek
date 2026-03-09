import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown } from 'lucide-react';

interface SelectOption {
    value: string;
    label: string;
}

interface CustomSelectProps {
    options: SelectOption[];
    value: string;
    onChange: (value: string) => void;
    label?: string;
    className?: string;
}

export default function CustomSelect({ options, value, onChange, label, className = '' }: CustomSelectProps) {
    const [isOpen, setIsOpen] = useState(false);
    const selectRef = useRef<HTMLDivElement>(null);

    const selectedOption = options.find((opt) => opt.value === String(value)) || options[0];

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (selectRef.current && !selectRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <div className={`flex flex-col gap-1.5 relative ${className}`} ref={selectRef}>
            {label && (
                <label className="text-xs font-medium text-zinc-400 uppercase tracking-wider">
                    {label}
                </label>
            )}
            <button
                type="button"
                onClick={() => setIsOpen(!isOpen)}
                className={`
                    flex items-center justify-between gap-3 min-w-[160px]
                    bg-zinc-900 border border-zinc-700
                    text-zinc-200 text-sm font-medium
                    px-4 py-2.5 rounded-xl
                    transition-all duration-200
                    hover:bg-zinc-800 hover:border-zinc-600
                    focus:outline-none focus:ring-2 focus:ring-indigo-500/50
                    ${isOpen ? 'bg-zinc-800 border-zinc-600' : ''}
                `}
            >
                <span className="truncate">{selectedOption?.label}</span>
                <motion.div animate={{ rotate: isOpen ? 180 : 0 }} transition={{ duration: 0.2 }}>
                    <ChevronDown size={16} className="text-zinc-400" />
                </motion.div>
            </button>

            <AnimatePresence>
                {isOpen && (
                    <motion.ul
                        initial={{ opacity: 0, y: -10, scale: 0.95 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        exit={{ opacity: 0, y: -10, scale: 0.95 }}
                        transition={{ duration: 0.15, ease: 'easeOut' }}
                        className="
                            absolute z-50 top-full left-0 mt-2 min-w-full w-max
                            bg-zinc-900 rounded-xl
                            border border-zinc-700 shadow-2xl shadow-black/80
                            py-1.5 overflow-hidden
                        "
                    >
                        {options.map((opt) => (
                            <li key={opt.value}>
                                <button
                                    type="button"
                                    onClick={() => {
                                        onChange(opt.value);
                                        setIsOpen(false);
                                    }}
                                    className={`
                                        w-full text-left px-4 py-2.5 text-sm
                                        transition-colors duration-150 cursor-pointer
                                        ${String(value) === String(opt.value)
                                            ? 'bg-indigo-500/20 text-indigo-300 font-medium'
                                            : 'text-zinc-300 hover:bg-zinc-800 hover:text-white'
                                        }
                                    `}
                                >
                                    {opt.label}
                                </button>
                            </li>
                        ))}
                    </motion.ul>
                )}
            </AnimatePresence>
        </div>
    );
}
