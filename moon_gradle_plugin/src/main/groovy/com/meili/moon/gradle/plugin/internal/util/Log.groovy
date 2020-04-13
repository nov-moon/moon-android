package com.meili.moon.gradle.plugin.internal.util

class Log {
    static void e(Object obj) {
        println "${Consts.LogTag} -> ${obj}"
    }
}

