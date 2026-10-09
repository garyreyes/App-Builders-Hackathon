package ph.appbuilders.offlinehealth.app.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Dev-only: the theme laid out like Tokens.dc.html, so the two can be compared side by side on a phone.
// Every label is a token name, not user copy. MainActivity shows this until the real screens are wired.

private val Swatches = listOf(
    "Paper" to Palette.Paper, "Surface" to Palette.Surface, "SurfaceStrong" to Palette.SurfaceStrong,
    "Divider" to Palette.Divider, "Outline" to Palette.Outline, "Ink" to Palette.Ink,
    "InkMuted" to Palette.InkMuted, "InkSubtle" to Palette.InkSubtle, "Danger" to Palette.Danger,
    "DangerInk" to Palette.DangerInk, "DangerSurface" to Palette.DangerSurface, "BrandMark" to Palette.BrandMark,
)

private val Spacings = listOf(
    "xxs" to Space.xxs, "xs" to Space.xs, "sm" to Space.sm, "md" to Space.md,
    "gutter" to Space.gutter, "lg" to Space.lg, "xl" to Space.xl, "xxl" to Space.xxl,
)

@Composable
fun ThemeSpecimen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.gutter, vertical = Space.lg),
        verticalArrangement = Arrangement.spacedBy(Space.xl),
    ) {
        Section("Type · Atkinson Hyperlegible Next") { TypeRows() }
        Section("Color") { Swatches.forEach { (name, color) -> SwatchRow(name, color) } }
        Section("Spacing (dp)") { Spacings.forEach { (name, value) -> SpacingRow(name, value) } }
        Section("Corner radius (dp)") { RadiusRows() }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        content()
    }
}

@Composable
private fun TypeRows() {
    val t = MaterialTheme.typography
    val muted = Palette.InkMuted
    TypeRow("headlineMedium · 28/36 Bold", t.headlineMedium)
    TypeRow("headlineSmall · 24/30 ExtraBold", t.headlineSmall, Palette.Danger)
    TypeRow("titleLarge · 22/28 Bold", t.titleLarge)
    TypeRow("titleMedium · 18/24 Bold", t.titleMedium)
    TypeRow("bodyLarge · 18/26 Regular · ñ é ü", t.bodyLarge)
    TypeRow("bodyMedium · 16/24 Regular", t.bodyMedium)
    TypeRow("labelLarge · 16/20 Bold", t.labelLarge)
    TypeRow("labelMedium · 14/20 SemiBold", t.labelMedium, muted)
}

@Composable
private fun TypeRow(label: String, style: TextStyle, color: Color = Palette.Ink) {
    Text(label, style = style, color = color)
}

@Composable
private fun SwatchRow(name: String, color: Color) {
    val hex = "#%06X".format(color.toArgb() and 0xFFFFFF)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
        Box(
            Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(10.dp))
                .border(1.dp, Palette.Ink.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
        )
        Text(name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        Text(hex, style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
    }
}

@Composable
private fun SpacingRow(name: String, value: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
        Text(name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(64.dp))
        Box(Modifier.size(width = value, height = 14.dp).background(Palette.Ink, RoundedCornerShape(2.dp)))
        Text("${value.value.toInt()}", style = MaterialTheme.typography.labelMedium, color = Palette.InkMuted)
    }
}

@Composable
private fun RadiusRows() {
    val shapes = MaterialTheme.shapes
    val sheetTop = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    listOf("sm · 8" to shapes.small, "md · 12" to shapes.medium, "lg · 16" to shapes.large,
        "xl · 24 (sheet top)" to sheetTop, "full" to CircleShape)
        .forEach { (name, shape) -> RadiusRow(name, shape) }
}

@Composable
private fun RadiusRow(name: String, shape: Shape) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
        Box(
            Modifier
                .size(width = 56.dp, height = 40.dp)
                .background(Palette.Surface, shape)
                .border(1.5.dp, Palette.Outline, shape),
        )
        Text(name, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(name = "360 × 800", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(name = "320 dp · font 1.5×", widthDp = 320, heightDp = 800, fontScale = 1.5f, showBackground = true)
@Composable
private fun ThemeSpecimenPreview() {
    AppTheme { ThemeSpecimen() }
}
