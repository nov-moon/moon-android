package com.meili.moon.gradle.plugin.internal

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.meili.moon.gradle.plugin.internal.permission.TransformDelegate
import com.meili.moon.gradle.plugin.internal.util.FileUtils
import com.meili.moon.gradle.plugin.internal.util.Log
import groovy.io.FileType
import org.gradle.api.tasks.StopExecutionException
/**
 * 动态权限的代码插桩入口。
 *
 * Transform是Gradle提供的标准代码处理入口。他将在dex打包前，给你处理class的机会。
 *
 *
 * PermissionTransform会在编译期遍历class文件，查看方法中是否使用Permission注解。
 * 如果发现了使用此注解的方法，则将此class文件进行插桩修改，并保存到临时目录。
 * 在遍历完成后，将保存的内容覆盖到系统目录中。
 * 插桩内容主要是尝试调用MNPermission.requestPermission()方法，
 * 如果返回true，则代表所请求权限都有，继续执行方法，如果返回false，则代表无请求的权限，需要进行请求操作
 *
 *
 * Transform的处理方法为：transform方法，他会提供class输入对象[input]和处理后的输出管理器[output]。
 * 1. 输入对象又分为jar文件输入和文件夹输入.
 *      一般情况下，所有的依赖包，依赖项目，都会是jar包输入，而当前项目源码会是文件夹输入。
 *      我们处理插桩方法，只处理当前工程下的源码部分。而引入的jar包都应该是已经处理完的内容。
 *
 * 2. 输出管理主要用来提供处理后的类如何存储。
 *      因为Transform在设计中是可能会有多个对象，按注册顺序生成Transform队列。
 *      队头的Transform输出结果作为下一个Transform的输入内容，所以如何规范化输出结果就是上下两个Transform内容能否连贯的重要功能。
 *      transform方法的入参，提供了output参数，用来生成输出文件的存储位置，从而让上下Transform连贯起来。
 *
 * 更多Android对Transform的处理细节，可以参看Android插件的依赖库：gradle-core，他是google开发的，android插件的一个重要依赖库。
 * 里面有android编译的详细内容，transform部分可参见：LibraryTaskManager类和TaskManager类
 *
 */
class PermissionTransform extends Transform {

    /**
     * 当前Transform的名称，他将直接决定你的处理输出文件目录。一般情况下在build/intermediates/transforms/当前名称/
     */
    @Override
    String getName() {
        return "MoonKitPermissionTransform"
    }

    /**
     * 处理方法，入参transformInvocation封装了input、output等重要参数
     *
     * TransformInvocation中的参数列举：
     *      context.path = :当前module名称:transformClassesWithMoonKitTransformForDebug
     *      context.temp = 当前module下的build/tmp/transformClassesWithMoonKitTransformForDebug
     *      context.var = debug  猜测为当前编译类型
     *      input集合中的item：
     *          directoryInputs 目录输入
     *              name：一般为一个md5值，可能是路径的md5
     *              file: 当前目录的文件对象
     *          jarInputs jar输入
     *              name：jar包的包名
     *                  如果是maven的方式引入，可能是：com.android.support:support-core-ui:27.1.1
     *                  如果是项目依赖，可能是：:moon_sdk_kit
     *              file: 当前jar包的文件对象
     *
     * 在本方法中，我们做了两个事情：
     * 1. 遍历了input内容，并将每个文件都给到委托对象TransformDelegate，在其中做文件判断，记录，处理。
     * 2. 将遍历完成的input内容存放到output生成的文件位置上
     *
     * 具体的字节码处理方式，请参见TransformDelegate类
     *
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        Log.e("----------------------------- Start -----------------------------------------")
        def input = transformInvocation.getInputs()
        def output = transformInvocation.outputProvider

        // 创建委托对象
        def delegate = new TransformDelegate(transformInvocation.context.temporaryDir)

        // 遍历input内容
        input.each {
            // 遍历文件夹类型的内容
            it.directoryInputs.each {
                // 生成当前transform的文件位置 TODO 这里一般会使用文件路径md5 + name，暂时先这么处理，看看bug如何发生
                def cacheDir = output.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)

                def targetFile = it.file
                if (targetFile.isDirectory()) {
                    // 此方法会递归遍历此文件夹下的所有文件
                    targetFile.eachFileRecurse(FileType.FILES) { File file ->
                        // 将目标文件，例如某某class，给到委托类进行判断和记录
                        delegate.process(file, cacheDir)
                    }
                } else {
                    // 将目标文件，例如某某class，给到委托类进行判断和记录
                    delegate.process(targetFile, cacheDir)
                }


                // 将当前文件以及子文件全部copy到output生成的文件目录中
                FileUtils.copyFile(targetFile.absolutePath, cacheDir.absolutePath)

                delegate.processEnd()
            }

            // 遍历jar类型的内容
            it.jarInputs.each {
                Log.e("input: jar name = " + it.getName())
                Log.e("input: jar path = " + it.getFile().absolutePath)

                def targetFile = it.file

                // 生成当前transform的文件位置
                def cacheFile = output.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)

                if (targetFile.isDirectory()) {
                    throw new StopExecutionException("jar文件不应该为文件夹(${it.getName()})")
                }

                // 将当前文件以及子文件全部copy到output生成的文件目录中
                FileUtils.copyFile(targetFile.absolutePath, cacheFile.absolutePath)
            }
        }

        Log.e("=============================== End =========================================")
    }

    /**
     * Returns the type(s) of data that is consumed by the Transform. This may be more than
     * one type.
     *
     * <strong>This must be of type {@link com.android.build.api.transform.QualifiedContent.DefaultContentType}</strong>
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * Returns the scope(s) of the Transform. This indicates which scopes the transform consumes.
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
//        return ImmutableSet.of()
        return TransformManager.PROJECT_ONLY
    }

    /**
     * Returns whether the Transform can perform incremental work.
     *
     * <p>If it does, then the TransformInput may contain a list of changed/removed/added files, unless
     * something else triggers a non incremental run.
     */
    @Override
    boolean isIncremental() {
        return false
    }
}