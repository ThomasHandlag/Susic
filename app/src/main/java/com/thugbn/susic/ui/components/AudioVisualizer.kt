package com.thugbn.susic.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

/**
 * Audio visualizer with multiple visualization forms
 */
@Composable
fun AudioVisualizer(
    waveform: ByteArray,
    fft: ByteArray,
    visualizerType: VisualizerType,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    accentColor: Color = Color.Cyan
) {
    when (visualizerType) {
        VisualizerType.BAR -> BarVisualizer(fft, modifier, color)
        VisualizerType.LINE -> LineVisualizer(waveform, modifier, color)
        VisualizerType.WAVE -> WaveVisualizer(waveform, modifier, color, accentColor)
        VisualizerType.PARTICLE -> ParticleVisualizer(fft, modifier, color, accentColor)
        VisualizerType.CIRCLE -> CircleVisualizer(fft, modifier, color, accentColor)
    }
}

/**
 * Bar visualizer (frequency bars)
 */
@Composable
private fun BarVisualizer(
    fft: ByteArray,
    modifier: Modifier,
    color: Color
) {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(fft) {
        while (true) {
            withFrameMillis {
                phase += 0.05f
                if (phase > 2 * PI) phase = 0f
            }
        }
    }
    Canvas(modifier = modifier) {
        if (fft.isEmpty()) return@Canvas

        val barCount = minOf(64, fft.size / 2)
        val barWidth = size.width / barCount
        val barSpacing = barWidth * 0.2f
        val actualBarWidth = barWidth - barSpacing

        for (i in 0 until barCount) {
            val magnitude = if (i * 2 < fft.size) {
                val rfk = fft[i * 2].toInt()
                val ifk = if (i * 2 + 1 < fft.size) fft[i * 2 + 1].toInt() else 0
                sqrt((rfk * rfk + ifk * ifk).toFloat())
            } else {
                0f
            }

            val barHeight = (magnitude / 128f) * size.height * 0.8f + phase
            val x = i * barWidth + barSpacing / 2
            val y = size.height - barHeight

            // Draw bar with gradient effect
            val alpha = (barHeight / size.height).coerceIn(0.3f, 1f)
            drawRect(
                color = color.copy(alpha = alpha),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(actualBarWidth, barHeight)
            )
        }
    }
}

/**
 * Line visualizer (waveform as lines)
 */
