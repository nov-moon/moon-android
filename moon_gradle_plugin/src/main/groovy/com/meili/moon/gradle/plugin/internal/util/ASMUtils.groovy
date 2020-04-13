package com.meili.moon.gradle.plugin.internal.util

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.util.ASMifier
import org.objectweb.asm.util.TraceClassVisitor
/**
 * ASM的工具类
 */
class ASMUtils {

    /**
     * 打印给定class内容如何使用asm进行生成
     *
     * 可以根据打印信息作为参考，编写对应的asm代码
     */
    static def printAsmCodeByPath(String file) {
        printAsmCodeByFile(new File(file))
    }

    /**
     * 打印给定class内容如何使用asm进行生成
     *
     * 可以根据打印信息作为参考，编写对应的asm代码
     */
    static def printAsmCodeByFile(File file) {
        if (!file.exists()) {
            Log.e("printAsmCode -> file not exists (${file.absolutePath})")
            return
        }
        printAsmCodeByStream(file.newInputStream())
    }

    /**
     * 打印给定class内容如何使用asm进行生成
     *
     * 可以根据打印信息作为参考，编写对应的asm代码
     */
    static def printAsmCodeByStream(InputStream inputStream) {
        def classReader = new ClassReader(inputStream)
        printAsmCode(classReader)
    }

    /**
     * 打印给定class内容如何使用asm进行生成
     *
     * 可以根据打印信息作为参考，编写对应的asm代码
     */
    static def printAsmCode(ClassReader cr) {
        def asmifier = new ASMifier()
        def trace = new TraceClassVisitor(null, asmifier, System.out.newPrintWriter())
        cr.accept(trace, ClassReader.EXPAND_FRAMES)
    }

    static boolean hasInterface(InputStream stream, String interfaceName, boolean autoClose = true, Closure findCallback) {

        def classReader = new ClassReader(stream)

        // 输入UseTestDemo的Asm生成代码
//        if (classReader.className.contains("UseTestDemo")) {
//            ASMUtils.printAsmCode(classReader)
//        }

        def interfaces = classReader.interfaces
        def find = interfaces.find { (it == interfaceName) }
        if (find != null && !find.isEmpty()) {
            findCallback.call(classReader.className)
        }
        if (autoClose) {
            stream.close()
        }
    }

    /**
     * 在指定类文件上是否存在某个方法，这个方法被指定注解名字注解
     * @param file 指定类文件
     * @param annotationClass 注解名称
     * @param visible 是否可见：注解为运行期注解为可见注解，lib注解为不可见注解
     * @return 如果有则返回true，否则返回false
     */
    static boolean hasAnnotationOnMethod(File file, String annotationClass, boolean visible) {
        ClassReader cr = new ClassReader(file.newInputStream())
        ClassNode cn = new ClassNode()
        cr.accept(cn, ClassReader.EXPAND_FRAMES)

        def result = false

        cn.methods.each { MethodNode item ->
            def an = item.visibleAnnotations
            if (!visible) {
                an = item.invisibleAnnotations
            }
            if (an != null) {
                an.each { AnnotationNode itemAnnotation ->
                    if (itemAnnotation.desc == annotationClass) {
                        result = true
                    }
                }
            }
        }

        return result
    }

    static void processInput2Output(InputStream inputStream, OutputStream outputStream, Closure visitorCallback) {
        def classReader = new ClassReader(inputStream)
        def classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
        def visitor = visitorCallback.call(classWriter)
        classReader.accept(visitor, ClassReader.EXPAND_FRAMES)
        outputStream.write(classWriter.toByteArray())
    }
}