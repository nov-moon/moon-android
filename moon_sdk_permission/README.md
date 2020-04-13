# moon_sdk_permission使用文档
动态权限申请框架分为两种使用方式：注解方式、直接调用方式。

## 引入
    api "com.meili.moon.sdk:permission:1.2"

## 代码示例
直接使用（kotlin）

```kotlin
    fun location(a: IntArray) {
        val toast = Toast.makeText(this, "（[Int）:（$a）", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.BOTTOM, 0, 0)
        toast.show()
    }

    fun onFail(per:List<PermissionItem>){
        per.forEach {
            Log.e("fail",it.Permission )
        }
    }

    fun requestLocation(){

        CommonSdk.permission().requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            callback = object : PermissionCallback {
                override fun onFinish(allGranted: Boolean, grant: List<String>, deny: List<String>){                                                                                                                                                        })
                    if (isGrant){
                         location(intArrayOf(1,2))
                    }else{
                        onFail(permissions)
                    }
        }
    }
```

注解方式（kotlin）

```kotlin
@NeedPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,method = "onFail")
    fun location(a: IntArray) {
        val toast = Toast.makeText(this, "（[Int）:（$a）", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.BOTTOM, 0, 0)
        toast.show()
    }

    fun onFail(grant:List<String>,deny:List<String>){
        deny.forEach {
            Log.e("fail",it )
        }
    }
```


## 注解方式使用说明
使用注解方式做权限申请必须搭配permission_plugin插件使用，permission_plugin插件负责在编译期做字节码插桩。示例：在根build.gradle添加


```
buildscript {

    repositories {
        maven { url "http://maven.youjie.com/content/groups/public/" }
    }
    dependencies {
        classpath "com.meili.moon:permission-plugin:1.0"
    } 
}
```
并在app的build.gradle中添加下面代码
`apply plugin: 'com.meili.moon.permission'`

注意事项：
1. 被NeedPermission注解的方法返回类型一定是void。
2. NeedPermission注解中method字段代表失败时调用的方法名称，这个方法必须是当前类的方法，且参数列表返回类型必须与示例一致。


## 注解方式原理介绍
我们选择在编译时分析每个类文件，找到含有NeedPermission注解的方法。用ASM框架做字节码插桩，以示例来分析，下面是插桩后的class文件反编译出的结果。我们会将相关信息收集，并传递给MNPermission，用于申请权限结束后的回调。


```java
@NeedPermission(
      method = "onfail",
      value = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}
   )
   public final void location(@NotNull int[] a) {
      ArrayList var10004 = new ArrayList();
      var10004.add(a);
      if (MNPermission.doCheckPermission(this, "android.permission.ACCESS_FINE_LOCATION;android.permission.ACCESS_COARSE_LOCATION", "location", "([I)V", var10004, "onfail")) {
         Intrinsics.checkParameterIsNotNull(a, "a");
         Toast toast = Toast.makeText((Context)this, (CharSequence)("（[Int）:（" + a + '）'), 1);
         toast.setGravity(80, 0, 0);
         toast.show();
      }
   }
```


## 注解方式效率分析
使用注解方式会影响编译时速度和运行时效率。
编译时：我们会额外分析每个class文件的结构。感觉不明显。
运行时：我们使用反射方式来调起目标方法。

