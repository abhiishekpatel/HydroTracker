import { Trophy } from 'lucide-react';

export default function Celebration({ visible }) {
  if (!visible) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 pointer-events-none">
      {/* Confetti particles */}
      <div className="absolute inset-0 overflow-hidden">
        {Array.from({ length: 30 }).map((_, i) => (
          <div
            key={i}
            className="absolute w-3 h-3 rounded-full animate-confetti"
            style={{
              left: `${Math.random() * 100}%`,
              backgroundColor: ['#3b82f6', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#ec4899'][i % 6],
              animationDelay: `${Math.random() * 1}s`,
              animationDuration: `${2 + Math.random() * 2}s`,
            }}
          />
        ))}
      </div>

      {/* Trophy message */}
      <div className="bg-white/95 backdrop-blur-sm rounded-3xl shadow-2xl px-8 py-6 flex flex-col items-center gap-3 animate-pop-in">
        <Trophy size={48} className="text-amber-500" />
        <h2 className="text-2xl font-bold text-sky-700">Goal Reached!</h2>
        <p className="text-gray-600 text-center text-sm">
          You hit 4L today. Keep it up!
        </p>
      </div>
    </div>
  );
}
