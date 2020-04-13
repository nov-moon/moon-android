package com.meili.moon.gradle.plugin


import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.invocation.DefaultGradle

import javax.inject.Inject

/**
 * MoonTools中的Extension对象
 *
 * 主要用来接收build.gradle中的moonTools代码块中的配置。
 *
 * 他提供的配置形式如下：
 *
 * moonTools {
 *      //是否使用demo模式，默认为false
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
 * }
 *
 */
class MoonToolsExtension {
    //当前是否为demo，默认不是
    boolean useDemo = false
    //当前项目是否引入kotlin插件，默认引入
    boolean useKotlin = true
//    NamedDomainObjectContainer<AndroidSourceSet> demoSourceSet
    MavenExtension maven
    /**
     * 当前项目依赖方式：0 maven依赖(默认)，1 project依赖。其他值都认为是maven依赖
     */
    int allMaven = -1

    /**
     * 当前项目依赖方式，如果设置此值，则上面的allMaven会无效，：0 maven依赖，1 project依赖，其他值 等同于没有设置此值
     */
    int forceAllMaven = -1

    //内部属性，外部设置无效
    String projectIsAllMaven = null
    //内部属性，外部设置无效
    String projectForceAllMaven = null


    @Inject
    MoonToolsExtension(Project project) {
//        demoSourceSet = project.container( AndroidSourceSet) {
//            project.objects.newInstance(DefaultAndroidSourceSet, "demoSourceSet", project, true)
//        }

        Instantiator instantiator = ((DefaultGradle) project.getGradle()).getServices().get(Instantiator)

        maven = instantiator.newInstance(MavenExtension)
    }
//
//    void demoSourceSet(Action<? super NamedDomainObjectContainer<AndroidSourceSet>> action) {
//        action.execute(sourceSet)
//    }

    void maven(Action<? super MavenExtension> action) {
        action.execute(maven)
    }


    @Override
    public String toString() {
        return "MoonToolsExtension{" +
                "useDemo=" + useDemo +
                ", useKotlin=" + useKotlin +
                ", maven=" + maven +
                ", allMaven=" + allMaven +
                ", forceAllMaven=" + forceAllMaven +
                ", projectIsAllMaven='" + projectIsAllMaven + '\'' +
                ", projectForceAllMaven='" + projectForceAllMaven + '\'' +
                '}';
    }
}