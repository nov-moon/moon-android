package com.meili.moon.gradle.plugin

import com.meili.moon.gradle.plugin.internal.KitTransform
import com.meili.moon.gradle.plugin.internal.PermissionTransform
import com.meili.moon.gradle.plugin.internal.util.AndroidUtils
import com.meili.moon.gradle.plugin.internal.util.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 皓月Kit库插件
 *
 * kit库插件主要简化皓月库的接入成本。
 * 1. 在编译阶段kit插件会自动注册IComponent接口实例，并进行初始化。
 *      kit插件会在编译期遍历所有当前环境下的类，如果发现是IComponent的子类，则会在DefComponentInstaller中进行代码添加。
 *      具体的遍历规则请参见KitTransform类
 *
 */
class MoonKitPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = Utils.getAndroidExtension(project)
        if (AndroidUtils.isApplication(project)) {
            extension.registerTransform(new KitTransform())
        }
        extension.registerTransform(new PermissionTransform())
    }

}