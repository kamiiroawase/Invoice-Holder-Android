package com.github.kamiiroawase.android.invoiceholder.util

import androidx.core.graphics.createBitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri

object BitmapRenderUtil {
    fun decodeImageToBitmap(
        context: Context,
        uri: Uri,
        maxSideLength: Int = 2048
    ): Bitmap? {
        try {
            return ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            ) { decoder, info, _ ->
                if (info.size.width <= 0 || info.size.height <= 0) {
                    throw IllegalArgumentException("Invalid image size")
                }

                val scaleSize = calculateScaledSize(
                    info.size.width,
                    info.size.height,
                    maxSideLength
                )

                decoder.setTargetSize(scaleSize.width, scaleSize.height)
            }
        } catch (_: Exception) {
            return null
        }
    }

    fun renderPdfPages(
        context: Context,
        uri: Uri,
        maxSideLength: Int = 2048,
        onBitmapReady: ((bitmap: Bitmap?) -> Unit)
    ) {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { page ->
                        try {
                            if (page.width <= 0 || page.height <= 0) {
                                throw IllegalArgumentException("Invalid image size")
                            }

                            val scaleSize = calculateScaledSize(
                                page.width,
                                page.height,
                                maxSideLength,
                                true,
                            )

                            val bitmap = createBitmap(scaleSize.width, scaleSize.height)

                            bitmap.eraseColor(Color.WHITE)

                            val matrix = Matrix().apply {
                                setScale(scaleSize.scale, scaleSize.scale)
                            }

                            page.render(
                                bitmap,
                                null,
                                matrix,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )

                            onBitmapReady.invoke(bitmap)
                        } catch (_: Exception) {
                            onBitmapReady.invoke(null)
                        }
                    }
                }
            }
        }
    }

    fun calculateScaledSize(
        srcWidth: Int,
        srcHeight: Int,
        maxSideLength: Int = 2048,
        allowUpscale: Boolean = false
    ): ScaledSize {
        val maxSide = maxOf(srcWidth, srcHeight)

        val scale = when {
            !allowUpscale && maxSide <= maxSideLength -> 1f
            else -> maxSideLength.toFloat() / maxSide
        }

        return ScaledSize(
            scale,
            maxOf(1,(srcWidth * scale).toInt()),
            maxOf(1,(srcHeight * scale).toInt())
        )
    }

    data class ScaledSize(
        val scale: Float,
        val width: Int,
        val height: Int
    )
}
