package com.meili.moon.gradle.plugin.internal

import com.meili.moon.gradle.plugin.MoonToolsExtension
import com.meili.moon.gradle.plugin.internal.util.Log
import com.meili.moon.gradle.plugin.internal.util.Utils
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ModuleDependency

class ConfigAllMavenDelegate {

    private static final String CONFIG_NAME = "projectMaven"
    private static final String FORCE_USAGE_PREFIX = "^"
    private String compileName

    private Project project

    void apply(Project target, MoonToolsExtension extension) {
        project = target
        def configurationContainer = project.getConfigurations()
        def androidSourceSet = Utils.getMainSourceSet(project)

        def config = Utils.createConfiguration(configurationContainer, CONFIG_NAME, "moon config", false)

        compileName = androidSourceSet.getApiConfigurationName()

        project.afterEvaluate {

            // 第一步：处理全局的forceAllMaven
            def forceAllMaven

            try {
                forceAllMaven = project.getRootProject().property("moon.forceAllMaven").toString()
            } catch (Exception exception) {
                forceAllMaven = null
            }
            extension.projectForceAllMaven = forceAllMaven

            def isAllMaven
            try {
                isAllMaven = project.getRootProject().property("moon.isAllMaven").toString()
            } catch (Exception exception) {
                isAllMaven = null
            }
            extension.projectIsAllMaven = isAllMaven

            if (extension.allMaven != 1 && extension.allMaven != 0) {
                extension.allMaven = isAllMaven == "false" ? 1 : 0
            }

            config.getDependencies().all {

                if (forceAllMaven == "true") {
                    addMavenDependency(it, true)
                    return
                } else if (forceAllMaven == "false") {
                    addProjectDependency(it, true)
                    return
                }

                // 第二步：处理Module级别的forceAllMaven
                if (extension.forceAllMaven == 0) {
                    addProjectDependency(it, true)
                    return
                } else if (extension.forceAllMaven == 1) {
                    addMavenDependency(it, true)
                    return
                }

                // 第三步：处理特定依赖、Module中的allMaven、全局的isAllMaven

                if (extension.allMaven == 0) {
                    addMavenDependency(it, false)
                } else {
                    addProjectDependency(it, false)
                }
            }
        }

    }

    private void addMavenDependency(Dependency it, boolean ignorePrefix) {
        addDependency(it, ignorePrefix) { String[] groupArray ->
            String group = groupArray[1]
            if (group.startsWith(FORCE_USAGE_PREFIX)) {
                group = group.substring(1)
            }
            def result = "$group:${it.name}:${it.version}"
            def resultDependency = project.getDependencies().add(compileName, result)
            fixDependency(it, resultDependency)
        }
    }

    private void addProjectDependency(Dependency it, boolean ignorePrefix) {
        addDependency(it, ignorePrefix) { String[] groupArray ->

            def projectStr = groupArray[0]

            if (projectStr.startsWith(FORCE_USAGE_PREFIX)) {
                projectStr = projectStr.substring(1)
            }

            def result = ":$projectStr"

            def resultDependency = project.getDependencies().add(compileName, project.project(result))
            fixDependency(it, resultDependency)
        }
    }

    private void addDependency(Dependency it, boolean ignorePrefix, Closure dependency) {
        def groupArray = it.group.split("\\|")
        if (groupArray.size() != 2) {
            throw Exception("引用配置错误：${it.group}")
        }

        String projectStr = groupArray[0]
        String group = groupArray[1]
        if (ignorePrefix) {
            dependency.call(groupArray)
        } else {
            if (projectStr.startsWith(FORCE_USAGE_PREFIX)) {
                projectStr = projectStr.substring(1)

                def result = ":$projectStr"

                def resultDependency = project.getDependencies().add(compileName, project.project(result))
                fixDependency(it, resultDependency)
            } else if (group.startsWith(FORCE_USAGE_PREFIX)) {
                group = group.substring(1)

                def result = "$group:${it.name}:${it.version}"


                def resultDependency = project.getDependencies().add(compileName, result)
                fixDependency(it, resultDependency)
            } else {
                dependency.call(groupArray)
            }
        }
    }

    private void fixDependency(Dependency from, Dependency to) {

        if (from instanceof ModuleDependency && to instanceof ModuleDependency) {
            def toModule = (ModuleDependency) to
            def fromModule = (ModuleDependency) from
            toModule.setTransitive(fromModule.isTransitive())
            fromModule.excludeRules.each {
                Log.e("${fromModule} : ${ExcludeRule.GROUP_KEY} -> ${it.group}")
                Log.e("${fromModule} : ${ExcludeRule.MODULE_KEY} -> ${it.module}")
                def roles = new HashMap()
                roles.put(ExcludeRule.GROUP_KEY, it.group)
                roles.put(ExcludeRule.MODULE_KEY, it.module)
                toModule.exclude(roles)
            }
        }

    }

}