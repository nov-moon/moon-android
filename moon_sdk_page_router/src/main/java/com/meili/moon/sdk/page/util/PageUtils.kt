package com.meili.moon.sdk.page.util

// Scanner, find out class with any conditions, copy from google source code.

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.util.app
import com.meili.moon.sdk.util.isDebug
import dalvik.system.DexFile
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


private const val EXTRACTED_NAME_EXT = ".classes"
private const val EXTRACTED_SUFFIX = ".zip"

private val SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes"

private const val PREFS_FILE = "multidex.version"
private const val KEY_DEX_NUMBER = "dex.number"

private const val VM_WITH_MULTIDEX_VERSION_MAJOR = 2
private const val VM_WITH_MULTIDEX_VERSION_MINOR = 1


/**
 * 通过指定包名，扫描包下面包含的所有的ClassName
 *
 * @param context     U know
 * @param packageName 包名
 * @return 所有class的集合
 */
@Throws(PackageManager.NameNotFoundException::class, IOException::class, InterruptedException::class)
fun Package.getWrapFiles(): Set<String> {

    val packageName = this.name

    val classNames = HashSet<String>()

    val paths = getSourcePaths()
//    val parserCtl = CountDownLatch(paths.size)

    for (path in paths) {
        var dexfile: DexFile? = null
        try {
            if (path.endsWith(EXTRACTED_SUFFIX)) {
                //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                dexfile = DexFile.loadDex(path, "$path.tmp", 0)
            } else {
                dexfile = DexFile(path)
            }

            val dexEntries = dexfile!!.entries()
            while (dexEntries.hasMoreElements()) {
                val className = dexEntries.nextElement()
                if (className.startsWith(packageName)) {
                    classNames.add(className)
                }
            }
        } catch (ignore: Throwable) {
            Log.e("ARouter", "Scan map file in dex files made error.", ignore)
        } finally {
            if (null != dexfile) {
                try {
                    dexfile.close()
                } catch (ignore: Throwable) {
                }

            }

//            parserCtl.countDown()
        }
    }

//    parserCtl.await()

    ("Filter " + classNames.size + " classes by packageName <" + packageName + ">").log()
    return classNames
}

/**
 * get all the dex path
 *
 * @param context the application context
 * @return all the dex path
 * @throws PackageManager.NameNotFoundException
 * @throws IOException
 */
@Throws(PackageManager.NameNotFoundException::class, IOException::class)
fun getSourcePaths(): List<String> {
    val applicationInfo = app.packageManager.getApplicationInfo(app.packageName, 0)
    val sourceApk = File(applicationInfo.sourceDir)

    val sourcePaths = ArrayList<String>()
    sourcePaths.add(applicationInfo.sourceDir) //add the default apk path

    //the prefix of extracted file, ie: test.classes
    val extractedFilePrefix = sourceApk.name + EXTRACTED_NAME_EXT

    // 如果VM已经支持了MultiDex，就不要去Secondary Folder加载 Classesx.zip了，那里已经么有了
    // 通过是否存在sp中的multidex.version是不准确的，因为从低版本升级上来的用户，是包含这个sp配置的
    if (!isVMMultiDexCapable) {
        //the total dex numbers
        val totalDexNumber = getMultiDexPreferences(app).getInt(KEY_DEX_NUMBER, 1)
        val dexDir = File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME)

        for (secondaryNumber in 2..totalDexNumber) {
            //for each dex file, ie: test.classes2.zip, test.classes3.zip...
            val fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX
            val extractedFile = File(dexDir, fileName)
            if (extractedFile.isFile) {
                sourcePaths.add(extractedFile.absolutePath)
                //we ignore the verify zip part
            } else {
                throw IOException("Missing extracted secondary dex file '" + extractedFile.path + "'")
            }
        }
    }

    if (isDebug) { // Search instant run support only debuggable
        sourcePaths.addAll(tryLoadInstantRunDexFile(applicationInfo))
    }
    return sourcePaths
}

/**
 * Identifies if the current VM has a native support for multidex, meaning there is no need for
 * additional installation by this library.
 *
 * @return true if the VM handles multidex
 */
private val isVMMultiDexCapable: Boolean
    get() {
        var isMultiDexCapable = false
        var vmName: String? = null

        try {
            vmName = "'Android'"
            val versionString = System.getProperty("java.vm.version")
            if (versionString != null) {
                val matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString)
                if (matcher.matches()) {
                    try {
                        val major = Integer.parseInt(matcher.group(1))
                        val minor = Integer.parseInt(matcher.group(2))
                        isMultiDexCapable = major > VM_WITH_MULTIDEX_VERSION_MAJOR || major == VM_WITH_MULTIDEX_VERSION_MAJOR && minor >= VM_WITH_MULTIDEX_VERSION_MINOR
                    } catch (ignore: NumberFormatException) {
                    }

                }
            }
        } catch (ignore: Exception) {
        }

        ("VM with name " + vmName + if (isMultiDexCapable) " has multidex support" else " does not have multidex support").log()
        return isMultiDexCapable
    }


private fun getMultiDexPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_FILE, if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) Context.MODE_PRIVATE else Context.MODE_PRIVATE or Context.MODE_MULTI_PROCESS)
}

/**
 * Get instant run dex path, used to catch the branch usingApkSplits=false.
 */
private fun tryLoadInstantRunDexFile(applicationInfo: ApplicationInfo): List<String> {
    val instantRunSourcePaths = ArrayList<String>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != applicationInfo.splitSourceDirs) {
        // add the split apk, normally for InstantRun, and newest version.
        instantRunSourcePaths.addAll(Arrays.asList(*applicationInfo.splitSourceDirs))
        "Found InstantRun support".log()
    } else {
        try {
            // This man is reflection from Google instant run sdk, he will tell me where the dex files go.
            val pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths")
            val getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String::class.java)
            val instantRunDexPath = getDexFileDirectory.invoke(null, applicationInfo.packageName) as String

            val instantRunFilePath = File(instantRunDexPath)
            if (instantRunFilePath.exists() && instantRunFilePath.isDirectory) {
                val dexFile = instantRunFilePath.listFiles()
                for (file in dexFile) {
                    if (null != file && file.exists() && file.isFile && file.name.endsWith(".dex")) {
                        instantRunSourcePaths.add(file.absolutePath)
                    }
                }
                "Found InstantRun support".log()
            }

        } catch (e: Exception) {
            ("InstantRun support error, " + e.message).log()
        }

    }

    return instantRunSourcePaths
}