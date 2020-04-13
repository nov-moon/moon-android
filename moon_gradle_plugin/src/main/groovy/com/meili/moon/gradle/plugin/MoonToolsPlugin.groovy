package com.meili.moon.gradle.plugin

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.meili.moon.gradle.plugin.internal.ConfigAllMavenDelegate
import com.meili.moon.gradle.plugin.internal.ConfigDemoDelegate
import com.meili.moon.gradle.plugin.internal.util.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * 皓月项目的开发工具
 *
 * MoonTools作为皓月项目的便捷开发工具，主要提供了三种功能：
 *
 * 1. Module的app和lib模式切换，app模式下进行库的功能开发和测试，lib模式下进行库的打包和依赖
 *
 * 2.Project中的全局maven依赖和project依赖切换，支持快速全局切换项目是project依赖还是maven依赖
 *
 * 3.本地maven与公司maven快速切换
 *
 * 可以通过根目录的gradle.properties文件和module中的build.gradle进行功能配置。
 *
 * 在gradle.properties中：
 *
 * #设置全局配置为使用maven
 * #moon.isAllMaven=false
 *
 * #强制项目中所有module的依赖方式，有三种设置方式：true 强制为maven，false 强制为project，不设置 不强制任何方式
 * #moon.forceAllMaven=false
 *
 * 在module中的build.gradle中：
 *
 * 新增moonTools代码块：
 *
 * moonTools {*      //是否使用demo模式，默认为false
 *      useDemo true
 *
 *      //是否使用kotlin插件，默认为true
 *      useKotlin true
 *
 *      //当前项目依赖方式：0 maven依赖(默认)，1 project依赖
 *      allMaven 1
 *
 *      //当前项目依赖方式，如果设置此值，则上面的allMaven会无效，：0 maven依赖，1 project依赖，其他值 等同于没有设置此值
 *      forceAllMaven 1
 *}*
 * 新增两种依赖声明：
 *
 *  // 只在demo中生效的库依赖
 *  demo 'com.android.support.constraint:constraint-layout:1.1.3'
 *
 *  // 声明格式为：projectMaven 'module名称 + 竖线(|) + maven依赖'
 *  projectMaven 'moon_sdk_base|com.meili.moon.sdk:base:1.11.0'
 *
 *  具体使用方式可参考{@link MoonTools文档}
 *
 */
class MoonToolsPlugin implements Plugin<Project> {

    //当前project
    private Project project

    @Override
    void apply(Project target) {

        project = target

        //解析moonTools代码块信息，这里的解析使用了字符串解析，如果修改extension中的字段名称，请注意此处
        MoonToolsExtension sdkExtension = processExtension()

//        Log.e("origin: ${sdkExtension}")

        // 处理App和lib的切换
        (new ConfigDemoDelegate()).apply(project, sdkExtension)
        // 处理Maven和Project依赖方式
        (new ConfigAllMavenDelegate()).apply(project, sdkExtension)

        project.afterEvaluate {
            // 添加MoonTools的检查任务
            project.tasks.create("checkMoonTools") {
                doLast {
                    printSrc()
                    Log.e("end!")
                }
            }
            Log.e("configEnd: ${sdkExtension}")
        }
        //打印kotlin编译源码目录
//        project.afterEvaluate {
//            SourceTask task = project.tasks.getByName("compileDebugKotlin")
//            task.source.each {
//                Log.e("task src = ${it.absolutePath}")
//            }
//        }
    }

    /**
     * 通过解析当前Module下的build.gradle文件，获取当前moonTools中的配置
     */
    private MoonToolsExtension processExtension() {
        MoonToolsExtension sdkExtension = project.extensions.create(MoonToolsExtension, 'moonTools', MoonToolsExtension, project)

        def hasMoonSdk = false
        def startCount = 0
        project.buildscript.getSourceFile().eachLine {
            def line = it.trim()
            if (line == "moonTools {") {
                startCount++
                hasMoonSdk = true
            } else if (hasMoonSdk) {
                if (line == "{") {
                    startCount++
                    hasMoonSdk = startCount > 0
                } else if (line == "}") {
                    startCount--
                    hasMoonSdk = startCount > 0
                } else if (line.startsWith("useDemo")) {
                    sdkExtension.useDemo = line.contains("true")
                } else if (line.startsWith("useKotlin")) {
                    sdkExtension.useKotlin = line.contains("true")
                }
            }
        }

        return sdkExtension
    }

    private void printSrc() {
        Log.e("------------------------------- Start -------------------------------------------------")

//        printKotlinSrc()

        Log.e("································· main ··············································")
        BaseExtension extension = project.getExtensions().getByName('android')

        AndroidSourceSet source = extension.getSourceSets().getByName("main")
        Log.e("sourceSet: java = ${source.java}, res = ${source.res}, resources = ${source.resources}, manifest = ${source.manifest}")

        Log.e("······························ dependency ·················································")
//
        project.getConfigurations().each {
            it.getDependencies().each {
                Log.e("dependency: ${it}")
            }
        }

        Log.e("================================  End  ===============================================")

    }

    private printKotlinSrc() {
        KotlinProjectExtension kotlin = project.getExtensions().getByName("kotlin")

        kotlin.sourceSets.each { KotlinSourceSet sourceSet ->
            Log.e(sourceSet.kotlin.name)

            sourceSet.kotlin.sourceDirectories.each {
                if (it.absolutePath.contains("demo")) {
                    Log.e("kotlin: sourceDirectories = ${it.absolutePath}")
                }
            }
            sourceSet.kotlin.srcDirs.each {
                if (it.absolutePath.contains("demo")) {
                    Log.e("kotlin: srcDirs = ${it.absolutePath}")
                }
            }
            sourceSet.kotlin.srcDirTrees.each {

                if (it.dir.exists()) {
                    it.dir.eachFileRecurse {
                        if (it.absolutePath.contains("demo")) {
                            Log.e("kotlin: srcDirTrees = ${it.absolutePath}")
                        }
                    }
                }
            }
            Log.e(sourceSet.resources.name)

            sourceSet.resources.sourceDirectories.each {
                if (it.absolutePath.contains("demo")) {
                    Log.e("resources: sourceDirectories = ${it.absolutePath}")
                }
            }
            sourceSet.resources.srcDirs.each {
                if (it.absolutePath.contains("demo")) {
                    Log.e("resources: srcDirs = ${it.absolutePath}")
                }
            }
            sourceSet.resources.srcDirTrees.each {
                if (it.dir.exists()) {
                    it.dir.eachFileRecurse {
                        if (it.absolutePath.contains("demo")) {
                            Log.e("resources: srcDirTrees = ${it.absolutePath}")
                        }
                    }
                }
            }
        }
    }
}
