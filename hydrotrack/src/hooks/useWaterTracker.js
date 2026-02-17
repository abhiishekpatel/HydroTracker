import { useState, useEffect, useCallback } from 'react';

const DAILY_GOAL = 4000; // 4 liters in ml
const STORAGE_KEY = 'hydrotrack_data';

function getTodayKey() {
  return new Date().toISOString().split('T')[0];
}

function loadData() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function saveData(data) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  } catch (e) {
    console.error('Failed to save data:', e);
  }
}

function calculateStreak(data, today) {
  if (!data.history) return 0;

  // Check if yesterday's goal was met to continue streak
  let streak = data.history[today] >= DAILY_GOAL ? 1 : 0;
  const date = new Date(today);

  // If today hasn't reached goal yet, start checking from yesterday
  if (streak === 0) {
    // Check if today has any intake - if so, streak is from yesterday backwards
    // If not, also start from yesterday
  }

  // Count backwards from yesterday
  const startOffset = streak === 0 ? 1 : 1;
  for (let i = startOffset; i < 365; i++) {
    const d = new Date(date);
    d.setDate(d.getDate() - i);
    const key = d.toISOString().split('T')[0];
    if (data.history[key] >= DAILY_GOAL) {
      streak++;
    } else {
      break;
    }
  }

  return streak;
}

export function useWaterTracker() {
  const [intake, setIntake] = useState(0);
  const [streak, setStreak] = useState(0);
  const [goalReached, setGoalReached] = useState(false);
  const [justReachedGoal, setJustReachedGoal] = useState(false);
  const [storageError, setStorageError] = useState(false);

  // Load data on mount
  useEffect(() => {
    const today = getTodayKey();
    const data = loadData();

    if (data) {
      const todayIntake = data.history?.[today] || 0;
      setIntake(todayIntake);
      setGoalReached(todayIntake >= DAILY_GOAL);
      setStreak(calculateStreak(data, today));
    }
  }, []);

  // Check for day change every minute
  useEffect(() => {
    const interval = setInterval(() => {
      const today = getTodayKey();
      const data = loadData();
      if (data) {
        const todayIntake = data.history?.[today] || 0;
        if (todayIntake !== intake) {
          setIntake(todayIntake);
          setGoalReached(todayIntake >= DAILY_GOAL);
          setStreak(calculateStreak(data, today));
        }
      }
    }, 60000);
    return () => clearInterval(interval);
  }, [intake]);

  const addWater = useCallback((amount) => {
    const today = getTodayKey();
    const data = loadData() || { history: {} };

    if (!data.history) data.history = {};
    const currentIntake = data.history[today] || 0;
    const newIntake = currentIntake + amount;
    data.history[today] = newIntake;

    try {
      saveData(data);
      setStorageError(false);
    } catch {
      setStorageError(true);
    }

    setIntake(newIntake);

    if (newIntake >= DAILY_GOAL && currentIntake < DAILY_GOAL) {
      setGoalReached(true);
      setJustReachedGoal(true);
      setTimeout(() => setJustReachedGoal(false), 4000);
    }

    setStreak(calculateStreak(data, today));
  }, []);

  const resetToday = useCallback(() => {
    const today = getTodayKey();
    const data = loadData() || { history: {} };
    if (!data.history) data.history = {};
    data.history[today] = 0;
    saveData(data);
    setIntake(0);
    setGoalReached(false);
    setJustReachedGoal(false);
    setStreak(calculateStreak(data, today));
  }, []);

  const percentage = Math.min((intake / DAILY_GOAL) * 100, 100);
  const remaining = Math.max(DAILY_GOAL - intake, 0);

  return {
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
  };
}
