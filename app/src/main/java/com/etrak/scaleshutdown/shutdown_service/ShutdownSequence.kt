package com.etrak.scaleshutdown.shutdown_service

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etrak.scaleshutdown.R
import com.etrak.scaleshutdown.ui.theme.ScaleShutdownTheme

@Composable
fun ShutdownSequence(
    onCancelClick: () -> Unit,
    countdown: Int
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = countdown.toString(),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 72.sp,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.fillMaxWidth(0.5f),
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    }
}

@Preview
@Composable
fun MainContentPreview() {
    ScaleShutdownTheme(darkTheme = false) {
        ShutdownSequence(
            onCancelClick = { },
            countdown = 10
        )
    }
}