@Composable
private fun LineVisualizer(
    waveform: ByteArray,
    modifier: Modifier,
    color: Color
) {

    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(waveform) {
        while (true) {
            withFrameMillis {
                phase += 0.05f
                if (phase > 2 * PI) phase = 0f
            }
        }
    }
    Canvas(modifier = modifier) {
        if (waveform.isEmpty()) return@Canvas

        val path = Path()
        val points = minOf(128, waveform.size)
        val step = size.width / points
        val centerY = size.height / 2

        path.moveTo(0f, centerY)

        for (i in 0 until points) {
            val amplitude = (waveform[i].toInt() + 128) / 256f
            val x = i * step
            val y = centerY + (amplitude - 0.5f) * size.height + phase

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

/**
 * Wave visualizer (smooth sine wave effect)
 */
@Composable
private fun WaveVisualizer(
    waveform: ByteArray,
    modifier: Modifier,
    color: Color,
    accentColor: Color
) {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(waveform) {
        while (true) {
            withFrameMillis {
                phase += 0.05f
                if (phase > 2 * PI) phase = 0f
            }
        }
    }

    Canvas(modifier = modifier) {
        if (waveform.isEmpty()) return@Canvas

        val centerY = size.height / 2
        val waves = 3

        for (wave in 0 until waves) {
            val path = Path()
            val points = 200
            val amplitude = if (waveform.isNotEmpty()) {
                (waveform[wave % waveform.size].toInt() + 128) / 256f
            } else {
                0.3f
            }

            for (i in 0..points) {
                val x = (i.toFloat() / points) * size.width
                val offset = wave * 0.5f
                val y = centerY + sin((x / size.width) * 4 * PI + phase + offset).toFloat() *
                        amplitude * size.height * 0.3f

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            val waveColor = if (wave == 0) color else accentColor
            val alpha = 1f - (wave * 0.3f)

            drawPath(
                path = path,
                color = waveColor.copy(alpha = alpha),
                style = Stroke(width = (3 - wave).dp.toPx())
            )
        }
    }
}

/**
 * Particle visualizer (particles react to frequency)
 */
@Composable
private fun ParticleVisualizer(
    fft: ByteArray,
    modifier: Modifier,
    color: Color,
    accentColor: Color
) {
    val particles = remember {
        List(50) {
            Particle(
                x = Random.nextFloat(), // 0.0 to 1.0
                y = Random.nextFloat(), // 0.0 to 1.0
                speedX = (Random.nextFloat() - 0.5f) * 0.002f, // Small initial speed
                speedY = (Random.nextFloat() - 0.5f) * 0.002f, // Small initial speed
                size = Random.nextFloat() * 3f + 2f
            )
        }.toMutableStateList()
    }

    LaunchedEffect(fft) {
        while (true) {
            withFrameMillis {
                particles.forEachIndexed { index, particle ->
                    // 1. Calculate magnitude from FFT (same as before)
                    val magnitude = if (fft.isNotEmpty()) {
                        val fftIndex = (index * fft.size / particles.size).coerceIn(0, fft.size - 1)
                        abs(fft[fftIndex].toInt()) / 128f
                    } else {
                        0.1f // Default magnitude when FFT is empty
                    }

                    // 2. Calculate speed multiplier (same as before)
                    // This correctly increases speed based on FFT
                    val speedMultiplier = (1 + magnitude * 3) // Added *3 to make effect more obvious

                    // 3. Calculate new positions
                    var newX = particle.x + particle.speedX * speedMultiplier
                    var newY = particle.y + particle.speedY * speedMultiplier

                    // --- MODIFICATION START: Implement Bouncing ---
                    var newSpeedX = particle.speedX
                    var newSpeedY = particle.speedY

                    // Check horizontal bounds
                    if (newX > 1f) {
                        newX = 1f         // Clamp position to the edge
                        newSpeedX *= -1f  // Reverse horizontal direction
                    } else if (newX < 0f) {
                        newX = 0f         // Clamp position to the edge
                        newSpeedX *= -1f  // Reverse horizontal direction
                    }

                    // Check vertical bounds
                    if (newY > 1f) {
                        newY = 1f         // Clamp position to the edge
                        newSpeedY *= -1f  // Reverse vertical direction
                    } else if (newY < 0f) {
                        newY = 0f         // Clamp position to the edge
                        newSpeedY *= -1f  // Reverse vertical direction
                    }
                    // --- MODIFICATION END ---

                    // 4. Update the particle in the list
                    particles[index] = particle.copy(
                        x = newX,
                        y = newY,
                        speedX = newSpeedX, // Store the (potentially) reversed speed
                        speedY = newSpeedY, // Store the (potentially) reversed speed
                        currentMagnitude = magnitude
                    )
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            // Scale normalized coordinates (0.0-1.0) to canvas size
            val x = particle.x * size.width
            val y = particle.y * size.height
            val radius = particle.size * (1 + particle.currentMagnitude * 2)

            drawCircle(
                color = if (particle.currentMagnitude > 0.5f) accentColor else color,
                radius = radius,
                center = Offset(x, y),
                alpha = 0.7f + particle.currentMagnitude * 0.3f
            )
        }
    }
}

/**
 * Circle visualizer (circular frequency bars)
 */
@Composable
private fun CircleVisualizer(
    fft: ByteArray,
    modifier: Modifier,
    color: Color,
    accentColor: Color
) {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(fft) {
        while (true) {
            withFrameMillis {
                phase += 0.05f
                if (phase > 2 * PI) phase = 0f
            }
        }
    }
    Canvas(modifier = modifier) {
        if (fft.isEmpty()) return@Canvas

        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadius = minOf(size.width, size.height) * 0.25f
        val barCount = minOf(64, fft.size / 2)
        val angleStep = (2 * PI / barCount).toFloat()

        for (i in 0 until barCount) {
            val magnitude = if (i * 2 < fft.size) {
                val rfk = fft[i * 2].toInt()
                val ifk = if (i * 2 + 1 < fft.size) fft[i * 2 + 1].toInt() else 0
                sqrt((rfk * rfk + ifk * ifk).toFloat())
            } else {
                0f
            }

            val barLength = (magnitude / 128f) * baseRadius + phase
            val angle = i * angleStep

            val startX = centerX + cos(angle) * baseRadius
            val startY = centerY + sin(angle) * baseRadius
            val endX = centerX + cos(angle) * (baseRadius + barLength)
            val endY = centerY + sin(angle) * (baseRadius + barLength)

            val barColor = if (magnitude > 64) accentColor else color

            drawLine(
                color = barColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                alpha = (magnitude / 128f).coerceAtLeast(0.3f)
            )
        }

        // Draw center circle
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = baseRadius * 0.8f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * Visualizer types
 */
enum class VisualizerType {
    BAR,
    LINE,
    WAVE,
    PARTICLE,
    CIRCLE
}

/**
 * Particle data class
 */
private data class Particle(
    val x: Float,
    val y: Float,
    val speedX: Float,
    val speedY: Float,
    val size: Float,
    val currentMagnitude: Float = 0f
)

