package com.meili.moon.gradle.plugin.internal.util

import org.gradle.api.Project

class AndroidUtils {
    def public static applicationId = 'com.android.application'
    def public static libraryId = 'com.android.library'

    static boolean isApplication(Project project) {
        project.pluginManager.hasPlugin(applicationId)
    }

    static boolean isLibrary(Project project) {
        project.pluginManager.hasPlugin(libraryId)
    }
}