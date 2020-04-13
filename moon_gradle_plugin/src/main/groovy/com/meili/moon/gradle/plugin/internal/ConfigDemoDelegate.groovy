package com.meili.moon.gradle.plugin.internal

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceDirectorySet
import com.android.build.gradle.api.AndroidSourceSet
import com.meili.moon.gradle.plugin.MoonToolsExtension
import com.meili.moon.gradle.plugin.internal.util.AndroidUtils
import com.meili.moon.gradle.plugin.internal.util.Log
import com.meili.moon.gradle.plugin.internal.util.Utils
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.StopExecutionException

/**
 * 配置当前Module是否为Demo的功能委托类
 *
 * 1. 根据传入的配置信息，引入对应的Android插件
 * 2. 根据配置信息，决定是否引入kotlin相关插件，这里没有引入kotlin包，是因为kotlin版本可能需要外部定义
 * 3. 添加demo相关资源目录
 * 4. 添加demo的配置声明
 * 5. 在config阶段完成后，根据配置重新修复资源目录
 * 6. 在config阶段完成后，根据配置重新修复依赖配置
 */
class ConfigDemoDelegate {

    private final String demoName = "demo"

    private Project project

    void apply(Project target, MoonToolsExtension sdkExtension) {
        project = target

        if (AndroidUtils.isApplication(project) || AndroidUtils.isLibrary(project)) {
            throw new StopExecutionException("使用MoonTools插件，请移除官方插件：com.android.application和com.android.library")
        }

        def androidPlugin = AndroidUtils.applicationId
        def otherAndroidPlugin = AndroidUtils.libraryId
        if (!sdkExtension.useDemo) {
            androidPlugin = AndroidUtils.libraryId
            otherAndroidPlugin = AndroidUtils.applicationId
        }

        project.apply(plugin: androidPlugin)

        //要放到kotlin插件引入之前，原因是kotlin插件初始化过程中也会注册afterEvaluate，并且在其中配置resource，如果在其后面将导致我们的删除操作无效
        project.afterEvaluate {

            if (project.pluginManager.hasPlugin(otherAndroidPlugin)) {
                throw new StopExecutionException("使用MoonTools插件，请移除官方插件：${otherAndroidPlugin}，可能你在引用moon-tools后仍然引用官方插件造成了这个错误")
            }
            Log.e("afterEvaluate: androidPlugin = ${androidPlugin}, otherAndroidPlugin = ${otherAndroidPlugin}(has:${project.pluginManager.hasPlugin(otherAndroidPlugin)})")

            //重新修复demo的资源目录以及demo的依赖配置
            fixApplication(sdkExtension)
        }


        //增加demo的config
        def configurationContainer = project.getConfigurations()
        Utils.createConfiguration(configurationContainer, demoName, "demo config", false)

        if (sdkExtension.useDemo) {
            //添加必要的依赖声明和demo资源配置
            addApplication(sdkExtension)
        }

        if (sdkExtension.useKotlin) {
            project.apply(plugin: 'kotlin-android')
            project.apply(plugin: 'kotlin-android-extensions')
        }

        // 如果当前module是demo类型，则不能进行maven打包
        project.tasks.getByName("uploadArchives").doFirst {
            if (sdkExtension.useDemo) {
                throw new Exception("当前选项是app，不能进行打包上传")
            }
        }
    }

    private void addApplication(MoonToolsExtension sdkExtension) {

        BaseExtension extension = getAndroidExtension()

        fixDemoDir()

        // 增加demo的src
        AndroidSourceSet sourceSet = extension.getSourceSets().getByName("main")

        addApplicationSourceSet(sourceSet.java, "src/demo/java")

        addApplicationSourceSet(sourceSet.res, "src/demo/res")

        def manifest = sourceSet.manifest
        manifest.srcFile("src/demo/AndroidManifest.xml")

        addDemoDependency(sdkExtension.useDemo)
    }

    // 暂时不做任何事情
    private void fixApplication(MoonToolsExtension sdkExtension) {

//        BaseExtension extension = getAndroidExtension()
//
//        if (!sdkExtension.useDemo) {
//            AndroidSourceSet sourceSet = extension.getSourceSets().getByName("main")
//
//            sourceSet.each {
//                fixApplicationSourceSet(it.java, "src/demo/java")
//                fixApplicationSourceSet(it.res, "src/demo/res")
//            }
//
//            def manifest = sourceSet.manifest
//            manifest.srcFile("src/main/AndroidManifest.xml")
//        }
//
//        addDemoDependency(sdkExtension.useDemo)
    }

    /**
     * 添加demo相关依赖
     */
    private void addDemoDependency(boolean useDemo) {

        def configurationContainer = project.getConfigurations()
        Configuration demoConfig = configurationContainer.getByName(demoName)

        if (!useDemo) return

        BaseExtension extension = getAndroidExtension()

        AndroidSourceSet sourceSet = extension.getSourceSets().getByName("main")

        Configuration implementation = configurationContainer.getByName(sourceSet.getImplementationConfigurationName())
        HashSet extendsFroms = new HashSet<Configuration>()
        extendsFroms.addAll(implementation.getExtendsFrom())
        extendsFroms.add(demoConfig)
        implementation.extendsFrom = extendsFroms
    }

    /**
     * 获取android的extension配置
     */
    private BaseExtension getAndroidExtension() {
        return project.getExtensions().getByName('android')
    }

    /**
     * 将指定目录添加到指定sourceSet中
     */
    private void addApplicationSourceSet(AndroidSourceDirectorySet sourceSet, String path) {
        HashSet resSource = new HashSet<File>()
        resSource.addAll(sourceSet.getSrcDirs())
        resSource.add(path)
        sourceSet.srcDirs = resSource
    }

    /**
     * 去除指定sourceSet中的指定目录
     */
    private static void fixApplicationSourceSet(AndroidSourceDirectorySet sourceSet, String path) {
        def src = sourceSet.getSrcDirs()
        def newSrc = new HashSet()

        src.each {
            if (!it.path.contains(path)) {
                newSrc.add(it)
            }
        }
        sourceSet.setSrcDirs(newSrc)
    }

    private void fixDemoDir() {

        Utils.fixDemoResource(project, "src/demo")

//        def packagePath = Utils.getLibPackage(project).replaceAll("\\.", File.separator)

//        Utils.checkAndCreateDirIfNotExist(project, "src/demo/java/${packagePath}/demo/")
//        Utils.checkAndCreateDirIfNotExist(project, "src/demo/res/drawable/")
//        Utils.checkAndCreateDirIfNotExist(project, "src/demo/res/drawable-xxhdpi/")
//        Utils.checkAndCreateDirIfNotExist(project, "src/demo/res/layout/")
//        Utils.checkAndCreateDirIfNotExist(project, "src/demo/res/values/")
//        Utils.checkDemoManifest(project, "src/demo/AndroidManifest.xml")
    }


}