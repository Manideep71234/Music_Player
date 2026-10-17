package com.example.demo.data.repository

import com.example.demo.data.model.LrcLine
import java.io.File

/**
 * Parser for synced .lrc lyric files.
 */
object LrcParser {

    fun parseLrcContent(content: String): List<LrcLine> {
        val lines = mutableListOf<LrcLine>()
        val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

        content.lines().forEach { line ->
            val match = regex.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val msStr = match.groupValues[3]
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val timestamp = min * 60000L + sec * 1000L + ms
                val text = match.groupValues[4].trim()

                if (text.isNotEmpty()) {
                    lines.add(LrcLine(timestampMs = timestamp, text = text))
                }
            }
        }

        return lines.sortedBy { it.timestampMs }
    }

    fun loadLrcForSong(songTitle: String, songArtist: String, songPath: String?): List<LrcLine> {
        if (!songPath.isNullOrEmpty()) {
            val lrcFile = File(songPath.substringBeforeLast(".") + ".lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                try {
                    val text = lrcFile.readText()
                    val parsed = parseLrcContent(text)
                    if (parsed.isNotEmpty()) return parsed
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Generate clean synced demo lyrics as fallback
        return listOf(
            LrcLine(0L, "🎵 $songTitle"),
            LrcLine(3000L, "Artist: $songArtist"),
            LrcLine(8000L, "Listening to offline high quality audio..."),
            LrcLine(14000L, "Enjoy the liquid glass player experience"),
            LrcLine(20000L, "Tap any line to jump to that moment in the track"),
            LrcLine(28000L, "Auto-scrolling synchronized lyrics"),
            LrcLine(36000L, "Offline music with Room database & Media3"),
            LrcLine(45000L, "🎵 $songTitle - $songArtist")
        )
    }
}
