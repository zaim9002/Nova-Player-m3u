package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Immersive Dark Palette
val DarkBg = Color(0xFF030712) // Deep space ultra dark
val DarkBgSecondary = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceVariant = Color(0xFF1F2937)
val DarkCardBorder = Color(0x336366F1) // Indigo subtle glass border
val DarkCardBorderFocused = Color(0xFF6366F1)

// Vibrant Neon Accents
val NeonIndigo = Color(0xFF6366F1)
val NeonViolet = Color(0xFF8B5CF6)
val NeonBlue = Color(0xFF00D2FF)
val NeonCyan = Color(0xFF06B6D4)
val NeonPurple = Color(0xFF7C3AED)
val NeonPink = Color(0xFFEC4899)
val NeonAccentGlow = Color(0x406366F1)

// Glassmorphism Surfaces
val GlassSurface = Color(0xE6111827)
val GlassCardBg = Color(0xCC131C2E)
val GlassHeaderBg = Color(0xCC080B12)

// Text Colors
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

// Status Colors
val ErrorRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981)
val WarningYellow = Color(0xFFF59E0B)

// Immersive UI Gradient Brushes
val ImmersiveBgBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0D111D),
        Color(0xFF05070D),
        Color(0xFF030509)
    )
)

val ImmersiveCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xEE1E293B),
        Color(0xDD0F172A)
    )
)

val PrimaryNeonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6366F1),
        Color(0xFF8B5CF6),
        Color(0xFF00D2FF)
    )
)

val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x806366F1),
        Color(0x3300D2FF),
        Color(0x108B5CF6)
    )
)

val LiveTagGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFEF4444),
        Color(0xFFDC2626)
    )
)
