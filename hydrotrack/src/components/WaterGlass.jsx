export default function WaterGlass({ percentage }) {
  return (
    <div className="flex flex-col items-center">
      <div className="relative w-40 h-64 sm:w-48 sm:h-72 border-4 border-sky-300 rounded-b-3xl rounded-t-lg bg-white/10 overflow-hidden shadow-lg">
        {/* Water fill */}
        <div
          className="absolute bottom-0 left-0 right-0 transition-all duration-700 ease-out"
          style={{ height: `${percentage}%` }}
        >
          {/* Wave animation */}
          <div className="absolute top-0 left-0 right-0 h-4 -translate-y-2">
            <svg
              viewBox="0 0 200 20"
              preserveAspectRatio="none"
              className="w-full h-full"
            >
              <path
                d="M0,10 C30,4 70,16 100,10 C130,4 170,16 200,10 L200,20 L0,20 Z"
                className="fill-sky-400/90"
              >
                <animate
                  attributeName="d"
                  dur="3s"
                  repeatCount="indefinite"
                  values="
                    M0,10 C30,4 70,16 100,10 C130,4 170,16 200,10 L200,20 L0,20 Z;
                    M0,10 C30,16 70,4 100,10 C130,16 170,4 200,10 L200,20 L0,20 Z;
                    M0,10 C30,4 70,16 100,10 C130,4 170,16 200,10 L200,20 L0,20 Z
                  "
                />
              </path>
            </svg>
          </div>
          <div className="w-full h-full bg-gradient-to-t from-sky-500/90 to-sky-400/80" />
        </div>

        {/* Percentage overlay */}
        <div className="absolute inset-0 flex items-center justify-center">
          <span
            className={`text-3xl sm:text-4xl font-bold drop-shadow-md ${
              percentage > 50 ? 'text-white' : 'text-sky-600'
            }`}
          >
            {Math.round(percentage)}%
          </span>
        </div>

        {/* Glass shine effect */}
        <div className="absolute top-0 left-2 w-3 h-full bg-white/10 rounded-full" />
      </div>
    </div>
  );
}
