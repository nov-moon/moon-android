package com.meili.moon.sdk.app.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.util.TypedValue
import android.R.attr.y
import android.R.attr.path
import android.R.attr.y
import android.R.attr.path
import android.graphics.*
import android.util.Log
import android.graphics.Canvas.ALL_SAVE_FLAG
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.base.util.hasShown
import com.meili.moon.sdk.log.LogUtil


/**
 * Author： fanyafeng
 * Date： 18/10/25 下午3:39
 * Email: fanyafeng@live.cn
 *
 * 以下百度抄的：
 *
 * 正弦曲线可表示为y=Asin(ωx+φ)+k，定义为函数y=Asin(ωx+φ)+k在直角坐标系上的图象，
 * 其中sin为正弦符号，x是直角坐标系x轴上的数值，y是在同一直角坐标系上函数对应的y值，k、ω和φ是常数（k、ω、φ∈R且ω≠0）
 *
 * A——振幅，当物体作轨迹符合正弦曲线的直线往复运动时，其值为行程的1/2。
 * (ωx+φ)——相位，反映变量y所处的状态。
 * φ——初相，x=0时的相位；反映在坐标系上则为图像的左右移动。
 * k——偏距，反映在坐标系上则为图像的上移或下移。
 * ω——角速度， 控制正弦周期(单位弧度内震动的次数)。
 */
class WaveView(val mContext: Context?, attrs: AttributeSet? = null) : View(mContext, attrs) {

    private val TAG = WaveView::class.java.simpleName

    private var A = dp2px(3)//view的一半
    private var k = dp2px(40)
    private var φ = 0F
    private var ω = 0.00

    private var path: Path? = null
    private var paint: Paint? = null

    private val SIN = 0
    private val COS = 1

    private var waveType = 0

    private val TOP = 0
    private val BOTTOM = 1

    private var waveFillType = 1

    private var waveSpeed = 3F

    private var startPeriod = 0.00

    private var valueAnimator: ValueAnimator? = null

    private var picBitmap: Bitmap? = null
    private var picBlueBitmap: Bitmap? = null
    private var bitmap: Bitmap? = null
    private var Bluebitmap: Bitmap? = null
    private var mBitmapWidth = 0
    private var mBitmapHight = 0

    init {
//        k = A
        initPaint()
        initAnimation()
        picBitmap = BitmapFactory.decodeResource(resources, R.drawable.logo_loading2_00000)
        picBlueBitmap = BitmapFactory.decodeResource(resources, R.drawable.logo_loading2_00036)
        bitmap = Bitmap.createBitmap(picBitmap, 0, 0, picBitmap?.width ?: 0, picBitmap?.height ?: 0)
        Bluebitmap = Bitmap.createBitmap(picBlueBitmap, 0, 0, picBitmap?.width
                ?: 0, picBitmap?.height ?: 0)
//        A = bitmap!!.height / 2
//        k = height / 2
//        k = bitmap!!.height + ((height - bitmap!!.height) / 2)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

//        canvas!!.drawARGB(255, 139, 197, 186)
//        canvas!!.drawColor(Color.WHITE)
        if (canvas != null)
            when (waveType) {
                SIN -> {
                    drawSin(canvas)
                }
                COS -> {
                    drawCos(canvas)
                }
            }
    }

    private fun drawCos(canvas: Canvas) {
        when (waveFillType) {
            TOP -> fillTop(canvas)
            BOTTOM -> fillBottom(canvas)
        }
    }

    private fun drawSin(canvas: Canvas) {
        when (waveFillType) {
            TOP -> fillTop(canvas)
            BOTTOM -> fillBottom(canvas)
        }
    }

    private fun initPaint() {
        path = Path()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint?.isAntiAlias = true
        paint?.style = Paint.Style.FILL_AND_STROKE
        paint?.color = Color.BLUE
    }

    private fun initAnimation() {
        valueAnimator = ValueAnimator.ofInt(0, width)
        valueAnimator?.duration = 1000
        valueAnimator?.repeatCount = ValueAnimator.INFINITE
        valueAnimator?.interpolator = LinearInterpolator()
        var startTime = System.currentTimeMillis()
        valueAnimator?.addUpdateListener {
            invalidate()
            it.currentPlayTime
            if (System.currentTimeMillis() - startTime > 500) {
                startTime = System.currentTimeMillis()
                if (!hasShown()) {
                    it.cancel()
                }
            }
        }
//        valueAnimator!!.start()
    }

