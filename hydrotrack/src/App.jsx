import './App.css';
import { RotateCcw, Droplets } from 'lucide-react';
import { useWaterTracker } from './hooks/useWaterTracker';
import WaterGlass from './components/WaterGlass';
import QuickAddButtons from './components/QuickAddButtons';
import IntakeStats from './components/IntakeStats';
import StreakBadge from './components/StreakBadge';
import Celebration from './components/Celebration';
import HydrationTips from './components/HydrationTips';

function App() {
  const {
    intake,
    percentage,
    remaining,
    streak,
    goalReached,
    justReachedGoal,
    storageError,
    addWater,
    resetToday,
    DAILY_GOAL,
  } = useWaterTracker();

  return (
    <div className="min-h-screen bg-gradient-to-b from-sky-100 via-sky-50 to-white flex flex-col items-center px-4 py-8 gap-6">
      {/* Header */}
      <header className="text-center">
        <div className="flex items-center justify-center gap-2 mb-1">
          <Droplets size={28} className="text-sky-500" />
          <h1 className="text-2xl sm:text-3xl font-bold text-sky-700">
            HydroTrack
          </h1>
        </div>
        <p className="text-sm text-gray-500">
          Your daily hydration companion
        </p>
      </header>

      {/* Streak Badge */}
      <StreakBadge streak={streak} />

      {/* Water Glass Visual */}
      <WaterGlass percentage={percentage} />

      {/* Stats */}
      <IntakeStats intake={intake} remaining={remaining} goal={DAILY_GOAL} />

      {/* Quick Add Buttons */}
      <QuickAddButtons onAdd={addWater} />

      {/* Reset Button */}
      <button
        onClick={resetToday}
        className="flex items-center gap-2 text-sm text-gray-400 hover:text-red-400 transition-colors cursor-pointer"
      >
        <RotateCcw size={14} />
        Reset today
      </button>

      {/* Storage Error Notice */}
      {storageError && (
        <div className="bg-red-50 border border-red-200 text-red-600 text-sm rounded-xl px-4 py-2 max-w-xs text-center">
          Could not save data. Check your browser storage settings.
        </div>
      )}

      {/* Tips */}
      <HydrationTips />

      {/* Goal Reached Message */}
      {goalReached && !justReachedGoal && (
        <p className="text-sm text-green-600 font-medium">
          Daily goal reached! Great job staying hydrated.
        </p>
      )}

      {/* Celebration Overlay */}
      <Celebration visible={justReachedGoal} />
    </div>
  );
}

export default App;
