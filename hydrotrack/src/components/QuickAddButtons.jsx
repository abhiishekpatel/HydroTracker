import { Droplets } from 'lucide-react';

const VOLUMES = [
  { ml: 250, label: '250ml' },
  { ml: 500, label: '500ml' },
  { ml: 750, label: '750ml' },
  { ml: 1000, label: '1L' },
];

export default function QuickAddButtons({ onAdd, disabled }) {
  return (
    <div className="grid grid-cols-2 gap-3 w-full max-w-xs">
      {VOLUMES.map(({ ml, label }) => (
        <button
          key={ml}
          onClick={() => onAdd(ml)}
          disabled={disabled}
          className="flex items-center justify-center gap-2 py-4 px-4 bg-sky-500 hover:bg-sky-600 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-2xl shadow-md transition-all duration-150 min-h-[52px] cursor-pointer"
        >
          <Droplets size={18} />
          <span>{label}</span>
        </button>
      ))}
    </div>
  );
}
