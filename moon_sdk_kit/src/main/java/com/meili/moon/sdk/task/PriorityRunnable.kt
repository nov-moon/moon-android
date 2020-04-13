package com.meili.moon.sdk.task

/**
 * 有优先级属性的Runnable
 * Created by imuto on 16/5/23.
 */
internal class PriorityRunnable(priority: Priority?, private val runnable: Runnable) : Runnable {
    var SEQ: Long = 0
    val priority: Priority = priority ?: Priority.DEFAULT
    override fun run() {
        this.runnable.run()
    }
}
