package com.meili.moon.gradle.plugin.internal.kit

import com.meili.moon.gradle.plugin.internal.util.Log

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * 文件处理的委托类
 *
 * 在遍历输入文件时，会分两类进行遍历：class、jar
 * 1. 在遍历class时，直接通过BytecodeProcessor判断当前class是否为目标class，如果是，则将当前类的类全名记录在processor中
 * 2. 在遍历jar时，将对jar进行解包，使用遍历class方式遍历所有文件。
 *      在遍历jar时，如果当前类是我们的DefComponentInstaller类，则记录下包含他的jar文件对象，在后面字节码插装时使用。
 *
 * 在文件遍历完成后，processor中将得到一个存放了所有目标类全名的list，和要进行插装的目标jar文件记录
 *
 * process方法使用以上记录，进行字节码插装。
 *
 * Created by imuto on 2019-07-24.
 */
class TransformDelegate {

    // 开始处理时间
    private def startTime = System.currentTimeMillis()

    // class文件的后缀名
    private def classSuffix = ".class"

    // 遍历jar包是的忽略特征值：android相关jar包
    private def ignoreAndroidJar = "com.android"
    // 遍历jar包是的忽略特征值：android相关jar包
    private def ignoreAndroidJar2 = "android"
    // 遍历jar包是的忽略特征值：一些其他的第三方jar包
    private def ignoreOrgPrefixJar = "org"

    // 我们要进行插装的目标类全名
    private def processClassPath = "com/meili/moon/sdk/util/DefComponentInstaller"
    // 我们要进行插装的目标类全名，带有class后缀
    private def processClassId = "${processClassPath}${classSuffix}"
    // 我们要进行插装的目标类所在jar的文件对象
    private File processJar = null

    // 字节码插装的处理器
    private BytecodeProcessor processor = new KitAsmProcessor(processClassPath)

    /**
     * 尝试扫描当前文件，并将符合规则的类全名记录在processor中
     */
    def scanClass(File file) {
        def fileName = file.name
        if (fileName.endsWith(classSuffix)) {
            scanClassInner(new FileInputStream(file))
        }
    }

    /**
     * 扫描当前jar包，判断是否包含插装类，如果包含则记录当前jar包要存储的位置。
     * 扫描当前jar包，将符合规则的类全名记录在processor中
     *
     * @param name 当前jar包在input中的名称，可能会是：com.android.support:appcompat-v7:27.1.1
     * @param file 当前jar的文件对象
     * @param to 当前jar，在output规则下的最终存储文件对象
     */
    def scanJar(String name, File file, File to) {
        if (!file.exists()) return

        if (name.startsWithAny(ignoreAndroidJar, ignoreAndroidJar2, ignoreOrgPrefixJar)) {
            return
        }

        def jarFile = new JarFile(file)
        def entries = jarFile.entries()

        while (entries.hasMoreElements()) {
            def element = entries.nextElement()
            def path = element.name
            if (path == processClassId) {
                processJar = to
            } else if (path.endsWith(classSuffix)) {
                def inputStream = jarFile.getInputStream(element)
                scanClassInner(inputStream)
            }
        }
        jarFile.close()
    }

    /**
     * 根据[scanClass]、[scanJar]方法的结果，进行字节码插装处理
     */
    def process() {

        if (processJar == null || !processJar.exists()) {
            Log.e("没有找到目标jar文件（${processJar}）")
            Log.e("处理用时：${System.currentTimeMillis() - startTime}")
            return
        }

        if (!processor.validate()) {
            Log.e("没有找到目标IComponent")
            Log.e("处理用时：${System.currentTimeMillis() - startTime}")
            return
        }

        Log.e("开始处理 GoGo")

        // 新建一个目标jar文件的缓存文件，用来接收目标jar文件的文件内容和插装后的文件
        def toJarFile = new File(processJar.absolutePath + ".tmp")
        if (toJarFile.exists()) {
            toJarFile.delete()
        }
        toJarFile.createNewFile()

        // 最终jar的访问流
        def toJarOS = new JarOutputStream(new FileOutputStream(toJarFile))

        // 打开目标jar文件，并遍历其中的元素。
        // 1. 将不是目标class的文件，原封不动的放入最终jar
        // 2. 将目标class进行字节码插装，并放入最终jar
        // 3. 将最终生成的jar文件，覆盖原来的目标jar文件
        def fromJar = new JarFile(processJar)
        def entries = fromJar.entries()
        while (entries.hasMoreElements()) {
            def element = entries.nextElement()
            def name = element.name

            def inputStream = fromJar.getInputStream(element)

            def jarEntry = new JarEntry(name)
            toJarOS.putNextEntry(jarEntry)

            // 如果是目标class，则进行插装，并写入最终jar包。
            // 如果不是，直接写入最终的jar包
            if (name.contains(processClassId)) {
                processClass(inputStream, toJarOS)
            } else {
                inputStream.eachByte(512) { buffer, len ->
                    toJarOS.write(buffer, 0, len)
                }
            }

            inputStream.close()
            toJarOS.closeEntry()
        }
        toJarOS.close()
        fromJar.close()

        processJar.delete()
        def isSuccess = toJarFile.renameTo(processJar)

        Log.e("处理结束（${isSuccess}）")
        Log.e("处理用时：${System.currentTimeMillis() - startTime}")
    }

    private def scanClassInner(InputStream stream) {
        processor.scanClass(stream)
    }

    private void processClass(InputStream inputStream, OutputStream outputStream) {
        processor.processClass(inputStream, outputStream)
    }

    /**
     * 字节码处理器，提供字节码扫描入口，字节码插装入口，以及字节码扫描结果是否可用
     */
    interface BytecodeProcessor {
        /**
         * 扫描当前文件输入，并查看是否符合入参规则
         */
        void scanClass(InputStream stream)

        /**
         * 给出目标文件输入流和字节码处理完成后的输出流。在输入流内容上，修改字节码，并最终写入到输出流上
         *
         */
        void processClass(InputStream inputStream, OutputStream outputStream)

        /**
         * 扫描完成后，当前扫描结果是否可用
         */
        boolean validate()
    }
}