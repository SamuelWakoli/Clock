package com.samwrotethecode.clock.ui.presentation.app_composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CircularCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape,
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(22.dp)
                .background(
                    color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.6f
                    ),
                    shape = CircleShape
                )
                .clickable { onCheckedChange(!checked) },
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun CircularCheckboxCheckedPreview() {
    CircularCheckbox(
        checked = true,
        onCheckedChange = {}
    )
}

@Preview(showBackground = false)
@Composable
private fun CircularCheckboxUncheckedPreview() {
    CircularCheckbox(
        checked = false,
        onCheckedChange = {}
    )
}