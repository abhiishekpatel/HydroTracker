import { useHaptics } from "../hooks/useHaptics";

const VOLUMES = [
  { ml: 250, label: "250 ml", icon: "💧", sub: "small glass" },
  { ml: 500, label: "500 ml", icon: "🥤", sub: "standard" },
  { ml: 750, label: "750 ml", icon: "🫗", sub: "large glass" },
  { ml: 1000, label: "1 L", icon: "🍶", sub: "full bottle" },
];

export default function QuickAddButtons({ onAdd, disabled }) {
  const haptics = useHaptics();

  const handlePress = (ml) => {
    haptics.addWater();
    onAdd(ml);
  };

  return (
    <div
      style={{
        width: "100%",
        maxWidth: "340px",
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "10px",
      }}
    >
      {VOLUMES.map(({ ml, label, icon, sub }) => (
        <button
          key={ml}
          onClick={() => handlePress(ml)}
          disabled={disabled}
          className="pressable"
          style={{
            position: "relative",
            overflow: "hidden",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            gap: "4px",
            padding: "16px 12px",
            borderRadius: "20px",
            background: "rgba(255,255,255,0.045)",
            border: "1px solid rgba(255,255,255,0.09)",
            backdropFilter: "blur(16px)",
            WebkitBackdropFilter: "blur(16px)",
            cursor: disabled ? "not-allowed" : "pointer",
            opacity: disabled ? 0.4 : 1,
            boxShadow: "0 4px 24px rgba(0,0,0,0.25)",
            WebkitTapHighlightColor: "transparent",
            outline: "none",
          }}
          onMouseEnter={(e) => {
            if (!disabled) {
              e.currentTarget.style.background = "rgba(255,255,255,0.08)";
              e.currentTarget.style.borderColor = "rgba(56,189,248,0.3)";
              e.currentTarget.style.boxShadow =
                "0 4px 28px rgba(0,0,0,0.3), 0 0 0 1px rgba(56,189,248,0.15)";
            }
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = "rgba(255,255,255,0.045)";
            e.currentTarget.style.borderColor = "rgba(255,255,255,0.09)";
            e.currentTarget.style.boxShadow = "0 4px 24px rgba(0,0,0,0.25)";
          }}
          onMouseDown={(e) => {
            if (!disabled) e.currentTarget.style.transform = "scale(0.93)";
          }}
          onMouseUp={(e) => {
            e.currentTarget.style.transform = "scale(1)";
          }}
          onTouchStart={(e) => {
            if (!disabled) e.currentTarget.style.transform = "scale(0.93)";
          }}
          onTouchEnd={(e) => {
            e.currentTarget.style.transform = "scale(1)";
          }}
        >
          {/* Subtle top-left shine */}
          <div
            style={{
              position: "absolute",
              top: 0,
              left: 0,
              right: 0,
              height: "50%",
              background:
                "linear-gradient(180deg, rgba(255,255,255,0.07) 0%, transparent 100%)",
              borderRadius: "20px 20px 0 0",
              pointerEvents: "none",
            }}
          />

          {/* Icon */}
          <span
            style={{
              fontSize: "1.6rem",
              lineHeight: 1,
              filter: "drop-shadow(0 2px 6px rgba(56,189,248,0.35))",
            }}
          >
            {icon}
          </span>

          {/* Amount label */}
          <span
            style={{
              fontSize: "0.95rem",
              fontWeight: 700,
              letterSpacing: "-0.02em",
              color: "#f1f5f9",
              lineHeight: 1.1,
            }}
          >
            {label}
          </span>

          {/* Sub-label */}
          <span
            style={{
              fontSize: "0.65rem",
              fontWeight: 500,
              letterSpacing: "0.06em",
              textTransform: "uppercase",
              color: "rgba(100,116,139,0.9)",
              lineHeight: 1,
            }}
          >
            {sub}
          </span>

          {/* Bottom accent bar */}
          <div
            style={{
              position: "absolute",
              bottom: 0,
              left: "20%",
              right: "20%",
              height: "2px",
              borderRadius: "999px",
              background: "linear-gradient(90deg, #38bdf8, #818cf8)",
              opacity: 0.5,
            }}
          />
        </button>
      ))}
    </div>
  );
}
