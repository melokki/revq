package eu.revq

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import eu.revq.resources.Res
import eu.revq.resources.revq_brand_mark
import org.jetbrains.compose.resources.painterResource

@Composable
fun appBrandPainter(): Painter = painterResource(Res.drawable.revq_brand_mark)

@Composable
fun AppBrandMark(
    modifier: Modifier = Modifier,
    contentDescription: String? = "RevQ application icon",
) {
    Image(
        painter = appBrandPainter(),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
