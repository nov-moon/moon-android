package com.meili.moon.gradle.plugin.internal.permission

import com.meili.moon.gradle.plugin.internal.util.ASMUtils
import com.meili.moon.gradle.plugin.internal.util.Log
import com.meili.moon.gradle.plugin.internal.util.Utils
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

class PermissionAsmProcessor {

    private def targetAnnotation = "Lcom/meili/moon/sdk/permission/Permission;"
    private def ownerClass = "com/meili/moon/sdk/permission/MoonPermissionImpl"
    private def ownerClassInstance = "INSTANCE"
    private def ownerClassInstanceDesc = "Lcom/meili/moon/sdk/permission/MoonPermissionImpl;"
    private def invokeMethodName = "checkPermissionForByteCode"
    private def invokeMethodDesc = "(" +
            "Ljava/lang/Object;" +
            "[Ljava/lang/String;" +
            "[Ljava/lang/String;" +
            "Ljava/lang/String;" +
            "Ljava/lang/String;" +
            "Ljava/lang/String;" +
            "[Ljava/lang/Object;)Z"

    PermissionAsmProcessor() {
    }

    void processClass(InputStream inputStream, OutputStream outputStream) {
        ASMUtils.processInput2Output(inputStream, outputStream) {
            return new FixClassAdapter(it)
        }
    }

    private class FixClassAdapter extends ClassVisitor {

        private def className

        FixClassAdapter(ClassVisitor classVisitor) {
            super(Opcodes.ASM7, classVisitor)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = name
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            def visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            visitMethod = new FixMethodAdapter(api, visitMethod, access, name, descriptor, className)
            return visitMethod
        }

    }

    private class FixMethodAdapter extends AdviceAdapter {

        private PermissionAnnotationVisitor annotationVisitor
        private String name
        private String className

        protected FixMethodAdapter(int api, MethodVisitor mv, int access, String name, String desc, String className) {
            super(api, mv, access, name, desc)
            this.name = name
            this.className = className
        }

        // 此方法在annotation解析完后才回到调用
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter()

            Log.e("FixMethodAdapter!! -> onMethodEnter($methodDesc)")
            if (annotationVisitor == null) {
                Log.e("FixMethodAdapter!! -> 方法 $name 没有找到Annotation")
                return
            }

            Log.e("FixMethodAdapter!! -> 方法 $name 开始插装")

            if (annotationVisitor.must != null) {
                annotationVisitor.must.each { String item ->
                    Log.e("FixMethodAdapter!! -> must($item)")
                }
            }
            if (annotationVisitor.should != null) {
                annotationVisitor.should.each { String item ->
                    Log.e("FixMethodAdapter!! -> should($item)")
                }
            }
            Log.e("FixMethodAdapter!! -> onDeniedMethod($annotationVisitor.onDeniedMethod)")

            Label label0 = new Label()
            visitLabel(label0)
            // 1. 将MNPermission.Instatnce对象推到栈顶，用作后续方法调用的对象，此时栈：INSTANCE
            visitFieldInsn(GETSTATIC, ownerClass, ownerClassInstance, ownerClassInstanceDesc)
            // 2. 将第0个slot，也就是this，推到栈顶，作为后续方法的第一个入参，此时栈：INSTANCE，this
            visitVarInsn(ALOAD, 0)

            // 3. 将must生成数组，并推到栈顶，作为后续方法的第二个参数，此时栈：INSTANCE，this，must
            permissionArrayByteCode(annotationVisitor.must)
            // 4. 将should生成数组，并推到栈顶，作为后续方法的第三个参数，此时栈：INSTANCE，this，must，should
            permissionArrayByteCode(annotationVisitor.should)

            // 5. 将如果onDeniedMethod有值，则将其值推到栈顶，否则将null推到栈顶，作为后续方法的第四个参数，
            // 此时栈：INSTANCE，this，must，should，onDeniedMethod
            if (annotationVisitor.onDeniedMethod != null && annotationVisitor.onDeniedMethod != "") {
                Log.e("FixMethodAdapter!! -> 插入method")
                visitLdcInsn(annotationVisitor.onDeniedMethod)
            } else {
                visitInsn(ACONST_NULL)
                Log.e("FixMethodAdapter!! -> 插入null method")
            }

            // 6. 将当前方法名称和方法描述推到栈顶，作为后续方法的第五、六个参数，此时栈：INSTANCE，this，must，should，onDeniedMethod，currMethod，methodDesc
            visitLdcInsn(name)
            visitLdcInsn(methodDesc)

            // 7. 如果当前方法有入参，则将当前方法的参数生成数组，推到栈顶，如果没有入参，将null推到栈顶，作为后续方法的第七个入参
            // 此时栈：INSTANCE，this，must，should，onDeniedMethod，currMethod，methodDesc，args
            def argumentTypes = Type.getArgumentTypes(methodDesc)
            def argumentsCount = argumentTypes.size()
            if (argumentsCount > 0) {
                Log.e("FixMethodAdapter!! -> 插入$argumentsCount 个arg")
                visitIntInsn(BIPUSH, argumentsCount)
                visitTypeInsn(ANEWARRAY, "java/lang/Object")
                argumentTypes.each { Type type ->
                    Log.e("arguments -> ${type.sort}, opt = ${type.getOpcode(ILOAD)}")
                }
                for (int i = 0; i < argumentsCount; i++) {
                    // 数组插入，先复制栈顶引用，再添加插入index，再添加具体内容，再调用valueof方法包装，最后存到数组中
                    visitInsn(DUP)
                    visitIntInsn(BIPUSH, i)
                    loadArg(i)
                    valueOf(argumentTypes[i])
                    visitInsn(AASTORE)
                }
            } else {
                visitInsn(ACONST_NULL)
                Log.e("FixMethodAdapter!! -> 插入null args")
            }

