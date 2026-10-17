package com.example.demo.data.repository

import java.lang.Character.UnicodeBlock

/**
 * Guesses song language based on Unicode character script in the title.
 */
object LanguageGuesser {

    fun guessLanguageFromTitle(title: String): String {
        for (ch in title) {
            val block = UnicodeBlock.of(ch)
            if (block == UnicodeBlock.DEVANAGARI) return "Hindi"
            if (block == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == UnicodeBlock.HIRAGANA ||
                block == UnicodeBlock.KATAKANA ||
                block == UnicodeBlock.HANGUL_SYLLABLES
            ) return "Asian"
            if (block == UnicodeBlock.ARABIC) return "Arabic"
            if (block == UnicodeBlock.CYRILLIC) return "Cyrillic"
        }
        return "English"
    }
}