    private fun fillTop(canvas: Canvas) {
        val layerId = canvas.saveLayer(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat(), null, Canvas.ALL_SAVE_FLAG)

        φ -= waveSpeed / 100
        Log.d(TAG, "初相：" + φ)
        var y = 0F
        path?.reset()
        path?.moveTo(0F, height.toFloat())

        var x = 0f
        while (x <= width) {
            y = (A * Math.sin(ω * x + φ + Math.PI * startPeriod) + k).toFloat()
            path?.lineTo(x, height - y)
            x += 20f
        }

        path?.lineTo(width.toFloat(), 0F)
        path?.lineTo(0F, 0F)
        path?.close()

        paint?.color = Color.WHITE

        canvas.drawPath(path, paint)
//        LogUtil.e("y=Asin(ωx+φ)+k","A:${A}B:${ω}C:${φ}D:${k}")
        paint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        paint?.color = Color.BLUE
        canvas.drawPath(path, paint)

        paint?.xfermode = null

        canvas.restoreToCount(layerId)
    }

    var isAdd = true
    var isBLue = true

    private fun fillBottom(canvas: Canvas) {
        if (isAdd) {
            if (k <= ((height - mBitmapHight) / 2)) {
                k = mBitmapHight + ((height - mBitmapHight) / 2)
                isBLue = !isBLue
            } else {
                k--
            }
        }

        val layerId = canvas.saveLayer(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        φ -= waveSpeed / 50
        var y = 0F
        path?.reset()
        path?.moveTo(0F, 0F)

        var x = 0f
        while (x <= width) {
            y = (A * Math.sin(ω * x + φ + Math.PI * startPeriod) + k).toFloat()
            path?.lineTo(x, y)
            x += 20f
        }

        path?.lineTo(width.toFloat(), height.toFloat())
        path?.lineTo(0F, height.toFloat())
        path?.close()
//        LogUtil.e("y=Asin(ωx+φ)+k","A:${A}B:${ω}C:${φ}D:${k}")
        mBitmapWidth = bitmap?.width ?: 0
        mBitmapHight = bitmap?.height ?: 0
        if (isBLue) {
            paint?.color = Color.parseColor("#064FA0")
            canvas?.drawBitmap(bitmap, ((width - mBitmapWidth) / 2).toFloat(), ((height - mBitmapHight) / 2).toFloat(), paint)
        } else {
            paint?.color = Color.parseColor("#CCCCCC")
            canvas?.drawBitmap(Bluebitmap, ((width - mBitmapWidth) / 2).toFloat(), ((height - mBitmapHight) / 2).toFloat(), paint)
        }
//        paint!!.color = Color.YELLOW
//        canvas.drawCircle((width / 4).toFloat(), (width / 4).toFloat(), (width / 4).toFloat(), paint)

        paint?.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)

        canvas.drawPath(path, paint)

        paint?.xfermode = null

        canvas.restoreToCount(layerId)
    }

    fun startAnimation() {
        if (valueAnimator != null) {
            valueAnimator?.start()
        }
    }

    fun stopAnimation() {
        if (valueAnimator != null) {
            valueAnimator?.cancel()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        ω = 2 * Math.PI / width
        mBitmapHight = bitmap?.height ?: 0
        mBitmapHight = bitmap?.height ?: 0
        k = bitmap?.height ?: 0 + ((height - mBitmapHight) / 2)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        LogUtil.e("wave销毁了");
        if (bitmap != null && !(bitmap!!.isRecycled))
        {
            bitmap=null
        }
        if (Bluebitmap != null && !(Bluebitmap!!.isRecycled))
        {
            Bluebitmap=null
        }

        picBitmap?.recycle()
        picBlueBitmap?.recycle()

    }

//    fun pauseAnimation() {
//        if (valueAnimator != null) {
//            valueAnimator!!.pause()
//        }
//    }
//
//    fun resumeAnimation() {
//        if (valueAnimator != null) {
//            valueAnimator!!.resume()
//        }
//    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    private fun dp2px(dpVal: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal.toFloat(), resources.displayMetrics).toInt()
    }
}
