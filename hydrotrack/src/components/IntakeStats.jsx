import { GlassWater, Target } from "lucide-react";

function formatMl(ml) {
  if (ml >= 1000) {
    const liters = (ml / 1000).toFixed(1);
    return `${liters}L`;
  }
  return `${ml}ml`;
}

const statCards = [
  {
    key: "consumed",
    icon: GlassWater,
    label: "Consumed",
    gradient: "linear-gradient(135deg, #38bdf8, #818cf8)",
    glow: "rgba(56,189,248,0.25)",
    getValue: (intake) => formatMl(intake),
  },
  {
    key: "remaining",
    icon: Target,
    label: "Remaining",
    gradient: "linear-gradient(135deg, #34d399, #38bdf8)",
    glow: "rgba(52,211,153,0.25)",
    getValue: (_, remaining) => formatMl(remaining),
  },
];

export default function IntakeStats({ intake, remaining, goal }) {
  const progressPct = Math.min((intake / goal) * 100, 100);

  return (
    <div
      style={{
        width: "100%",
        maxWidth: "340px",
        display: "flex",
        flexDirection: "column",
        gap: "10px",
      }}
    >
      {/* Stat cards row */}
      <div style={{ display: "flex", gap: "10px" }}>
        {statCards.map(
          ({ key, icon: Icon, label, gradient, glow, getValue }) => {
            const value =
              key === "consumed"
                ? getValue(intake)
                : getValue(intake, remaining);
            return (
              <div
                key={key}
                className="animate-fade-up"
                style={{
                  flex: 1,
                  borderRadius: "20px",
                  padding: "16px",
                  background: "rgba(255,255,255,0.045)",
                  border: "1px solid rgba(255,255,255,0.09)",
                  backdropFilter: "blur(16px)",
                  WebkitBackdropFilter: "blur(16px)",
                  boxShadow: `0 4px 24px rgba(0,0,0,0.25), 0 0 0 0 ${glow}`,
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  gap: "8px",
                  position: "relative",
                  overflow: "hidden",
                }}
              >
                {/* Top shine */}
                <div
                  style={{
                    position: "absolute",
                    top: 0,
                    left: 0,
                    right: 0,
                    height: "45%",
                    background:
                      "linear-gradient(180deg, rgba(255,255,255,0.06) 0%, transparent 100%)",
                    borderRadius: "20px 20px 0 0",
                    pointerEvents: "none",
                  }}
                />

                {/* Gradient icon container */}
                <div
                  style={{
                    width: "36px",
                    height: "36px",
                    borderRadius: "12px",
                    background: gradient,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    boxShadow: `0 4px 16px ${glow}`,
                    flexShrink: 0,
                  }}
                >
                  <Icon size={18} color="#fff" strokeWidth={2.2} />
                </div>

                {/* Label */}
                <span
                  style={{
                    fontSize: "0.65rem",
                    fontWeight: 600,
                    letterSpacing: "0.1em",
                    textTransform: "uppercase",
                    color: "rgba(100,116,139,0.9)",
                    lineHeight: 1,
                  }}
                >
                  {label}
                </span>

                {/* Value */}
                <span
                  style={{
                    fontSize: "1.55rem",
                    fontWeight: 800,
                    letterSpacing: "-0.04em",
                    color: "#f1f5f9",
                    lineHeight: 1,
                    transition: "all 0.35s ease",
                  }}
                >
                  {value}
                </span>

                {/* Bottom accent */}
                <div
                  style={{
                    position: "absolute",
                    bottom: 0,
                    left: "25%",
                    right: "25%",
                    height: "2px",
                    borderRadius: "999px",
                    background: gradient,
                    opacity: 0.45,
                  }}
                />
              </div>
            );
          },
        )}
      </div>

      {/* Progress bar */}
      <div
        style={{
          width: "100%",
          borderRadius: "20px",
          padding: "12px 16px",
          background: "rgba(255,255,255,0.03)",
          border: "1px solid rgba(255,255,255,0.07)",
          backdropFilter: "blur(16px)",
          WebkitBackdropFilter: "blur(16px)",
          display: "flex",
          flexDirection: "column",
          gap: "8px",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <span
            style={{
              fontSize: "0.7rem",
              fontWeight: 600,
              letterSpacing: "0.08em",
              textTransform: "uppercase",
              color: "rgba(100,116,139,0.8)",
            }}
          >
            Daily progress
          </span>
          <span
            style={{
              fontSize: "0.75rem",
              fontWeight: 700,
              color: progressPct >= 100 ? "#34d399" : "#38bdf8",
              transition: "color 0.4s ease",
            }}
          >
            {formatMl(intake)} / {formatMl(goal)}
          </span>
        </div>

        {/* Track */}
        <div
          style={{
            width: "100%",
            height: "6px",
            borderRadius: "999px",
            background: "rgba(255,255,255,0.07)",
            overflow: "hidden",
          }}
        >
          {/* Fill */}
          <div
            style={{
              height: "100%",
              width: `${progressPct}%`,
              borderRadius: "999px",
              background:
                progressPct >= 100
                  ? "linear-gradient(90deg, #34d399, #38bdf8)"
                  : "linear-gradient(90deg, #38bdf8, #818cf8)",
              boxShadow:
                progressPct >= 100
                  ? "0 0 10px rgba(52,211,153,0.6)"
                  : "0 0 10px rgba(56,189,248,0.5)",
              transition:
                "width 0.85s cubic-bezier(0.4, 0, 0.2, 1), background 0.5s ease, box-shadow 0.5s ease",
            }}
          />
        </div>
      </div>
    </div>
  );
}
