package com.meili.moon.gradle.plugin.internal.kit

import com.meili.moon.gradle.plugin.internal.util.ASMUtils
import com.meili.moon.gradle.plugin.internal.util.Log
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

class KitAsmProcessor implements TransformDelegate.BytecodeProcessor {

    def processTargetMethodName = "installComponents"
    def processClassPath = ""

    // 记录的所有目标类的类全名
    private def targetList = new ArrayList<String>()

    // 我们的目标类的接口全名，目标类需要继承此接口
    private def componentInterface = "com/meili/moon/sdk/IComponent"

    KitAsmProcessor(String processClassPath) {
        this.processClassPath = processClassPath
    }


    @Override
    void scanClass(InputStream stream) {
        ASMUtils.hasInterface(stream, componentInterface) {
            targetList.add(it)
        }
    }

    @Override
    void processClass(InputStream inputStream, OutputStream outputStream) {
        ASMUtils.processInput2Output(inputStream, outputStream) {
            return new FixClassAdapter(it)
        }
    }

    @Override
    boolean validate() {
        return targetList.size() > 0
    }

    private class FixClassAdapter extends ClassVisitor {

        FixClassAdapter(ClassVisitor classVisitor) {
            super(Opcodes.ASM7, classVisitor)
        }

        /**
         * Visits a method of the class. This method <i>must</i> return a new {@link org.objectweb.asm.MethodVisitor}
         * instance (or {@literal null}) each time it is called, i.e., it should not return a previously
         * returned visitor.
         *
         * @param access the method's access flags (see {@link Opcodes}). This parameter also indicates if
         *     the method is synthetic and/or deprecated.
         * @param name the method's name.
         * @param descriptor the method's descriptor (see {@link org.objectweb.asm.Type}).
         * @param signature the method's signature. May be {@literal null} if the method parameters,
         *     return type and exceptions do not use generic types.
         * @param exceptions the internal names of the method's exception classes (see {@link
         *     Type#getInternalName()}). May be {@literal null}.
         * @return an object to visit the byte code of the method, or {@literal null} if this class
         *     visitor is not interested in visiting the code of this method.
         */
        @Override
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            def visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (name == processTargetMethodName) {
                visitMethod = new FixMethodAdapter(api, visitMethod, access, name, descriptor)
            }
            return visitMethod
        }
    }

    private class FixMethodAdapter extends AdviceAdapter {


        /**
         * Creates a new {@link AdviceAdapter}.
         *
         * @param api
         *            the ASM API version implemented by this visitor. Must be one
         *            of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
         * @param mv
         *            the method visitor to which this adapter delegates calls.
         * @param access
         *            the method's access flags (see {@link Opcodes}).
         * @param name
         *            the method's name.
         * @param desc
         *            the method's descriptor (see {@link org.objectweb.asm.Type Type}).
         */
        protected FixMethodAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
            super(api, mv, access, name, desc)
        }

        @Override
        protected void onMethodEnter() {
            super.onMethodEnter()

            def listType = Type.getType(List.class)

            targetList.each {
                Log.e("targetClass = ${it}")

                try {
                    Label label = new Label()
                    visitLabel(label)

                    def itemType = Type.getObjectType(it)
                    visitFieldInsn(GETSTATIC, processClassPath, "componentList", listType.descriptor)
                    visitFieldInsn(GETSTATIC, itemType.internalName, "INSTANCE", itemType.descriptor)
                    visitMethodInsn(INVOKEINTERFACE, listType.internalName, "add", "(Ljava/lang/Object;)Z", true)
                    visitInsn(POP)
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }
}