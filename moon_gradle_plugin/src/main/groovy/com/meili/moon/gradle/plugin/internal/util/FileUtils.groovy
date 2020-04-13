package com.meili.moon.gradle.plugin.internal.util

class FileUtils {

    static boolean replace(String to, String from) {
        def fromFile = new File(from)
        def toFile = new File(to)
        if (!fromFile.exists() || !toFile.exists()) {
            Log.e("FileUtils -> failed(from:${fromFile.size()}|$fromFile.absolutePath  to:${toFile.size()}|$toFile.absolutePath)")
            return false
        }

        toFile.delete()

        copyFileOnly(fromFile, toFile)

        return true
    }

    static boolean copyFile(String from, String to) {

        def fromFile = new File(from)

        def toFile = new File(to)
        if (to.endsWith(".jar")) {
            copyFileOnly(fromFile, toFile)
            return true
        }
        if (!toFile.exists()) {
            toFile.mkdirs()
        }

        if (!fromFile.exists()) {
            return false
        }

        def listFiles = fromFile.listFiles()

        listFiles.each { file ->

            if (file.isFile()) {
                def target = new File(to + "/" + file.getName())
                copyFileOnly(file, target)
            } else {
                copyFile(file.getPath(), to + "/" + file.getName())
            }
        }

        return true
    }

    static copyFileOnly(File fromFile, File toFile) {
        if (!toFile.exists()) {
            toFile.createNewFile()
        }

        toFile.withOutputStream { writer ->
            fromFile.withInputStream { input ->
                input.eachByte(512) { byte[] buffer, int len ->
                    writer.write(buffer, 0, len)
                }
            }
        }
    }

}