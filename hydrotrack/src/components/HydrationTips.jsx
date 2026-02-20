import { Lightbulb, ChevronRight } from "lucide-react";
import { useState, useEffect, useRef } from "react";
import { useHaptics } from "../hooks/useHaptics";

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
  "Creatine loading phases require even more water — up to 5L per day.",
];

export default function HydrationTips() {
  const [tipIndex, setTipIndex] = useState(0);
  const [animating, setAnimating] = useState(false);
  const [isHovered, setIsHovered] = useState(false);
  const haptics = useHaptics();
  const timerRef = useRef(null);

  useEffect(() => {
    setTipIndex(Math.floor(Math.random() * TIPS.length));
  }, []);

  // Auto-rotate tips every 12 s
  useEffect(() => {
    timerRef.current = setInterval(() => {
      advanceTip();
    }, 12000);
    return () => clearInterval(timerRef.current);
  }, [tipIndex]); // eslint-disable-line react-hooks/exhaustive-deps

  const advanceTip = () => {
    if (animating) return;
    setAnimating(true);
    setTimeout(() => {
      setTipIndex((prev) => (prev + 1) % TIPS.length);
      setAnimating(false);
    }, 260);
  };

  const handleTap = () => {
    haptics.soft();
    clearInterval(timerRef.current);
    advanceTip();
  };

  return (
    <button
      onClick={handleTap}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={{
        width: "100%",
        maxWidth: "340px",
        borderRadius: "20px",
        padding: "18px 18px 16px",
        background: isHovered
          ? "rgba(255,255,255,0.07)"
          : "rgba(255,255,255,0.04)",
        border: isHovered
          ? "1px solid rgba(251,191,36,0.3)"
          : "1px solid rgba(255,255,255,0.08)",
        backdropFilter: "blur(16px)",
        WebkitBackdropFilter: "blur(16px)",
        boxShadow: isHovered
          ? "0 8px 32px rgba(0,0,0,0.3), 0 0 0 1px rgba(251,191,36,0.08)"
          : "0 4px 24px rgba(0,0,0,0.2)",
        cursor: "pointer",
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        textAlign: "left",
        position: "relative",
        overflow: "hidden",
        transition:
          "background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease",
        WebkitTapHighlightColor: "transparent",
        outline: "none",
        fontFamily: "inherit",
      }}
    >
      {/* Top shine */}
      <div
        style={{
          position: "absolute",
          top: 0,
          left: 0,
          right: 0,
          height: "40%",
          background:
            "linear-gradient(180deg, rgba(255,255,255,0.055) 0%, transparent 100%)",
          borderRadius: "20px 20px 0 0",
          pointerEvents: "none",
        }}
      />

      {/* Ambient glow from bulb */}
      <div
        style={{
          position: "absolute",
          top: "-20px",
          left: "-20px",
          width: "100px",
          height: "100px",
          borderRadius: "50%",
          background:
            "radial-gradient(circle, rgba(251,191,36,0.18) 0%, transparent 70%)",
          filter: "blur(20px)",
          pointerEvents: "none",
          transition: "opacity 0.2s ease",
          opacity: isHovered ? 1 : 0.6,
        }}
      />

      {/* Header row */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          {/* Icon container */}
          <div
            style={{
              width: "30px",
              height: "30px",
              borderRadius: "10px",
              background: "linear-gradient(135deg, #fbbf24, #fb923c)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              boxShadow: "0 4px 12px rgba(251,191,36,0.35)",
              flexShrink: 0,
            }}
          >
            <Lightbulb size={15} color="#fff" strokeWidth={2.2} />
          </div>

          {/* Label */}
          <span
            style={{
              fontSize: "0.65rem",
              fontWeight: 700,
              letterSpacing: "0.12em",
              textTransform: "uppercase",
              background: "linear-gradient(90deg, #fbbf24, #fb923c)",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
              backgroundClip: "text",
              lineHeight: 1,
            }}
          >
            Hydration Tip
          </span>
        </div>

        {/* Tap indicator */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "3px",
            opacity: 0.45,
            transition: "opacity 0.2s ease",
          }}
        >
          <span
            style={{
              fontSize: "0.6rem",
              fontWeight: 500,
              letterSpacing: "0.06em",
              textTransform: "uppercase",
              color: "rgba(148,163,184,0.8)",
            }}
          >
            tap to rotate
          </span>
          <ChevronRight size={11} style={{ color: "rgba(148,163,184,0.7)" }} />
        </div>
      </div>

      {/* Divider */}
      <div
        style={{
          width: "100%",
          height: "1px",
          background:
            "linear-gradient(90deg, rgba(251,191,36,0.25), rgba(255,255,255,0.06), transparent)",
        }}
      />

      {/* Tip text */}
      <div style={{ minHeight: "52px" }}>
        <p
          style={{
            fontSize: "0.85rem",
            fontWeight: 400,
            color: "rgba(203,213,225,0.88)",
            lineHeight: 1.65,
            margin: 0,
            opacity: animating ? 0 : 1,
            transform: animating ? "translateY(6px)" : "translateY(0)",
            transition: "opacity 0.26s ease, transform 0.26s ease",
          }}
        >
          {TIPS[tipIndex]}
        </p>
      </div>

      {/* Progress dots */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "5px",
          justifyContent: "center",
          paddingTop: "2px",
        }}
      >
        {TIPS.map((_, i) => (
          <div
            key={i}
            style={{
              width: i === tipIndex ? "18px" : "5px",
              height: "5px",
              borderRadius: "999px",
              background:
                i === tipIndex
                  ? "linear-gradient(90deg, #fbbf24, #fb923c)"
                  : "rgba(255,255,255,0.15)",
              transition:
                "width 0.35s cubic-bezier(0.4,0,0.2,1), background 0.35s ease",
              boxShadow:
                i === tipIndex ? "0 0 8px rgba(251,191,36,0.5)" : "none",
            }}
          />
        ))}
      </div>
    </button>
  );
}
