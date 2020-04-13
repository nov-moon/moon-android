package com.meili.moon.sdk.cache.lru

import com.meili.moon.sdk.cache.lru.model.DefCacheModel
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.log.TAG
import com.meili.moon.sdk.util.assertTrue
import com.meili.moon.sdk.util.encrypt.MD5
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.lengthRecursively
import java.io.File

/**缓存文件后缀名*/
private const val TEMP_EXTENSION = "ml_tmp"

/**
 * 文件缓存实现
 *
 * TODO 缺少进程内文件锁
 *
 * Created by imuto on 2018/3/19.
 */
class FileLruCache internal constructor(
        /**文件缓存文件夹,本文件夹内容会根据缓存策略自动删除，请不要在这个目录存放其他内容*/
        private val cacheDir: File)
    : BaseLruDbCache<File>(cacheDir.absolutePath) {

    init {
        LogUtil.d(TAG, "创建了文件缓存对象，缓存对象：${this}，缓存路径：${cacheDir.absolutePath}")
    }

    private val tempFileLambda: (File) -> Boolean = { it.isDirectory && it.extension == TEMP_EXTENSION }

    /**缓存不可用提示信息*/
    private val unavailableMsg = "${this::class.simpleName}:缓存目录不可用，缓存目录=$cacheTypeKey"

    override fun put(key: String, value: File) {
        put(key, DefCacheModel(value.absolutePath))
    }

    override fun onPut(model: LruDiskCacheModel) {
        val file = File(model.cacheBody)
        file.exists().assertTrue("缓存文件不存在：${model.cacheBody}")

        val copyTo = file.copyTo(File(cacheDir, "${MD5.getFileMD5String(file)}.$TEMP_EXTENSION"))
        copyTo.exists().assertTrue()

        model.cacheBody = copyTo.absolutePath
        model.isFileCache = true
    }

    override fun getValue(key: String): File? {
        return try {
            File(get(key)?.cacheBody)
        } catch (e: Exception) {
            LogUtil.e(e)
            null
        }
    }

    override fun availableCache(model: LruDiskCacheModel): Boolean {
        // 是否存在maxAge属性，如果存在使用maxAge做判断，如果不存在使用true
        val cacheAvailable = if (model.expires > 0) {
            model.expires * 1000 > System.currentTimeMillis()
        } else {
            true
        }
        // 缓存可用并且文件存在
        return try {
            cacheAvailable && File(model.cacheBody).exists()
        } catch (e: Exception) {
            LogUtil.e(e)
            false
        }
    }

    override fun onClearItem(model: LruDiskCacheModel) {
        if (isEmpty(model.cacheBody)) return

        File(model.cacheBody).delete().assertTrue()
    }

    /**
     * 父类做完父类规则删除后，这里进行文件规则删除，如果当前缓存文件超出最大缓存，则进行删除操作
     *
     * 1. 首先删除数据库没有记录的缓存文件，如果缓存占用已经满足，则退出。否则进入第二步
     * 2. 按照LRU规则，删除缓存记录，直到满足最大缓存文件规则
     */
    override fun onTrim2Size() {
        val config = globalConfig.get()
        // 检查当前缓存占用空间是否已经超出限额
        var overflow = getCacheFileSize() - config.cacheMaxFileSize
        if (overflow <= 0) {
            return
        }

        // 删除在数据库没有记录的缓存文件
        var deletedCount = 0L
        cacheDir.listFiles(tempFileLambda)
                .forEach {
                    val model = db.get(it.absolutePath, LruDiskCacheModel::class)
                    if (model == null) {
                        val len = it.length()
                        if (it.delete()) {
                            deletedCount += len
                        }
                    }
                }

        overflow -= deletedCount

        if (overflow < 0) {
            return
        }

        deletedCount = 0

        // 按照LRU的规则，删除缓存文件，直到缓存占用控制在最大缓存区间
        getLruOrderList {
            and("isFileCache", "=", true)
        }.forEach {
            if (overflow - deletedCount <= 0) {
                return@forEach
            }
            val len = File(it.cacheBody).length()
            remove(it)
            deletedCount += len
        }
    }

    override fun available() {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs().assertTrue(unavailableMsg)
        } else {
            cacheDir.isDirectory.assertTrue(unavailableMsg)
        }
    }

    private fun getCacheFileSize(): Long = cacheDir.lengthRecursively(tempFileLambda)
}