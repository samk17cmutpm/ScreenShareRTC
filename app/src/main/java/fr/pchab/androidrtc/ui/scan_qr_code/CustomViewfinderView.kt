package fr.pchab.androidrtc.ui.scan_qr_code

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.ViewfinderView
import fr.pchab.androidrtc.R

const val SCAN_VELOCITY = 50 // tốc độ của của scan line

class CustomViewfinderView(context: Context?, attrs: AttributeSet?) :
    ViewfinderView(context, attrs) {

    private var scanLineTop = 0
    private var scanLightHeight = 75

    override fun onDraw(canvas: Canvas?) {

        refreshSizes()
        if (framingRect == null || previewFramingRect == null) {
            return
        }

        paint.color = if (paint != null) resultColor else maskColor

        val frame = framingRect
        val previewFrame = previewFramingRect

        val frameTop = frame.top.toFloat()
        val frameBottom = frame.bottom.toFloat()
        val frameLeft = frame.left.toFloat()
        val frameRight = frame.right.toFloat()

        val width = width.toFloat()
        val height = height.toFloat()

        canvas?.apply {
            drawRect(0f, 0f, width, frameTop, paint)
            drawRect(0f, frameTop, frameLeft, frameBottom + 1, paint)
            drawRect(frameRight + 1, frameTop, width, frameBottom + 1, paint)
            drawRect(0f, frameBottom + 1, width, height, paint)
        }

        canvas?.let { canvas ->
            if (resultBitmap != null) {
                paint.alpha = CURRENT_POINT_OPACITY
                canvas.drawBitmap(resultBitmap, null, frame, paint)
            } else {
                //Vẽ viền
                drawFrameBounds(canvas, frame)
                //vẽ đường scan
                drawScanLight(canvas, frame)

                val scaleX = frame.width() / previewFrame.width()
                val scaleY = frame.height() / previewFrame.height()

                val currentPossible = possibleResultPoints
                val currentLast = lastPossibleResultPoints

                if (currentPossible.isEmpty()) {
                    lastPossibleResultPoints = null
                } else {
                    possibleResultPoints = ArrayList<ResultPoint>(5)
                    lastPossibleResultPoints = currentPossible

                    paint.apply {
                        alpha = CURRENT_POINT_OPACITY
                        color = resultPointColor
                    }
                    currentPossible.forEach { point ->
                        canvas.drawCircle(
                            frameLeft + (point.x * scaleX),
                            frameTop + (point.y * scaleY),
                            POINT_SIZE.toFloat(), paint
                        )
                    }
                }

                currentLast?.let {
                    paint.apply {
                        alpha = CURRENT_POINT_OPACITY / 2
                        color = resultPointColor
                    }

                    val radius = POINT_SIZE / 2.0f

                    currentLast.forEach { point ->
                        canvas.drawCircle(
                            frameLeft + (point.x * scaleX),
                            frameTop + (point.y * scaleY),
                            radius, paint
                        )
                    }
                }

                //vẽ lại scanline
                postInvalidateDelayed(
                    ANIMATION_DELAY,
                    frameLeft.toInt() - POINT_SIZE,
                    frameTop.toInt() - POINT_SIZE,
                    frameRight.toInt() + POINT_SIZE,
                    frameBottom.toInt() + POINT_SIZE
                )
            }
        }
    }

    /**
     * Vẽ đường quét
     *
     * @param canvas
     * @param frame
     */
    private fun drawScanLight(canvas: Canvas, frame: Rect) {
        paint.color = laserColor
        paint.alpha = 250
        if (scanLineTop == 0 || scanLineTop + SCAN_VELOCITY >= frame.bottom) {
            scanLineTop = frame.top
        } else {
            scanLineTop += SCAN_VELOCITY // thay đổi vị trí của scan line
        }
        val scanRect = Rect(
            frame.left, scanLineTop, frame.right,
            scanLineTop + scanLightHeight
        )
        val scanLight: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.scan_line)
        canvas.drawBitmap(scanLight, null, scanRect, paint)
    }

    /**
     * Vẽ đường viền khung - 4 góc của khung quét
     *
     * @param canvas
     * @param frame
     */
    private fun drawFrameBounds(canvas: Canvas, frame: Rect) {

        val frameTop = frame.top.toFloat()
        val frameBottom = frame.bottom.toFloat()
        val frameLeft = frame.left.toFloat()
        val frameRight = frame.right.toFloat()

        paint.color = Color.parseColor("#1373E3")
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 5f
        //chiều dài/rộng của các góc
        val width: Int = frame.width()
        val corLength = (width * 0.05).toInt()
        var corWidth = (corLength * 0.2).toInt()
        corWidth = if (corWidth > 15) 15 else corWidth

        canvas.apply {
            //trên - trái
            drawRect(frameLeft - corWidth, frameTop, frameLeft, frameTop + corLength, paint)
            drawRect(
                frameLeft - corWidth,
                frameTop - corWidth,
                frameLeft + corLength,
                frameTop,
                paint
            )
            // trân - phải
            drawRect(frameRight, frameTop, frameRight + corWidth, frameTop + corLength, paint)
            drawRect(
                frameRight - corLength,
                frameTop - corWidth,
                frameRight + corWidth,
                frameTop,
                paint
            )
            // dưới - trái
            drawRect(frameLeft - corWidth, frameBottom - corLength, frameLeft, frameBottom, paint)
            drawRect(
                frameLeft - corWidth,
                frameBottom,
                frameLeft + corLength,
                frameBottom + corWidth,
                paint
            )
            // dưới - phải
            drawRect(frameRight, frameBottom - corLength, frameRight + corWidth, frameBottom, paint)
            drawRect(
                frameRight - corLength,
                frameBottom,
                frameRight + corWidth,
                frameBottom + corWidth,
                paint
            )
        }
    }
}