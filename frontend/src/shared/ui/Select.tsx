import { type SelectHTMLAttributes } from 'react';

interface SelectOption {
    value: string;
    label: string;
}

interface SelectProps extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'onChange'> {
    options: SelectOption[];
    label?: string;
    onChange: (value: string) => void;
}

export default function Select({ options, label, onChange, value, className = '', ...props }: SelectProps) {
    return (
        <div className="flex flex-col gap-1.5">
            {label && (
                <label className="text-xs font-medium text-zinc-400 uppercase tracking-wider">
                    {label}
                </label>
            )}
            <select
                value={value}
                onChange={(e) => onChange(e.target.value)}
                className={`
          appearance-none
          bg-white/5 backdrop-blur-md
          border border-white/10
          text-zinc-200 text-sm
          px-4 py-2.5 pr-10
          rounded-xl
          cursor-pointer
          transition-all duration-200
          hover:bg-white/10 hover:border-white/20
          focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50
          bg-[url('data:image/svg+xml;charset=utf-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2216%22%20height%3D%2216%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%23a1a1aa%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22M6%209l6%206%206-6%22%2F%3E%3C%2Fsvg%3E')]
          bg-[length:16px] bg-[right_12px_center] bg-no-repeat
          ${className}
        `}
                {...props}
            >
                {options.map((opt) => (
                    <option key={opt.value} value={opt.value} className="bg-zinc-900 text-zinc-200">
                        {opt.label}
                    </option>
                ))}
            </select>
        </div>
    );
}
