import { Flame } from 'lucide-react';

export default function StreakBadge({ streak }) {
  if (streak <= 0) return null;

  return (
    <div className="flex items-center gap-2 bg-gradient-to-r from-orange-400 to-amber-500 text-white px-4 py-2 rounded-full shadow-md animate-bounce-once">
      <Flame size={20} className="text-yellow-200" />
      <span className="font-bold text-sm">
        {streak} day{streak !== 1 ? 's' : ''} streak!
      </span>
      <Flame size={20} className="text-yellow-200" />
    </div>
  );
}
