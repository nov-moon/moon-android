package com.meili.moon.gradle.plugin.internal.permission

import com.meili.moon.gradle.plugin.internal.util.ASMUtils
import com.meili.moon.gradle.plugin.internal.util.FileUtils
import com.meili.moon.gradle.plugin.internal.util.Log
import org.objectweb.asm.ClassReader

/**
 * 文件处理的委托类
 *
 * 在遍历输入文件时，会分两类进行遍历：class、jar
 * 此类只处理class文件
 * 如果当前class文件中有Permission注解的方法，则将此类做插装处理，并将新的class存到临时目录中
 * 当所有类都遍历完成后，将临时目录中的文件，替换目标class文件
 */
class TransformDelegate {

    // 开始处理时间
    private def startTime = System.currentTimeMillis()

    // class文件的后缀名
    private def classSuffix = ".class"

    private def targetAnnotation = "Lcom/meili/moon/sdk/permission/Permission;"

    // 字节码插装的处理器
    private PermissionAsmProcessor processor = new PermissionAsmProcessor()

    private Map<String, File> tempProcessFiles = new HashMap<>()

    private File mTempDir

    TransformDelegate(File tempDir) {
        mTempDir = tempDir
        if (mTempDir != null && !mTempDir.exists()) {
            mTempDir.mkdirs()
        }
    }

    /**
     * 根据[scanClass]、[scanJar]方法的结果，进行字节码插装处理
     */
    def process(File file, File cacheDir) {
        // 不是目标文件类型，直接返回
        def fileName = file.name
        if (!fileName.endsWith(classSuffix)) {
            return
        }

        ClassReader cr = new ClassReader(file.newInputStream())

        if (!ASMUtils.hasAnnotationOnMethod(file, targetAnnotation, true)) {
            return
        }

        def outFile = new File(mTempDir.absolutePath + "/" + cr.className + ".class")
        if (outFile.exists()) {
            outFile.delete()
        }
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs()
        }
        outFile.createNewFile()

        Log.e("outFile = $outFile.absolutePath")

        def outStream = new FileOutputStream(outFile)
        processor.processClass(file.newInputStream(), outStream)

        outStream.flush()
        outStream.close()

        Log.e("cacheDir=$cacheDir.absolutePath  className=$cr.className  outFileName=$outFile.name")

        tempProcessFiles.put(cacheDir.absolutePath + "/" + cr.className + ".class", outFile)

        Log.e("处理结束")
        Log.e("处理用时：${System.currentTimeMillis() - startTime}")
    }

    def processEnd() {
        tempProcessFiles.each { String key, File value ->
            def result = FileUtils.replace(key, value.absolutePath)
            Log.e("tempProcessFiles -> result($result) to=$key  from=$value.absolutePath")
        }
        tempProcessFiles.clear()
    }
}