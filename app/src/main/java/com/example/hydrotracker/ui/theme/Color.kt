package com.example.hydrotracker.ui.theme

import androidx.compose.ui.graphics.Color

// ── Deep background hierarchy ─────────────────────────────────────────────────
val Abyss = Color(0xFF060810)   // Deepest screen background
val AbyssMid = Color(0xFF0A1020)   // Mid-layer surface
val AbyssHigh = Color(0xFF101828)   // Card surface
val AbyssElevated = Color(0xFF16202E)   // Elevated / dialog surface

// ── Ice Blue — primary ────────────────────────────────────────────────────────
val IceBlue400 = Color(0xFF38BDF8)   // Main primary
val IceBlue300 = Color(0xFF7DD3FC)   // Lighter highlight
val IceBlue500 = Color(0xFF0EA5E9)   // Deeper active
val IceBlue600 = Color(0xFF0284C7)   // Pressed state

// ── Violet — secondary accent ─────────────────────────────────────────────────
val Violet400 = Color(0xFF818CF8)   // Soft violet accent
val Violet500 = Color(0xFF6366F1)   // Deeper violet
val Violet300 = Color(0xFFA5B4FC)   // Light violet tint

// ── Crystal Green — success / goal ───────────────────────────────────────────
val Crystal400 = Color(0xFF34D399)   // Goal met / success
val Crystal500 = Color(0xFF10B981)   // Deeper green
val Crystal300 = Color(0xFF6EE7B7)   // Light success tint

// ── Amber — streak / warning ──────────────────────────────────────────────────
val Amber400 = Color(0xFFFBBF24)
val Amber500 = Color(0xFFF59E0B)

// ── Error ─────────────────────────────────────────────────────────────────────
val Rose400 = Color(0xFFF87171)
val Rose500 = Color(0xFFEF4444)

// ── Neutral slate — text / borders ───────────────────────────────────────────
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFEFF4FA)
val Slate200 = Color(0xFFCDD8E8)
val Slate300 = Color(0xFF94A8C0)
val Slate400 = Color(0xFF607A96)
val Slate500 = Color(0xFF3D566E)
val Slate600 = Color(0xFF253547)
val Slate700 = Color(0xFF182436)
val Slate800 = Color(0xFF101825)
val Slate900 = Color(0xFF070D18)

// ── Glass surface tones ───────────────────────────────────────────────────────
val GlassDark = Color(0xFF0E1929)   // Dark glass panel
val GlassBorder = Color(0x12FFFFFF)   // 7 % white border
val GlassBorderBright = Color(0x1EFFFFFF) // 12 % white — active border

// ── Water rendering colours ───────────────────────────────────────────────────
val WaterPrimary = Color(0xFF0EA5E9)
val WaterPrimaryLight = Color(0xFF38BDF8)
val WaterPrimaryDeep = Color(0xFF0369A1)
val WaterHighlight = Color(0xFF7DD3FC)
val WaterTransparent = Color(0x550EA5E9)

// Legacy aliases kept for files not yet migrated
val Blue400 = IceBlue400
val Blue500 = IceBlue500
val Blue600 = IceBlue600
val Blue300 = IceBlue300
val Blue200 = Color(0xFFBAE6FD)
val Blue700 = Color(0xFF0369A1)
val Blue100 = Color(0xFFE0F2FE)
val Blue50 = Color(0xFFF0F9FF)
val Cyan400 = Crystal400
val Cyan300 = Crystal300
val Cyan500 = Crystal500
val Indigo400 = Violet400
val Indigo500 = Violet500
val Indigo600 = Color(0xFF4F46E5)
val Green400 = Crystal400
val Green500 = Crystal500
val Green600 = Color(0xFF059669)
val Red400 = Rose400
val Red500 = Rose500
val Orange400 = Color(0xFFFB923C)
val Orange500 = Color(0xFFF97316)
val Violet600 = Color(0xFF7C3AED)

val DeepNavy = Abyss
val DeepNavy800 = AbyssMid
val DeepNavy700 = AbyssHigh

val WaterBlue = WaterPrimary
val WaterBlueDark = WaterPrimaryDeep
val WaterBlueLight = WaterHighlight
val WaterBlueTransparent = WaterTransparent

// ── Light Design Palette ──────────────────────────────────────────────────────
val LightBackground = Color(0xFFF0F4F8)        // Page background (light blue-gray)
val HydroBlue = Color(0xFF2563EB)              // Primary blue — buttons, active states
val HydroBlueDark = Color(0xFF1D4ED8)          // Darker blue for pressed states
val HydroBlueContainer = Color(0xFFEBF5FF)     // Soft blue container / icon backgrounds
val HydroTextPrimary = Color(0xFF1E293B)       // Primary dark text
val HydroTextSecondary = Color(0xFF64748B)     // Secondary gray text
val HydroSuccess = Color(0xFF10B981)           // Success green
val HydroSuccessContainer = Color(0xFFD1FAE5)  // Light green container
val HydroWarning = Color(0xFFF59E0B)           // Warning amber
val HydroWarningContainer = Color(0xFFFEF3C7)  // Light amber container
val HydroDivider = Color(0xFFE2E8F0)           // Card borders / dividers
val HydroCardBg = Color(0xFFFFFFFF)            // White card background
