package com.example.wikiapp.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
// WIKIDATA BRAND COLORS
// Based on Wikimedia Design Style Guide & Wikidata visual identity
// ═══════════════════════════════════════════════════════════════════════════════

// Primary Wikidata Colors
val WikidataTeal = Color(0xFF006699)        // Main Wikidata brand color
val WikidataTealLight = Color(0xFF3399CC)   // Hover/accent
val WikidataTealDark = Color(0xFF004466)    // Pressed states

// Wikidata Entity Type Colors
val WikidataItemGreen = Color(0xFF339966)   // Items (Q-entities)
val WikidataPropertyOrange = Color(0xFFDD6611) // Properties (P-entities)
val WikidataLexemePurple = Color(0xFF996699)   // Lexemes (L-entities)

// Wikimedia Foundation Base Colors
val Base100 = Color(0xFFFFFFFF)  // Pure white
val Base90 = Color(0xFFF8F9FA)   // Page background
val Base80 = Color(0xFFEAECF0)   // Subtle borders
val Base70 = Color(0xFFC8CCD1)   // Borders
val Base50 = Color(0xFFA2A9B1)   // Placeholder text
val Base30 = Color(0xFF72777D)   // Secondary text
val Base20 = Color(0xFF54595D)   // Body text
val Base10 = Color(0xFF202122)   // Headlines

// Accent Colors
val AccentBlue = Color(0xFF3366CC)      // Links
val AccentBlueDark = Color(0xFF2A4B8D)  // Visited links
val AccentGreen = Color(0xFF00AF89)     // Success
val AccentRed = Color(0xFFDD3333)       // Error/Delete
val AccentYellow = Color(0xFFFFCC33)    // Warning

// ═══════════════════════════════════════════════════════════════════════════════
// LIGHT THEME - Matches Wikidata web interface
// ═══════════════════════════════════════════════════════════════════════════════

val LightPrimary = WikidataTeal
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFE6F3FA)
val LightOnPrimaryContainer = WikidataTealDark

val LightSecondary = WikidataItemGreen
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFE6F4ED)
val LightOnSecondaryContainer = Color(0xFF1A4D33)

val LightTertiary = WikidataPropertyOrange
val LightOnTertiary = Color.White
val LightTertiaryContainer = Color(0xFFFFF3E6)
val LightOnTertiaryContainer = Color(0xFF663300)

val LightBackground = Base90
val LightOnBackground = Base10
val LightSurface = Base100
val LightOnSurface = Base10
val LightSurfaceVariant = Base80
val LightOnSurfaceVariant = Base20
val LightOutline = Base70
val LightOutlineVariant = Base80

val LightError = AccentRed
val LightOnError = Color.White
val LightErrorContainer = Color(0xFFFFE6E6)
val LightOnErrorContainer = Color(0xFF660000)

// ═══════════════════════════════════════════════════════════════════════════════
// DARK THEME - Wikidata dark mode inspired
// ═══════════════════════════════════════════════════════════════════════════════

val DarkPrimary = WikidataTealLight
val DarkOnPrimary = Color(0xFF003344)
val DarkPrimaryContainer = Color(0xFF004D66)
val DarkOnPrimaryContainer = Color(0xFFB3E0F2)

val DarkSecondary = Color(0xFF66CC99)
val DarkOnSecondary = Color(0xFF003322)
val DarkSecondaryContainer = Color(0xFF1A5233)
val DarkOnSecondaryContainer = Color(0xFFB3E6CC)

val DarkTertiary = Color(0xFFFFAA66)
val DarkOnTertiary = Color(0xFF442200)
val DarkTertiaryContainer = Color(0xFF663D00)
val DarkOnTertiaryContainer = Color(0xFFFFDDBB)

val DarkBackground = Color(0xFF101418)
val DarkOnBackground = Color(0xFFE3E3E7)
val DarkSurface = Color(0xFF1A1E22)
val DarkOnSurface = Color(0xFFE3E3E7)
val DarkSurfaceVariant = Color(0xFF2D3238)
val DarkOnSurfaceVariant = Color(0xFFC4C7CF)
val DarkOutline = Color(0xFF8E9199)
val DarkOutlineVariant = Color(0xFF44474E)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// ═══════════════════════════════════════════════════════════════════════════════
// SEMANTIC COLORS - For specific UI elements
// ═══════════════════════════════════════════════════════════════════════════════

// Entity type badge colors
val ItemBadgeBackground = Color(0xFFE6F4ED)
val ItemBadgeText = WikidataItemGreen
val PropertyBadgeBackground = Color(0xFFFFF3E6)
val PropertyBadgeText = WikidataPropertyOrange
val LexemeBadgeBackground = Color(0xFFF5E6F5)
val LexemeBadgeText = WikidataLexemePurple

// Statement ranks
val PreferredRankColor = Color(0xFF00AF89)
val NormalRankColor = Base50
val DeprecatedRankColor = AccentRed

// Link colors
val LinkColor = AccentBlue
val VisitedLinkColor = AccentBlueDark
val ExternalLinkColor = Color(0xFF6B4BA1)
