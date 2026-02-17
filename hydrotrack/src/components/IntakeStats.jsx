import { GlassWater, Target } from 'lucide-react';

function formatMl(ml) {
  if (ml >= 1000) {
    const liters = (ml / 1000).toFixed(1);
    return `${liters}L`;
  }
  return `${ml}ml`;
}

export default function IntakeStats({ intake, remaining, goal }) {
  return (
    <div className="flex gap-4 w-full max-w-xs justify-center">
      <div className="flex-1 bg-white/80 backdrop-blur rounded-2xl p-3 shadow text-center">
        <GlassWater size={20} className="text-sky-500 mx-auto mb-1" />
        <p className="text-xs text-gray-500">Consumed</p>
        <p className="text-lg font-bold text-sky-700">{formatMl(intake)}</p>
      </div>
      <div className="flex-1 bg-white/80 backdrop-blur rounded-2xl p-3 shadow text-center">
        <Target size={20} className="text-sky-500 mx-auto mb-1" />
        <p className="text-xs text-gray-500">Remaining</p>
        <p className="text-lg font-bold text-sky-700">{formatMl(remaining)}</p>
      </div>
    </div>
  );
}
