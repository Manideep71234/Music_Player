package com.example.demo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demo.data.model.LrcLine
import com.example.demo.ui.theme.ActivePillHighlight
import com.example.demo.ui.theme.TextPrimary
import com.example.demo.ui.theme.TextSecondary

/**
 * Synced auto-scrolling lyrics view with tap-to-seek support.
 */
@Composable
fun LyricsView(
    lyrics: List<LrcLine>,
    currentPosition: Long,
    onSeekToLine: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (lyrics.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No lyrics available", color = TextSecondary)
        }
        return
    }

    val activeIndex = lyrics.indexOfLast { it.timestampMs <= currentPosition }.coerceAtLeast(0)
    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex in lyrics.indices) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isActive = index == activeIndex

            Text(
                text = line.text,
                color = if (isActive) ActivePillHighlight else TextPrimary.copy(alpha = 0.4f),
                fontSize = if (isActive) 22.sp else 16.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeekToLine(line.timestampMs) }
                    .padding(vertical = 12.dp, horizontal = 20.dp)
            )
        }
    }
}
