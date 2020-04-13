package com.meili.moon.sdk.common

import com.meili.moon.sdk.util.assertTrue

/**
 * 数据的锁状态持有对象
 *
 * 主要防止某些数据误操作，从而引起问题。
 * 使用方式是，在操作数据前，必须先执行[unlock]解锁，然后操作数据。
 * 操作数据时，内部会执行[assertUnlock]来检查锁状态，如果锁状态为未锁定，则正常执行，否则将报错。
 * 在完成检查，并执行完数据操作后，再执行[lock]锁定状态。
 *
 * Created by imuto on 2019-09-09.
 */
interface ILocker {
    /**
     * 将状态转换为锁定
     */
    fun lock()

    /**
     * 将状态转换为解锁
     */
    fun unlock()

    /**
     * 断言当前状态为解锁状态，断言失败则报错。
     */
    fun assertUnlock()
}

/**
 * 一般情况下的锁委托对象
 */
class LockerDelegate() : ILocker {
    private var lockState = 0
    /**
     * 将状态转换为锁定
     */
    override fun lock() {
        lockState = 0
    }

    /**
     * 将状态转换为解锁
     */
    override fun unlock() {
        lockState = 1
    }

    /**
     * 断言当前状态为解锁状态，断言失败则报错。
     */
    override fun assertUnlock() {
        (lockState == 1).assertTrue()
    }

}