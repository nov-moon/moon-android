package com.meili.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Author： fanyafeng
 * Date： 2019/2/26 5:52 PM
 * Email: fanyafeng@live.cn
 */
//@AutoService(Processor.class)
public class ComponentProcessor extends AbstractProcessor {

    /**
     * 文件相关的辅助类
     */
    private Filer mFiler;

    /**
     * 元素相关的辅助类
     */
    private Elements mElementUtils;

    /**
     * 日志相关的辅助类
     */
    private Messager mMessager;

    /**
     * 方法的名称
     */
    private final String METHOD_NAME = "initComponent";

    /**
     * 包名自己配置
     */
    private final String PACKAGE_NAME = "com.meili.annotationstudy01";

    /**
     * 类的名称
     */
    private final String CLASS_NAME = "Component";


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.size() == 0) {
            return true;
        }
        System.out.println("----------- " + ComponentProcessor.class.getSimpleName() + "开始运行  -----------");

        ArrayList<String> fullNameList = new ArrayList<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Component.class)) {
            //得到类的全路径
            String fullName = mElementUtils.getBinaryName((TypeElement) element).toString();
            fullNameList.add(fullName);
        }
        generateCode(fullNameList);

        System.out.println("----------- " + ComponentProcessor.class.getSimpleName() + "结束运行  -----------");
        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, CLASS_NAME + " generated");

        return false;
    }

    /**
     * 创建代码
     *
     * @param fullNameList
     */
    private void generateCode(ArrayList<String> fullNameList) {

        ClassName applicationTypeName = ClassName
                .get("android.app", "Application");
        // 创建request函数
        MethodSpec.Builder methodSpecBuilder = MethodSpec
                .methodBuilder(METHOD_NAME)
                .addParameter(applicationTypeName, "application")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (String fullName : fullNameList) {
            methodSpecBuilder.addStatement("$N.INSTANCE.init(application)", fullName);
            System.out.println(fullName);
        }
        MethodSpec methodSpec = methodSpecBuilder.build();

        // 创建Pid类
        TypeSpec typeSpec = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(methodSpec).build();

        JavaFile javaFile = JavaFile
                .builder(PACKAGE_NAME, typeSpec)
                .build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Component.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
