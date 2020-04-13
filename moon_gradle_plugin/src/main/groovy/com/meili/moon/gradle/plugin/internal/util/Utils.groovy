package com.meili.moon.gradle.plugin.internal.util

import com.android.annotations.NonNull
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer

import java.util.jar.JarEntry
import java.util.jar.JarFile

class Utils {

    static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty()
    }

    static boolean hasEmpty(String... str) {
        str.each {
            if (isEmpty(it)) {
                return true
            }
        }
        return false
    }


    /**
     * Creates a Configuration for a given source set.
     *
     * @param configurations the configuration container to create the new configuration
     * @param name the name of the configuration to create.
     * @param description the configuration description.
     * @param canBeResolved Whether the configuration can be resolved directly.
     * @return the configuration
     *
     * @see org.gradle.api.artifacts.Configuration#isCanBeResolved()
     */
    static Configuration createConfiguration(
            @NonNull ConfigurationContainer configurations,
            @NonNull String name,
            @NonNull String description,
            boolean canBeResolved) {

        Configuration configuration = configurations.findByName(name);
        if (configuration == null) {
            configuration = configurations.create(name);
        }
        configuration.setVisible(false);
        configuration.setDescription(description);
        configuration.setCanBeConsumed(false);
        configuration.setCanBeResolved(canBeResolved);

        return configuration;
    }

    static BaseExtension getAndroidExtension(project) {
        return project.getExtensions().getByName('android')
    }

    static AndroidSourceSet getMainSourceSet(project) {
        def extension = getAndroidExtension(project)
        def sourceSet = extension.getSourceSets().getByName("main")
        return sourceSet
    }

    static String getLibPackage(Project project) {

        def android = getAndroidExtension(project)
        def main = android.sourceSets.getByName("main")

        def customers = new XmlParser().parse(main.manifest.srcFile)
        def packageStr = customers.attribute("package")

        return packageStr
    }

    static void fixDemoResource(Project project, String path) {
        def targetFile = new File(project.projectDir, path)
        if (targetFile.exists()) {
            Log.e("demo文件夹已存在，暂不处理")
            return
        }

        targetFile.mkdirs()

        def mainActivity = "MainActivity.demo"
        def manifest = "AndroidManifest.xml"
        def strings = "values/strings.xml"
        def demoApp = "DemoApp.demo"
        def res = "init/"
        def libPackage = getLibPackage(project)
        def demoPackage = libPackage + ".demo"

        readResource(res) { JarEntry entry, String name, InputStream stream ->
            File itemFile
            if (entry.isDirectory()) {
                itemFile = new File(targetFile, name.substring(res.length()))
                itemFile.mkdirs()
            } else if (name.contains(mainActivity) || name.contains(demoApp)) {
                def packageStr = "java.${demoPackage}."
                def packagePath = packageStr.replaceAll("\\.", File.separator)
                itemFile = new File(targetFile, packagePath)
                if (!itemFile.exists()) {
                    itemFile.mkdirs()
                }
                def fileName = name.substring(path.lastIndexOf("/") + 1)
                        .replace(".demo", ".kt")
                itemFile = new File(itemFile, fileName)
                itemFile.createNewFile()

                itemFile.withWriter { writer ->
                    stream.eachLine { line, index ->
                        if (index == 1) {
                            line = "package ${demoPackage}"
                        }
                        writer.writeLine(line)
                    }
                }
            } else if (name.contains(manifest)) {
                def result = stream.text.replace("\$\$package\$\$", libPackage)
                itemFile = new File(targetFile, manifest)
                itemFile.createNewFile()
                itemFile.withWriter { writer ->
                    writer.write(result)
                }
            } else if (name.contains(strings)) {
                def appName = libPackage.substring(libPackage.lastIndexOf(".") + 1)
                appName = appName.substring(0, 1).toUpperCase() + appName.substring(1)

                def result = stream.text.replace("\$\$AppName\$\$", appName + "Demo")
                itemFile = new File(targetFile, name.substring(res.length()))
                itemFile.createNewFile()
                itemFile.withWriter { writer ->
                    writer.write(result)
                }
            } else {
                itemFile = new File(targetFile, name.substring(res.length()))
                itemFile.createNewFile()
                itemFile.withOutputStream { writer ->
                    stream.eachByte(512) { byte[] buffer, int len ->
                        writer.write(buffer, 0, len)
                    }
                }
            }

            stream.close()
        }
    }

    static void readResource(String dirPath, Closure process) {
        URL url = Utils.class.getClassLoader().getResource(dirPath)

        String jarPath = url.toString().substring(0, url.toString().indexOf("!/") + 2)

        URL jarURL = new URL(jarPath)
        JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection()
        JarFile jarFile = jarCon.getJarFile()
        Enumeration<JarEntry> jarEntries = jarFile.entries()

        while (jarEntries.hasMoreElements()) {
            JarEntry entry = jarEntries.nextElement()
            String name = entry.getName()
            if (name.startsWith(dirPath)) {
                def stream = Utils.class.getClassLoader().getResourceAsStream(name)
                process.call(entry, name, stream)
            }
        }
        jarFile.close()
    }

    def throwException(String msg) {
        throw new Exception(msg)
    }
}