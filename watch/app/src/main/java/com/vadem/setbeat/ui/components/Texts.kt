package com.vadem.setbeat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.vadem.setbeat.R
import com.vadem.setbeat.sensor.HeartRateReading
import com.vadem.setbeat.ui.theme.WorkoutColors

@Composable
fun CenteredText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        style = style,
        color = color,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun HeartRateLine(reading: HeartRateReading) {
    if (reading.unavailable) {
        CenteredText(stringResource(R.string.unsupported), MaterialTheme.typography.labelSmall)
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_heart),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = WorkoutColors.heartRate,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.bpm_value, reading.bpm?.toString() ?: "--"),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
