package com.meili.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Author： fanyafeng
 * Date： 2019/2/26 4:45 PM
 * Email: fanyafeng@live.cn
 */
@AutoService(Processor.class)
public class PageNameProcessor extends AbstractProcessor {

    /**
     * Tag
     */
    private static String TAG = PageNameProcessor.class.getSimpleName();
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


    private String packageName = IPageConfig.PACKAGE_NAME;

    /**
     * 类的名称
     */
    private final String CLASS_NAME = "PageConfig";

    /**
     * 生成的类名称
     */
    private String className = CLASS_NAME;

    /**
     * 方法的名称
     */
    private final String METHOD_NAME = "getPages";

    private final String METHOD_AFFINITY = "getAffinity";

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
            mMessager.printMessage(Diagnostic.Kind.NOTE, "empty page names");
            return true;
        }

//        PrintTxt.printLog("----------- " + TAG + "开始运行  -----------");
        for (Element element : roundEnvironment.getElementsAnnotatedWith(PageConfigParams.class)) {
            PageConfigParams annotation = element.getAnnotation(PageConfigParams.class);

            String prefix = annotation.pageConfigPrefixName();

            if (!prefix.isEmpty()) {
                className = prefix + CLASS_NAME;
            }
        }

        List<PageConfigModel> pageNames = new ArrayList<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(PageName.class)) {
            PageName annotation = element.getAnnotation(PageName.class);
            String nameStr = annotation.name();

            if (isEmpty(nameStr)) {
                nameStr = annotation.value();
            }

//            PrintTxt.printLog(TAG + " nameStr:" + nameStr);

            String affinityId = annotation.affinity();

            String binaryName = mElementUtils.getBinaryName((TypeElement) element).toString();

            String noteStr = annotation.note();
            if (isEmpty(noteStr)) {
                noteStr = "参见类：" + binaryName;
            }
//            PrintTxt.printLog(TAG + " noteStr:" + noteStr);

//            PrintTxt.printLog(TAG + " binaryName:" + binaryName);

            List<String> interceptors = null;
            try {
                Class<?>[] array = annotation.interceptors();
            } catch (MirroredTypeException e) {
//                interceptors = new String[1];
//                interceptors[0] = e.getTypeMirror().toString();
            } catch (MirroredTypesException e) {

                // 在apt的时候，拿不到class对象，暂且使用从exception中获取class名称
                List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
                interceptors = new ArrayList<>();
                for (int i = 0; i < typeMirrors.size(); i++) {
                    String s = typeMirrors.get(i).toString();
                    if (s.equals("java.lang.Object")) {
                        continue;
                    }
                    interceptors.add(s);
                }
            }

            if (interceptors == null) {
                interceptors = new ArrayList<>();
            }

            String[] result = new String[interceptors.size()];
            interceptors.toArray(result);

            PageConfigModel pageNameModel = new PageConfigModel(nameStr, affinityId, noteStr, binaryName, result);

            if (isEmpty(nameStr)) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, binaryName + "类上的PageName注解为空");
            } else {
                mMessager.printMessage(Diagnostic.Kind.NOTE, pageNameModel.toString());
            }

            pageNames.add(pageNameModel);
        }

        generateCode(pageNames);

        processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, className + " generated");
        return false;
    }

    private void generateCode(List<PageConfigModel> list) {

        //创建类
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(IPageConfig.class);

        String mDataName = "PAGE_LIST";

        FieldSpec.Builder mDataBuilder = FieldSpec.builder(ParameterizedTypeName.get(List.class, PageConfigModel.class),
                mDataName, Modifier.PRIVATE, Modifier.STATIC);

        FieldSpec mPageListField = mDataBuilder.build();
        classBuilder.addField(mPageListField);

        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();

        staticBlockBuilder.addStatement(mDataName + " = new $T<>()", ArrayList.class);

        //创建成员变量
        Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};

        for (PageConfigModel item : list) {
            String fieldName = item.getPageName();
            if (isEmpty(fieldName)) continue;
//            PrintTxt.printLog("fieldName:" + fieldName);
            FieldSpec fieldSpec = FieldSpec
                    .builder(String.class, fieldName.toUpperCase().replace("/", "_"), modifiers)
                    .initializer("$S", fieldName)
                    .addJavadoc("{@link $N} \n$N\n", item.getClassName(), item.getNote())
                    .build();
            classBuilder.addField(fieldSpec);

            staticBlockBuilder.add("\n");

            char[] chars = fieldName.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '/' && i < chars.length - 1) {
                    chars[i + 1] = Character.toUpperCase(chars[i + 1]);
                }
            }

            String arrayName = (new String(chars)).replaceAll("/", "") + "Array";

            String[] interceptors = item.getInterceptors();
            if (interceptors != null && interceptors.length > 0) {
                staticBlockBuilder.addStatement("String[] $L = new String[$L]", arrayName, interceptors.length);

                for (int i = 0; i < interceptors.length; i++) {
                    staticBlockBuilder.addStatement("$L[$L] = $S", arrayName, i, interceptors[i]);
                }
            } else {
                staticBlockBuilder.addStatement("String[] $L = null", arrayName);
            }

            staticBlockBuilder.addStatement(mDataName + ".add(new $T($N, $S, $S, $S, $L))",
                    PageConfigModel.class, fieldSpec, item.getAffinityId(), item.getNote(), item.getClassName(), arrayName);

        }

        classBuilder.addStaticBlock(staticBlockBuilder.build());

        // 生成getPage方法
        MethodSpec.Builder getPageBuilder = MethodSpec
                .methodBuilder(METHOD_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, PageConfigModel.class));

        getPageBuilder.addStatement("return $N", mPageListField);

        classBuilder.addMethod(getPageBuilder.build());

        TypeSpec typeSpec = classBuilder.build();

        JavaFile javaFile = JavaFile
                .builder(packageName, typeSpec)
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
        types.add(PageName.class.getCanonicalName());
        types.add(PageConfigParams.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private boolean isEmpty(String text) {
        return text == null || text.trim().equals("");
    }

    private Object getClassFromAnnotation(Element key) {
        List<? extends AnnotationMirror> annotationMirrors = key.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            if (PageName.class.getName().equals(annotationMirror.getAnnotationType().toString())) {
                Set<? extends ExecutableElement> keySet = annotationMirror.getElementValues().keySet();
                for (ExecutableElement executableElement : keySet) {
                    if (Objects.equals(executableElement.getSimpleName().toString(), "value")) {
                        return annotationMirror.getElementValues().get(executableElement).getValue();
                    }
                }
            }
        }
        return null;
    }
}
