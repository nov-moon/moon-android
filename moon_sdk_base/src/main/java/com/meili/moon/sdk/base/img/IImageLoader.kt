package com.meili.moon.sdk.base.img

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.meili.moon.sdk.util.isEmpty
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.nostra13.universalimageloader.utils.MemoryCacheUtils

/**
 * Created by imuto on 2018/6/4.
 */
interface IImageLoader {

    fun bind(img: ImageView, uri: String?, trustMemory: Boolean = true)
    fun bind(img: ImageView, uri: String?, options: DisplayImageOptions, trustMemory: Boolean = true)
    fun bind(img: ImageView, uri: String?, options: DisplayImageOptions, listener: ImageLoadingListener?, trustMemory: Boolean = true)
    fun load(uri: String?, options: DisplayImageOptions, listener: ImageLoadingListener?)
    fun load(uri: String?, targetImageSize: ImageSize, options: DisplayImageOptions?,
             listener: ImageLoadingListener?, trustMemory: Boolean = true)

    fun loadSync(uri: String?, options: DisplayImageOptions, trustMemory: Boolean): Bitmap?

    object ImageLoaderImpl : IImageLoader {

        override fun bind(img: ImageView, uri: String?, trustMemory: Boolean) {
            checkMemory(trustMemory, uri, img)
            ImageLoader.getInstance().displayImage(processUri(uri), img)
        }

        override fun bind(img: ImageView, uri: String?, options: DisplayImageOptions, trustMemory: Boolean) {
            checkMemory(trustMemory, uri, img)
            ImageLoader.getInstance().displayImage(processUri(uri), img, options)
        }

        override fun bind(img: ImageView, uri: String?, options: DisplayImageOptions, listener: ImageLoadingListener?, trustMemory: Boolean) {
            checkMemory(trustMemory, uri, img)
            ImageLoader.getInstance().displayImage(processUri(uri), img, options, listener)
        }

        override fun load(uri: String?, options: DisplayImageOptions, listener: ImageLoadingListener?) {
            val innerListener = InnerListener(listener, null)
            ImageLoader.getInstance().loadImage(processUri(uri), null, options, innerListener)
        }

        override fun load(uri: String?, targetImageSize: ImageSize, options: DisplayImageOptions?, listener: ImageLoadingListener?, trustMemory: Boolean) {
            checkMemory(trustMemory, uri, targetImageSize = targetImageSize)
            val innerListener = InnerListener(listener, targetImageSize)
            ImageLoader.getInstance().loadImage(processUri(uri), targetImageSize, options, innerListener)
        }

        override fun loadSync(uri: String?, options: DisplayImageOptions, trustMemory: Boolean): Bitmap? {
            return ImageLoader.getInstance().loadImageSync(processUri(uri), options)
        }

        private fun checkMemory(trustMemory: Boolean, uri: String?, img: ImageView? = null, targetImageSize: ImageSize? = null) {
            uri ?: return
            if (!trustMemory) {
                val imageSize = targetImageSize ?: ImageSize(img!!.width, img.height)
                val generateKey = MemoryCacheUtils.generateKey(uri, imageSize)
                ImageLoader.getInstance().memoryCache.remove(generateKey)
                ImageLoader.getInstance().diskCache.remove(uri)
            }
        }

        private fun processUri(uri: String?): String? {
            uri ?: return uri

            try {
                val uriNew = Uri.parse(uri)
                if (isEmpty(uriNew.scheme)) {
                    return "file://$uri"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return uri
        }

        private class InnerListener internal constructor(private val listener: ImageLoadingListener?, private val targetSize: ImageSize?) : ImageLoadingListener {

            override fun onLoadingStarted(uri: String?, view: View?) {
                listener?.onLoadingStarted(uri, view)
            }

            override fun onLoadingFailed(uri: String?, view: View?, failReason: FailReason) {
                listener?.onLoadingFailed(uri, view, failReason)
            }

            override fun onLoadingComplete(uri: String?, view: View?, loadedImage: Bitmap) {
                listener?.onLoadingComplete(uri, view, loadedImage)
            }

            override fun onLoadingCancelled(uri: String?, view: View?) {
                if (listener != null) {
                    var memoryCacheKey = uri
                    if (targetSize != null) {
                        memoryCacheKey = MemoryCacheUtils.generateKey(uri, targetSize)
                    }
                    val bitmap = ImageLoader.getInstance().memoryCache.get(memoryCacheKey)
                    if (bitmap != null) {
                        listener.onLoadingComplete(uri, view, bitmap)
                    } else {
                        listener.onLoadingCancelled(uri, view)
                    }
                }
            }
        }
    }
}