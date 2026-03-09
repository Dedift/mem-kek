import Button from './Button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
    currentPage: number;       // 0-based
    totalPages: number;
    onPageChange: (page: number) => void;
}

export default function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
    if (totalPages <= 1) return null;

    const maxVisible = 5;
    const pages: (number | '...')[] = [];

    if (totalPages <= maxVisible + 2) {
        for (let i = 0; i < totalPages; i++) pages.push(i);
    } else {
        pages.push(0);
        if (currentPage > 2) pages.push('...');

        const start = Math.max(1, currentPage - 1);
        const end = Math.min(totalPages - 2, currentPage + 1);
        for (let i = start; i <= end; i++) pages.push(i);

        if (currentPage < totalPages - 3) pages.push('...');
        pages.push(totalPages - 1);
    }

    return (
        <nav className="flex items-center justify-center gap-1.5">
            <Button
                variant="ghost"
                size="sm"
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="!px-2"
            >
                <ChevronLeft size={16} />
            </Button>

            {pages.map((page, idx) =>
                page === '...' ? (
                    <span key={`dots-${idx}`} className="px-2 text-zinc-500 text-sm">
                        ...
                    </span>
                ) : (
                    <button
                        key={page}
                        onClick={() => onPageChange(page)}
                        className={`
              w-9 h-9 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer
              ${page === currentPage
                                ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-lg shadow-indigo-500/25'
                                : 'bg-white/5 text-zinc-400 hover:bg-white/10 hover:text-white border border-white/5'
                            }
            `}
                    >
                        {page + 1}
                    </button>
                )
            )}

            <Button
                variant="ghost"
                size="sm"
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="!px-2"
            >
                <ChevronRight size={16} />
            </Button>
        </nav>
    );
}
