import { Flame } from "lucide-react";
import { useEffect } from "react";
import { useHaptics } from "../hooks/useHaptics";

export default function StreakBadge({ streak }) {
  const haptics = useHaptics();

  useEffect(() => {
    if (streak > 0) {
      haptics.success();
    }
    // Only fire once on mount / when streak first appears
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (streak <= 0) return null;

  return (
    <div
      className="animate-bounce-once"
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: "8px",
        padding: "8px 18px",
        borderRadius: "999px",
        background: "rgba(251,146,60,0.12)",
        border: "1px solid rgba(251,146,60,0.25)",
        backdropFilter: "blur(16px)",
        WebkitBackdropFilter: "blur(16px)",
        boxShadow:
          "0 4px 24px rgba(251,146,60,0.15), inset 0 1px 0 rgba(255,255,255,0.08)",
        cursor: "default",
        userSelect: "none",
      }}
    >
      {/* Left flame */}
      <Flame
        size={16}
        style={{
          color: "#fb923c",
          filter: "drop-shadow(0 0 6px rgba(251,146,60,0.8))",
          flexShrink: 0,
        }}
        strokeWidth={2.2}
      />

      {/* Text */}
      <span
        style={{
          fontSize: "0.8rem",
          fontWeight: 700,
          letterSpacing: "0.02em",
          background: "linear-gradient(90deg, #fb923c, #fbbf24)",
          WebkitBackgroundClip: "text",
          WebkitTextFillColor: "transparent",
          backgroundClip: "text",
          lineHeight: 1,
          whiteSpace: "nowrap",
        }}
      >
        {streak} day{streak !== 1 ? "s" : ""} streak
      </span>

      {/* Right flame */}
      <Flame
        size={16}
        style={{
          color: "#fbbf24",
          filter: "drop-shadow(0 0 6px rgba(251,191,36,0.8))",
          flexShrink: 0,
        }}
        strokeWidth={2.2}
      />
    </div>
  );
}
