/**
 * useHaptics — Web Vibration API wrapper
 *
 * Works on Android Chrome / Android Firefox.
 * Gracefully no-ops on iOS Safari and desktop browsers
 * (those runtimes return false / throw, which we swallow silently).
 */
export function useHaptics() {
  const isSupported =
    typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function';

  const vibrate = (pattern) => {
    if (!isSupported) return;
    try {
      navigator.vibrate(pattern);
    } catch {
      // Silently ignore — some browsers throw instead of returning false
    }
  };

  return {
    isSupported,

    /** Ultra-light tap — UI acknowledgement */
    tap: () => vibrate(8),

    /** Slightly stronger — water successfully added */
    addWater: () => vibrate(14),

    /** Double-pulse — milestone / partial feedback */
    success: () => vibrate([18, 60, 18]),

    /** Celebration pattern — daily goal reached 🎉 */
    celebrate: () => vibrate([30, 40, 60, 40, 30, 40, 80]),

    /** Short double-tap — reset / destructive action */
    reset: () => vibrate([12, 40, 12]),

    /** Single soft buzz — navigating tips */
    soft: () => vibrate(6),
  };
}
