package com.braiso22.bstock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import bstock.shared.generated.resources.Res
import bstock.shared.generated.resources.ic_image
import coil3.compose.SubcomposeAsyncImage
import com.braiso22.bstock.data.Item
import org.jetbrains.compose.resources.painterResource

@Composable
fun ItemImage(url: String, modifier: Modifier = Modifier, grayscale: Boolean = false) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isBlank()) {
            ImagePlaceholder()
        } else {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = if (grayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                modifier = Modifier.fillMaxSize(),
                loading = { CircularProgressIndicator(Modifier.size(20.dp).align(Alignment.Center), strokeWidth = 2.dp) },
                error = { ImagePlaceholder() },
            )
        }
    }
}

@Composable
private fun ImagePlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(Res.drawable.ic_image),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ItemRow(
    item: Item,
    supportingText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Item ya hecho (p. ej. metido en el carro): fila en gris y texto tachado. */
    crossedOut: Boolean = false,
    /** Fondo a usar en vez del de por defecto (que depende de si está bajo mínimo). */
    containerColor: Color? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val decoration = if (crossedOut) TextDecoration.LineThrough else null
    Card(
        onClick = onClick,
        modifier = modifier.alpha(if (crossedOut) 0.6f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = when {
                crossedOut -> MaterialTheme.colorScheme.surfaceContainerHighest
                containerColor != null -> containerColor
                item.isBelowMinimum -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = { ItemImage(item.imageUrl, Modifier.size(56.dp), grayscale = crossedOut) },
            headlineContent = {
                Text(item.name, style = MaterialTheme.typography.titleMedium, textDecoration = decoration)
            },
            supportingContent = { Text(supportingText, textDecoration = decoration) },
            trailingContent = trailing,
        )
    }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MessageContent(
    message: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(64.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
