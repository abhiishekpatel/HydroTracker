import { Trophy, Sparkles } from "lucide-react";
import { useEffect, useRef } from "react";
import { useHaptics } from "../hooks/useHaptics";

const CONFETTI_COLORS = [
  "#38bdf8", // sky
  "#818cf8", // indigo
  "#34d399", // emerald
  "#fb923c", // orange
  "#f472b6", // pink
  "#fbbf24", // amber
  "#a78bfa", // violet
  "#60a5fa", // blue
];

const CONFETTI_SHAPES = ["circle", "square", "rect"];

function generateParticles(count) {
  return Array.from({ length: count }, (_, i) => ({
    id: i,
    x: Math.random() * 100,
    color: CONFETTI_COLORS[i % CONFETTI_COLORS.length],
    shape: CONFETTI_SHAPES[i % CONFETTI_SHAPES.length],
    size: 6 + Math.random() * 8,
    delay: Math.random() * 1.2,
    duration: 2.4 + Math.random() * 2,
    rotateEnd: 360 + Math.random() * 720,
    xDrift: (Math.random() - 0.5) * 60,
  }));
}

const PARTICLES = generateParticles(44);

export default function Celebration({ visible }) {
  const haptics = useHaptics();
  const firedRef = useRef(false);

  useEffect(() => {
    if (visible && !firedRef.current) {
      firedRef.current = true;
      haptics.celebrate();
    }
    if (!visible) {
      firedRef.current = false;
    }
  }, [visible]); // eslint-disable-line react-hooks/exhaustive-deps

  if (!visible) return null;

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        zIndex: 50,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        pointerEvents: "none",
        // subtle dark backdrop tint
        background: "rgba(5, 9, 26, 0.55)",
        backdropFilter: "blur(6px)",
        WebkitBackdropFilter: "blur(6px)",
      }}
    >
      {/* ── Confetti layer ── */}
      <div
        style={{
          position: "absolute",
          inset: 0,
          overflow: "hidden",
          pointerEvents: "none",
        }}
      >
        {PARTICLES.map((p) => {
          const isCircle = p.shape === "circle";
          const isRect = p.shape === "rect";
          return (
            <div
              key={p.id}
              style={{
                position: "absolute",
                top: "-20px",
                left: `${p.x}%`,
                width: isRect ? p.size * 0.5 : p.size,
                height: isRect ? p.size * 1.8 : p.size,
                borderRadius: isCircle ? "50%" : isRect ? "2px" : "3px",
                background: p.color,
                boxShadow: `0 0 6px ${p.color}88`,
                opacity: 0,
                animation: `confetti-fall ${p.duration}s ease-in ${p.delay}s forwards`,
                "--dur": `${p.duration}s`,
                "--delay": `${p.delay}s`,
                // inline keyframe via custom property isn't possible;
                // we rely on .animate-confetti from App.css
              }}
              className="animate-confetti"
            />
          );
        })}
      </div>

      {/* ── Modal card ── */}
      <div
        className="animate-pop-in"
        style={{
          position: "relative",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          gap: "16px",
          padding: "36px 44px",
          borderRadius: "28px",
          background: "rgba(11, 17, 32, 0.88)",
          border: "1px solid rgba(255,255,255,0.1)",
          backdropFilter: "blur(24px) saturate(180%)",
          WebkitBackdropFilter: "blur(24px) saturate(180%)",
          boxShadow:
            "0 32px 80px rgba(0,0,0,0.7), 0 0 0 1px rgba(56,189,248,0.12), inset 0 1px 0 rgba(255,255,255,0.07)",
          pointerEvents: "auto",
          maxWidth: "300px",
          textAlign: "center",
        }}
      >
        {/* Ambient glow behind trophy */}
        <div
          style={{
            position: "absolute",
            top: "10px",
            left: "50%",
            transform: "translateX(-50%)",
            width: "120px",
            height: "120px",
            borderRadius: "50%",
            background:
              "radial-gradient(circle, rgba(251,191,36,0.28) 0%, transparent 70%)",
            filter: "blur(24px)",
            pointerEvents: "none",
          }}
        />

        {/* Trophy icon with gradient wrapper */}
        <div
          style={{
            position: "relative",
            width: "72px",
            height: "72px",
            borderRadius: "22px",
            background: "linear-gradient(135deg, #fbbf24, #fb923c)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            boxShadow:
              "0 8px 32px rgba(251,191,36,0.45), inset 0 1px 0 rgba(255,255,255,0.2)",
            flexShrink: 0,
          }}
        >
          <Trophy size={36} color="#fff" strokeWidth={2} />
        </div>

        {/* Headline */}
        <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "8px",
            }}
          >
            <Sparkles
              size={14}
              style={{ color: "#fbbf24", flexShrink: 0 }}
              strokeWidth={2}
            />
            <h2
              style={{
                fontSize: "1.5rem",
                fontWeight: 800,
                letterSpacing: "-0.03em",
                background: "linear-gradient(90deg, #f1f5f9, #38bdf8)",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
                backgroundClip: "text",
                lineHeight: 1.1,
                margin: 0,
              }}
            >
              Goal Reached!
            </h2>
            <Sparkles
              size={14}
              style={{ color: "#fbbf24", flexShrink: 0 }}
              strokeWidth={2}
            />
          </div>

          <p
            style={{
              fontSize: "0.88rem",
              fontWeight: 400,
              color: "rgba(148,163,184,0.85)",
              lineHeight: 1.5,
              margin: 0,
            }}
          >
            You crushed your{" "}
            <span style={{ color: "#38bdf8", fontWeight: 600 }}>4 L</span>{" "}
            target today.
            <br />
            Your body thanks you! 💪
          </p>
        </div>

        {/* Divider */}
        <div
          style={{
            width: "60%",
            height: "1px",
            background:
              "linear-gradient(90deg, transparent, rgba(56,189,248,0.3), transparent)",
          }}
        />

        {/* Footer micro-text */}
        <p
          style={{
            fontSize: "0.7rem",
            fontWeight: 500,
            letterSpacing: "0.06em",
            textTransform: "uppercase",
            color: "rgba(100,116,139,0.7)",
            margin: 0,
          }}
        >
          Keep the streak alive ✦
        </p>
      </div>
    </div>
  );
}
