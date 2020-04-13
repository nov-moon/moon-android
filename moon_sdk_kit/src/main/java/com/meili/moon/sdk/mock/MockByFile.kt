package com.meili.moon.sdk.mock

/**
 * mock数据的注解：从文件加载mock
 *
 * 默认在asset下的mock文件夹读取，比如[filePath] == 'test'，则文件在assets下的路径为mock/test。
 * 用户可以指定当前file路径是否为asset的，默认为true。如果设置为false则认为[filePath]为手机存储卡上的全路径
 *
 * @param [filePath] 文件路径
 * @param [isAssets] 是否为asset文件
 *
 * Created by imuto on 2018/12/21.
 */
@Target(AnnotationTarget.CLASS)
annotation class MockByFile(val filePath: String, val isAssets: Boolean = true)