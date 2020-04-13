package com.meili.moon.sdk.util

import android.os.Process
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.LogUtil
import java.io.*
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.text.DecimalFormat
import java.util.*

/**
 * @author: imuto
 * @date: 2015/06/26
 * 进程间锁, 仅在同一个应用中有效.
 */
class ProcessLock private constructor(
        private val mLockName: String,
        private val mFile: File,
        private val mFileLock: FileLock,
        private val mStream: Closeable) : Closeable {

    /**
     * 锁是否有效
     */
    val isValid: Boolean
        get() = isValid(mFileLock)

    /**
     * 释放锁
     */
    fun release() {
        release(mLockName, mFileLock, mFile, mStream)
    }

    /**
     * 释放锁
     */
    @Throws(IOException::class)
    override fun close() {
        release()
    }

    override fun toString(): String {
        return mLockName
    }

    companion object {

        private val LOCK_FILE_DIR_STR = "process_lock"
        private val PID = Process.myPid()
        private val LOCK_MAP = HashMap<String, ProcessLock>(5)
        private val LOCK_FILE_DIR = File(CommonSdk.environment().appDir(), LOCK_FILE_DIR_STR)

        init {
            LOCK_FILE_DIR.deleteRecursively()
            LOCK_FILE_DIR.mkdirs()
        }

        /**
         * 获取进程锁
         *
         * @param lockName
         * @param writeMode 是否写入模式(支持读并发).
         * @return null 或 进程锁, 如果锁已经被占用, 返回null.
         */
        fun tryLock(lockName: String, writeMode: Boolean): ProcessLock? {
            return tryLockInternal(lockName, customHash(lockName), writeMode)
        }

        /**
         * 获取进程锁
         *
         * @param lockName
         * @param writeMode         是否写入模式(支持读并发).
         * @param maxWaitTimeMillis 最大值 1000 * 60
         * @return null 或 进程锁, 如果锁已经被占用, 则在超时时间内继续尝试获取该锁.
         */
        fun tryLock(lockName: String, writeMode: Boolean, maxWaitTimeMillis: Long): ProcessLock? {
            var lock: ProcessLock? = null
            val expiryTime = System.currentTimeMillis() + maxWaitTimeMillis
            val hash = customHash(lockName)
            while (System.currentTimeMillis() < expiryTime) {
                lock = tryLockInternal(lockName, hash, writeMode)
                if (lock != null) {
                    break
                } else {
                    try {
                        Thread.sleep(1) // milliseconds
                    } catch (ignored: Throwable) {
                    }

                }
            }

            return lock
        }

        private fun isValid(fileLock: FileLock?): Boolean {
            return fileLock != null && fileLock.isValid
        }

        private fun release(lockName: String, fileLock: FileLock?, file: File, stream: Closeable?) {
            synchronized(LOCK_MAP) {
                if (fileLock != null) {
                    try {
                        LOCK_MAP.remove(lockName)
                        fileLock.release()
                        LogUtil.d("released: $lockName:$PID")
                    } catch (ignored: Throwable) {
                    } finally {
                        closeQuietly(fileLock.channel())
                    }
                }
                closeQuietly(stream)
                file.deleteRecursively()
            }
        }

        private val FORMAT = DecimalFormat("0.##################")

        // 取得字符串的自定义hash值, 尽量保证255字节内的hash不重复.
        private fun customHash(str: String): String {
            if (isEmpty(str)) return "0"
            var hash = 0.0
            val bytes = str.toByteArray()
            for (i in 0 until str.length) {
                hash = (255.0 * hash + bytes[i]) * 0.005
            }
            return FORMAT.format(hash)
        }

        private fun tryLockInternal(lockName: String, hash: String, writeMode: Boolean): ProcessLock? {
            synchronized(LOCK_MAP) {

                // android对文件锁共享支持的不好, 暂时全部互斥.
                if (LOCK_MAP.containsKey(lockName)) {
                    val lock = LOCK_MAP[lockName]
                    if (lock == null) {
                        LOCK_MAP.remove(lockName)
                    } else if (lock.isValid) {
                        return null
                    } else {
                        LOCK_MAP.remove(lockName)
                        lock.release()
                    }
                }

                var input: FileInputStream? = null
                var out: FileOutputStream? = null
                var stream: Closeable? = null
                var channel: FileChannel? = null
                try {

                    val file = File(LOCK_FILE_DIR, hash)
                    if (file.exists() || file.createNewFile()) {
                        if (writeMode) {
                            out = FileOutputStream(file, false)
                            channel = out.channel
                            stream = out
                        } else {
                            input = FileInputStream(file)
                            channel = input.channel
                            stream = input
                        }
                        if (channel != null) {
                            val fileLock = channel.tryLock(0L, java.lang.Long.MAX_VALUE, !writeMode)
                            if (isValid(fileLock)) {
                                LogUtil.d("lock: $lockName:$PID")
                                val processLock = ProcessLock(lockName, file, fileLock, stream)
                                LOCK_MAP.put(lockName, processLock)
                                return processLock
                            } else {
                                release(lockName, fileLock, file, out)
                            }
                        } else {
                            throw IOException("can not get file channel:" + file.absolutePath)
                        }
                    }
                } catch (ignored: Throwable) {
                    LogUtil.d("tryLock: " + lockName + ", " + ignored.message)
                    closeQuietly(input)
                    closeQuietly(out)
                    closeQuietly(channel)
                }

            }

            return null
        }
    }
}
