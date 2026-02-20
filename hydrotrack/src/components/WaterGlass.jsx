export default function WaterGlass({ percentage }) {
  const clamp = Math.min(Math.max(percentage, 0), 100);

  // SVG ring maths
  const SIZE = 260;
  const CX = SIZE / 2;
  const CY = SIZE / 2;
  const R = 112;
  const CIRCUMFERENCE = 2 * Math.PI * R;
  const dashOffset = CIRCUMFERENCE * (1 - clamp / 100);

  // Dynamic glow intensity based on fill level
  const glowAlpha = (0.15 + (clamp / 100) * 0.45).toFixed(2);
  const glowSize = 40 + clamp * 0.5;

  return (
    <div
      className="animate-fade-up"
      style={{
        position: "relative",
        width: SIZE,
        height: SIZE,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        flexShrink: 0,
      }}
    >
      {/* ── Ambient glow backdrop ── */}
      <div
        style={{
          position: "absolute",
          inset: 0,
          borderRadius: "50%",
          background: `radial-gradient(circle at 50% 60%,
            rgba(56,189,248,${glowAlpha}) 0%,
            rgba(129,140,248,${(glowAlpha * 0.5).toFixed(2)}) 40%,
            transparent 70%)`,
          filter: `blur(${glowSize}px)`,
          pointerEvents: "none",
          transition: "all 0.8s ease",
        }}
      />

      {/* ── Pulse ring (shown when goal reached) ── */}
      {clamp >= 100 && (
        <div
          className="animate-pulse-ring"
          style={{
            position: "absolute",
            inset: "10px",
            borderRadius: "50%",
            border: "2px solid rgba(52,211,153,0.6)",
            pointerEvents: "none",
          }}
        />
      )}

      {/* ── SVG progress ring ── */}
      <svg
        width={SIZE}
        height={SIZE}
        viewBox={`0 0 ${SIZE} ${SIZE}`}
        style={{
          position: "absolute",
          top: 0,
          left: 0,
          transform: "rotate(-90deg)",
          overflow: "visible",
        }}
      >
        <defs>
          <linearGradient id="ringGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#38bdf8" />
            <stop offset="50%" stopColor="#818cf8" />
            <stop offset="100%" stopColor="#34d399" />
          </linearGradient>
          <filter id="ringGlow">
            <feGaussianBlur stdDeviation="3" result="blur" />
            <feMerge>
              <feMergeNode in="blur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        {/* Track */}
        <circle
          cx={CX}
          cy={CY}
          r={R}
          fill="none"
          stroke="rgba(255,255,255,0.06)"
          strokeWidth="9"
        />

        {/* Progress arc */}
        {clamp > 0 && (
          <circle
            cx={CX}
            cy={CY}
            r={R}
            fill="none"
            stroke="url(#ringGradient)"
            strokeWidth="9"
            strokeLinecap="round"
            strokeDasharray={CIRCUMFERENCE}
            strokeDashoffset={dashOffset}
            filter="url(#ringGlow)"
            style={{
              transition:
                "stroke-dashoffset 0.85s cubic-bezier(0.4, 0, 0.2, 1)",
            }}
          />
        )}

        {/* Tick marks at 25 / 50 / 75 % */}
        {[25, 50, 75].map((pct) => {
          const angle = (pct / 100) * 360 - 90;
          const rad = (angle * Math.PI) / 180;
          const x1 = CX + (R - 7) * Math.cos(rad);
          const y1 = CY + (R - 7) * Math.sin(rad);
          const x2 = CX + (R + 7) * Math.cos(rad);
          const y2 = CY + (R + 7) * Math.sin(rad);
          return (
            <line
              key={pct}
              x1={x1}
              y1={y1}
              x2={x2}
              y2={y2}
              stroke="rgba(255,255,255,0.12)"
              strokeWidth="1.5"
              strokeLinecap="round"
            />
          );
        })}
      </svg>

      {/* ── Inner water orb ── */}
      <div
        style={{
          position: "absolute",
          inset: "22px",
          borderRadius: "50%",
          overflow: "hidden",
          background: "rgba(5, 9, 26, 0.92)",
          border: "1px solid rgba(255,255,255,0.07)",
        }}
      >
        {/* Water fill */}
        <div
          style={{
            position: "absolute",
            bottom: 0,
            left: 0,
            right: 0,
            height: `${clamp}%`,
            transition: "height 0.85s cubic-bezier(0.4, 0, 0.2, 1)",
          }}
        >
          {/* Wave surface */}
          <div
            style={{
              position: "absolute",
              top: "-14px",
              left: 0,
              right: 0,
              height: "28px",
              overflow: "hidden",
            }}
          >
            <svg
              viewBox="0 0 400 28"
              preserveAspectRatio="none"
              style={{ width: "200%", height: "28px" }}
              className="animate-wave-shift"
            >
              <path
                d="M0,14 C50,5 100,23 150,14 C200,5 250,23 300,14 C350,5 400,23 450,14 L450,28 L0,28 Z"
                fill="rgba(56,189,248,0.55)"
              />
              <path
                d="M0,18 C60,10 110,26 160,18 C210,10 260,26 310,18 C360,10 400,24 450,18 L450,28 L0,28 Z"
                fill="rgba(56,189,248,0.35)"
              />
            </svg>
          </div>

          {/* Water body */}
          <div
            style={{
              position: "absolute",
              inset: 0,
              background:
                "linear-gradient(to top, rgba(14,165,233,0.75) 0%, rgba(56,189,248,0.55) 60%, rgba(129,140,248,0.4) 100%)",
            }}
          />

          {/* Shimmer highlight inside water */}
          <div
            style={{
              position: "absolute",
              top: 0,
              left: "15%",
              width: "18%",
              bottom: 0,
              background:
                "linear-gradient(to bottom, rgba(255,255,255,0.12), transparent)",
              borderRadius: "999px",
            }}
          />
        </div>

        {/* Percentage + label */}
        <div
          style={{
            position: "absolute",
            inset: 0,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            gap: "4px",
            pointerEvents: "none",
          }}
        >
          <span
            style={{
              fontSize: clamp >= 100 ? "2.4rem" : "2.8rem",
              fontWeight: 800,
              letterSpacing: "-0.04em",
              color: clamp > 52 ? "#f1f5f9" : "#38bdf8",
              textShadow:
                clamp > 52
                  ? "0 2px 12px rgba(0,0,0,0.5)"
                  : "0 0 20px rgba(56,189,248,0.6)",
              lineHeight: 1,
              transition: "color 0.4s ease, font-size 0.3s ease",
            }}
          >
            {Math.round(clamp)}%
          </span>
          <span
            style={{
              fontSize: "0.7rem",
              fontWeight: 500,
              letterSpacing: "0.12em",
              textTransform: "uppercase",
              color:
                clamp > 52 ? "rgba(241,245,249,0.55)" : "rgba(56,189,248,0.55)",
              transition: "color 0.4s ease",
            }}
          >
            {clamp >= 100 ? "🎉 goal hit" : "hydrated"}
          </span>
        </div>

        {/* Inner vignette edge */}
        <div
          style={{
            position: "absolute",
            inset: 0,
            borderRadius: "50%",
            boxShadow: "inset 0 0 40px rgba(0,0,0,0.45)",
            pointerEvents: "none",
          }}
        />
      </div>
    </div>
  );
}
