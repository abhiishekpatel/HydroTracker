import { Lightbulb } from 'lucide-react';
import { useState, useEffect } from 'react';

const TIPS = [
  "Creatine pulls water into your muscles. Aim for at least 4L daily to stay properly hydrated.",
  "Spread your water intake throughout the day rather than drinking large amounts at once.",
  "Drink a glass of water with each creatine dose to help absorption.",
  "If your urine is dark yellow, you need more water. Aim for a pale straw color.",
  "Dehydration while on creatine can cause muscle cramps. Don't skip your water!",
  "Drinking water before meals can help with both hydration and portion control.",
  "Keep a water bottle at your desk. Visible cues help build the habit.",
  "Cold water is absorbed slightly faster than warm water during exercise.",
  "Electrolytes matter too! Consider adding a pinch of salt to your water post-workout.",
  "Creatine loading phases require even more water - up to 5L per day.",
];

export default function HydrationTips() {
  const [tipIndex, setTipIndex] = useState(0);

  useEffect(() => {
    setTipIndex(Math.floor(Math.random() * TIPS.length));
  }, []);

  const nextTip = () => {
    setTipIndex((prev) => (prev + 1) % TIPS.length);
  };

  return (
    <div
      onClick={nextTip}
      className="w-full max-w-xs bg-sky-50 border border-sky-200 rounded-2xl p-4 cursor-pointer hover:bg-sky-100 transition-colors"
    >
      <div className="flex items-start gap-3">
        <Lightbulb size={20} className="text-amber-500 mt-0.5 shrink-0" />
        <div>
          <p className="text-xs font-semibold text-sky-600 mb-1">Hydration Tip</p>
          <p className="text-sm text-gray-700 leading-relaxed">{TIPS[tipIndex]}</p>
          <p className="text-xs text-gray-400 mt-2">Tap for another tip</p>
        </div>
      </div>
    </div>
  );
}