            // 7. 使用栈中的INSTATNCE作为对象，调用其requestPermission方法，并将栈中其他内容作为参数，调用完成后，将结果存置栈顶
            // 此时栈：true或者false
            visitMethodInsn(INVOKEVIRTUAL, ownerClass, invokeMethodName, invokeMethodDesc, false)

            // 方法标志位
            Label l5 = new Label()

            // 8. 判断栈顶值是否为0
            // 如果不为0：代表为true，即代表requestPermission返回成功，跳转到方法后续逻辑，继续执行
            // 如果为0：代表为false，即代表requestPermission返回失败，则进行向下执行，进行return
            visitJumpInsn(IFNE, l5)
            Label l6 = new Label()
            visitLabel(l6)
            // 9. 退出当前方法
            visitInsn(RETURN)

            // 10. 不退出方法，继续执行
            visitLabel(l5)

            Log.e("FixMethodAdapter!! -> 方法 $name 结束插装")
        }

        private permissionArrayByteCode(String[] permissions) {
            if (permissions != null && permissions.size() > 0) {
                Log.e("FixMethodAdapter!! -> 插入${permissions.size()} permission")

                visitIntInsn(BIPUSH, permissions.size())
                visitTypeInsn(ANEWARRAY, "java/lang/String")
                permissions.eachWithIndex { String item, int index ->
                    visitInsn(DUP)
                    visitIntInsn(BIPUSH, index)
                    visitLdcInsn(item)
                    visitInsn(AASTORE)
                }
            } else {
                Log.e("FixMethodAdapter!! -> 插入null permission")
                visitInsn(ACONST_NULL)
            }
        }

        /**
         * Visits an annotation of this method.
         *
         * @param descriptor the class descriptor of the annotation class.
         * @param visible {@literal true} if the annotation is visible at runtime.
         * @return a visitor to visit the annotation values, or {@literal null} if this visitor is not
         *     interested in visiting this annotation.
         */
        @Override
        AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            Log.e("FixMethodAdapter!! -> visitAnnotation $descriptor")

            AnnotationVisitor visitor = super.visitAnnotation(descriptor, visible)
            if (descriptor == targetAnnotation) {
                def returnType = Type.getReturnType(methodDesc)
                if (returnType != Type.VOID_TYPE) {
                    Utils.throwException("$className.$name()方法上使用了Permission注解，要求此方法返回类型必须为void。" +
                            "如果不能满足此条件，请尝试使用CommonSdk.permission()的api进行直接权限请求，在此方法放弃使用Permission注解")
                }
                visitor = new PermissionAnnotationVisitor(api, visitor)
                annotationVisitor = visitor
            }
            return visitor
        }
    }

    class PermissionAnnotationVisitor extends AnnotationVisitor {

        String[] must = null
        String[] should = null
        String onDeniedMethod = null

        private PermissionAnnotationVisitor mustVisitor = null
        private PermissionAnnotationVisitor shouldVisitor = null
        private List<String> permissions = new ArrayList<>()

        private def annotationArgOnDenied = "onDeniedMethod"
        private def annotationArgName = "name"
        private def annotationArgShould = "should"

        /**
         * Constructs a new {@link AnnotationVisitor}.
         *
         * @param api the ASM API version implemented by this visitor. Must be one of {@link
         *     Opcodes#ASM4}, {@link Opcodes#ASM5}, {@link Opcodes#ASM6} or {@link Opcodes#ASM7}.
         * @param annotationVisitor the annotation visitor to which this visitor must delegate method
         *     calls. May be {@literal null}.
         */
        PermissionAnnotationVisitor(int api, AnnotationVisitor annotationVisitor) {
            super(api, annotationVisitor)
        }

        /**
         * Visits a primitive value of the annotation.
         *
         * @param name the value name.
         * @param value the actual value, whose type must be {@link Byte}, {@link Boolean}, {@link
         *     Character}, {@link Short}, {@link Integer} , {@link Long}, {@link Float}, {@link Double},
         * {@link String} or {@link Type} of {@link Type#OBJECT} or {@link Type#ARRAY} sort. This
         *     value can also be an array of byte, boolean, short, char, int, long, float or double values
         *     (this is equivalent to using {@link #visitArray} and visiting each array element in turn,
         *     but is more convenient).
         */
        @Override
        void visit(String name, Object value) {
            switch (name) {
                case annotationArgOnDenied:
                    onDeniedMethod = value
                    break
                default:
                    permissions.add(value)
                    break
            }
            super.visit(name, value)
        }

        @Override
        AnnotationVisitor visitArray(String name) {
            switch (name) {
                case annotationArgName:
                    mustVisitor = new PermissionAnnotationVisitor(api, super.visitArray(name))
                    return mustVisitor
                case annotationArgShould:
                    shouldVisitor = new PermissionAnnotationVisitor(api, super.visitArray(name))
                    return shouldVisitor
            }
        }

        @Override
        void visitEnd() {
            if (mustVisitor != null) {
                must = mustVisitor.permissions.toArray()
            }
            if (shouldVisitor != null) {
                should = shouldVisitor.permissions.toArray()
            }
            super.visitEnd()
        }
    }
}