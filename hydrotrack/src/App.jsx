import "./App.css";
import { useEffect } from "react";
import { RotateCcw, Droplets } from "lucide-react";
import { useWaterTracker } from "./hooks/useWaterTracker";
import { useHaptics } from "./hooks/useHaptics";
import WaterGlass from "./components/WaterGlass";
import QuickAddButtons from "./components/QuickAddButtons";
import IntakeStats from "./components/IntakeStats";
import StreakBadge from "./components/StreakBadge";
import Celebration from "./components/Celebration";
import HydrationTips from "./components/HydrationTips";

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

  const haptics = useHaptics();

  // Celebration haptic fires when goal is just reached
  useEffect(() => {
    if (justReachedGoal) {
      haptics.celebrate();
    }
  }, [justReachedGoal]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleReset = () => {
    haptics.reset();
    resetToday();
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        background:
          "linear-gradient(160deg, #05091a 0%, #0b1120 45%, #071024 100%)",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        padding: "0 0 48px",
        position: "relative",
        overflowX: "hidden",
      }}
    >
      {/* ── Background ambient orbs ── */}
      <div
        aria-hidden="true"
        style={{
          position: "fixed",
          inset: 0,
          pointerEvents: "none",
          zIndex: 0,
          overflow: "hidden",
        }}
      >
        {/* Top-left blue orb */}
        <div
          style={{
            position: "absolute",
            top: "-120px",
            left: "-80px",
            width: "420px",
            height: "420px",
            borderRadius: "50%",
            background:
              "radial-gradient(circle, rgba(56,189,248,0.08) 0%, transparent 70%)",
            filter: "blur(60px)",
          }}
        />
        {/* Bottom-right purple orb */}
        <div
          style={{
            position: "absolute",
            bottom: "-80px",
            right: "-60px",
            width: "360px",
            height: "360px",
            borderRadius: "50%",
            background:
              "radial-gradient(circle, rgba(129,140,248,0.07) 0%, transparent 70%)",
            filter: "blur(60px)",
          }}
        />
        {/* Center emerald orb – brightens when goal reached */}
        <div
          style={{
            position: "absolute",
            top: "35%",
            left: "50%",
            transform: "translateX(-50%)",
            width: "300px",
            height: "300px",
            borderRadius: "50%",
            background: goalReached
              ? "radial-gradient(circle, rgba(52,211,153,0.07) 0%, transparent 70%)"
              : "transparent",
            filter: "blur(60px)",
            transition: "background 1.2s ease",
          }}
        />
      </div>

      {/* ── Page content ── */}
      <div
        style={{
          position: "relative",
          zIndex: 1,
          width: "100%",
          maxWidth: "400px",
          padding: "0 20px",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          gap: "20px",
        }}
      >
        {/* ── Header ── */}
        <header
          className="animate-fade-up"
          style={{
            width: "100%",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            paddingTop: "52px",
            paddingBottom: "4px",
          }}
        >
          {/* Logo + name */}
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <div
              style={{
                width: "40px",
                height: "40px",
                borderRadius: "14px",
                background: "linear-gradient(135deg, #0ea5e9, #818cf8)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                boxShadow: "0 4px 20px rgba(56,189,248,0.35)",
                flexShrink: 0,
              }}
            >
              <Droplets size={20} color="#fff" strokeWidth={2.2} />
            </div>
            <div>
              <h1
                style={{
                  fontSize: "1.25rem",
                  fontWeight: 800,
                  letterSpacing: "-0.03em",
                  background:
                    "linear-gradient(90deg, #f1f5f9 30%, #38bdf8 100%)",
                  WebkitBackgroundClip: "text",
                  WebkitTextFillColor: "transparent",
                  backgroundClip: "text",
                  margin: 0,
                  lineHeight: 1.1,
                }}
              >
                HydroTrack
              </h1>
              <p
                style={{
                  fontSize: "0.65rem",
                  fontWeight: 500,
                  letterSpacing: "0.1em",
                  textTransform: "uppercase",
                  color: "rgba(100,116,139,0.8)",
                  margin: 0,
                  lineHeight: 1,
                  marginTop: "2px",
                }}
              >
                Daily hydration
              </p>
            </div>
          </div>

          {/* Goal reached badge (replaces subtitle when done) */}
          {goalReached && !justReachedGoal && (
            <div
              className="animate-fade-up"
              style={{
                display: "flex",
                alignItems: "center",
                gap: "6px",
                padding: "6px 12px",
                borderRadius: "999px",
                background: "rgba(52,211,153,0.1)",
                border: "1px solid rgba(52,211,153,0.25)",
              }}
            >
              <span style={{ fontSize: "0.7rem" }}>✓</span>
              <span
                style={{
                  fontSize: "0.7rem",
                  fontWeight: 700,
                  color: "#34d399",
                  letterSpacing: "0.04em",
                  whiteSpace: "nowrap",
                }}
              >
                Goal done
              </span>
            </div>
          )}
        </header>

        {/* ── Streak Badge ── */}
        {streak > 0 && <StreakBadge streak={streak} />}

        {/* ── Water Orb ── */}
        <WaterGlass percentage={percentage} />

        {/* ── Stats ── */}
        <IntakeStats intake={intake} remaining={remaining} goal={DAILY_GOAL} />

        {/* ── Section divider label ── */}
        <div
          style={{
            width: "100%",
            display: "flex",
            alignItems: "center",
            gap: "10px",
          }}
        >
          <div
            style={{
              flex: 1,
              height: "1px",
              background:
                "linear-gradient(90deg, transparent, rgba(255,255,255,0.07))",
            }}
          />
          <span
            style={{
              fontSize: "0.6rem",
              fontWeight: 700,
              letterSpacing: "0.14em",
              textTransform: "uppercase",
              color: "rgba(100,116,139,0.6)",
              whiteSpace: "nowrap",
            }}
          >
            Add Water
          </span>
          <div
            style={{
              flex: 1,
              height: "1px",
              background:
                "linear-gradient(90deg, rgba(255,255,255,0.07), transparent)",
            }}
          />
        </div>

        {/* ── Quick Add Buttons ── */}
        <QuickAddButtons onAdd={addWater} disabled={goalReached} />

        {/* ── Reset Button ── */}
        <button
          onClick={handleReset}
          style={{
            display: "flex",
            alignItems: "center",
            gap: "6px",
            padding: "8px 16px",
            borderRadius: "999px",
            background: "transparent",
            border: "1px solid rgba(255,255,255,0.07)",
            color: "rgba(100,116,139,0.7)",
            fontSize: "0.75rem",
            fontWeight: 500,
            cursor: "pointer",
            transition:
              "color 0.2s ease, border-color 0.2s ease, background 0.2s ease",
            WebkitTapHighlightColor: "transparent",
            outline: "none",
            fontFamily: "inherit",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.color = "#f87171";
            e.currentTarget.style.borderColor = "rgba(248,113,113,0.3)";
            e.currentTarget.style.background = "rgba(248,113,113,0.06)";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.color = "rgba(100,116,139,0.7)";
            e.currentTarget.style.borderColor = "rgba(255,255,255,0.07)";
            e.currentTarget.style.background = "transparent";
          }}
          onMouseDown={(e) => {
            e.currentTarget.style.transform = "scale(0.95)";
          }}
          onMouseUp={(e) => {
            e.currentTarget.style.transform = "scale(1)";
          }}
          onTouchStart={(e) => {
            e.currentTarget.style.transform = "scale(0.95)";
          }}
          onTouchEnd={(e) => {
            e.currentTarget.style.transform = "scale(1)";
          }}
        >
          <RotateCcw size={12} strokeWidth={2.5} />
          Reset today
        </button>

        {/* ── Storage Error ── */}
        {storageError && (
          <div
            className="animate-fade-up"
            style={{
              width: "100%",
              padding: "12px 16px",
              borderRadius: "16px",
              background: "rgba(239,68,68,0.08)",
              border: "1px solid rgba(239,68,68,0.22)",
              display: "flex",
              alignItems: "center",
              gap: "10px",
            }}
          >
            <span style={{ fontSize: "1rem", flexShrink: 0 }}>⚠️</span>
            <p
              style={{
                fontSize: "0.78rem",
                fontWeight: 500,
                color: "rgba(252,165,165,0.9)",
                margin: 0,
                lineHeight: 1.5,
              }}
            >
              Could not save data — check your browser storage settings.
            </p>
          </div>
        )}

        {/* ── Section divider label ── */}
        <div
          style={{
            width: "100%",
            display: "flex",
            alignItems: "center",
            gap: "10px",
          }}
        >
          <div
            style={{
              flex: 1,
              height: "1px",
              background:
                "linear-gradient(90deg, transparent, rgba(255,255,255,0.07))",
            }}
          />
          <span
            style={{
              fontSize: "0.6rem",
              fontWeight: 700,
              letterSpacing: "0.14em",
              textTransform: "uppercase",
              color: "rgba(100,116,139,0.6)",
              whiteSpace: "nowrap",
            }}
          >
            Tips
          </span>
          <div
            style={{
              flex: 1,
              height: "1px",
              background:
                "linear-gradient(90deg, rgba(255,255,255,0.07), transparent)",
            }}
          />
        </div>

        {/* ── Hydration Tips ── */}
        <HydrationTips />

        {/* ── Bottom spacer brand text ── */}
        <p
          style={{
            fontSize: "0.62rem",
            fontWeight: 500,
            letterSpacing: "0.1em",
            textTransform: "uppercase",
            color: "rgba(51,65,85,0.7)",
            marginTop: "8px",
            userSelect: "none",
          }}
        >
          HydroTrack · Stay hydrated
        </p>
      </div>

      {/* ── Celebration Overlay ── */}
      <Celebration visible={justReachedGoal} />
    </div>
  );
}

export default App;
