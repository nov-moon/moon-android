package com.meili.moon.sdk.app.util

import android.app.Service
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Vibrator

typealias OnShakeCallback = () -> Unit

/**
 * 检测的时间间隔
 */
const val UPDATE_INTERVAL: Int = 40

/**
 * 摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。
 */
const val shakeThreshold: Int = 1000

const val SENSOR_VALUE = 14

class ShakeUtils : SensorEventListener {

    private var mSensorManager: SensorManager? = null
    private var mOnShakeListener: OnShakeCallback? = null

    private var vibrator: Vibrator? = null
    /**
     * 上一次检测的时间
     */
    private var mLastUpdateTime: Long = 0L
    /**
     * 上一次检测时，加速度在x、y、z方向上的分量，用于和当前加速度比较求差。
     */
    private var mLastX: Float = 0f
    private var mLastY: Float = 0f
    private var mLastZ: Float = 0f

    private val mRecorder = mutableListOf<Long>()

    private val forceShake = 7

    constructor(context: Context) {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        vibrator = context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator?

    }

    fun setOnShakeListener(onShakeListener: OnShakeCallback) {
        mOnShakeListener = onShakeListener
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val currentTime: Long = System.currentTimeMillis()
        val diffTime: Long = currentTime - mLastUpdateTime
        if (diffTime < UPDATE_INTERVAL || !enable) return

        mLastUpdateTime = currentTime
        if (event != null) {
            val x: Float = event.values[0]
            val y: Float = event.values[1]
            val z: Float = event.values[2]

            //判断他当前和上一次的加速度方向反向，并且当前值大于最小指定值，则进行记录。如果出现连续3次及以上，则认为发生一次摇一摇
            if (Math.abs(x) > forceShake && x * mLastX < 0) {
                record(event.timestamp)
            }
            // 因为考虑到，我们的摇一摇，暂时只针对x轴的摇一摇，所以先禁用y轴和z轴产生的摇一摇
//            else if (Math.abs(y) > forceShake && y * mLastY < 0) {
//                record(event.timestamp)
//            }
//            else if (Math.abs(z) > forceShake && z * mLastZ < 0) {
//                record(event.timestamp)
//            }

//            android.util.Log.d(TAG, "x = $x, y = $y, z = $z, lx = ${x * mLastX < 0}, ly = ${y * mLastY < 0}, lz = ${z * mLastZ < 0}, ")

            mLastX = x
            mLastY = y
            mLastZ = z

            tryShake()

//            val deltaX: Float = x - mLastX
//            val deltaY: Float = y - mLastY
//            val deltaZ: Float = z - mLastZ

//            LogUtil.e("x = $x, y = $y, z = $z")
//
//            var delta: Double = Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / diffTime * 10000
//            // 当加速度的差值大于指定的阈值，认为这是一个摇晃
//            if (delta > shakeThreshold && enable) {
//                vibrator?.vibrate(200)
//                mOnShakeListener?.invoke()
//            }
        }

    }

    private fun record(time: Long) {
        if (mRecorder.isNotEmpty()) {
            val end = mRecorder[mRecorder.size - 1]
            if (end - time > 1000) {
                mRecorder.clear()
            }
        }
        mRecorder.add(time)
    }

    private fun tryShake() {
        if (mRecorder.size > 1) {
            if (enable) {
                vibrator?.vibrate(200)
                mOnShakeListener?.invoke()
            }
            mRecorder.clear()
        }
    }

    fun onResume() {
        mSensorManager?.registerListener(this,
                mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun onPause() {
        mSensorManager?.unregisterListener(this)
    }

    var enable = true

}

fun hasRightValue(values: FloatArray): Boolean {
    values.iterator().withIndex().forEach {
        if (it.value > SENSOR_VALUE) return true
    }
    return false
}