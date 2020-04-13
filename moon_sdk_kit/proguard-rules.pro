#Android官方地址：http://developer.android.com/guide/developing/tools/proguard.html
#介绍所有的混淆配置官方文档：https://www.guardsquare.com/en/proguard/manual/usage

# 过滤器定义
#---------------------------------------------------------------------------------------------------
#
#   修饰符：public、protected、private
#       在类、成员变量、函数前，可以加上访问限定符，表示匹配内容必须为制定修饰符修饰的内容
#       在修饰符前可以使用（"!"）表示相反，即必须不包含修饰符的内容
#
#---------------------------------------------------------------------------------------------------
#
#   类限定：class * extends android.content.Context
#
#       类型定义
#           class       表示任何接口类、抽象类和普通类；   不可使用（“ ! ”）
#           interface   表示只能是接口类；               可使用（“ ! ”），表示非接口的类
#           enum        表示只能是枚举类。               可使用（“ ! ”），表示非枚举的类
#
#       类名定义
#           对于类名（ classname ）来说，可以是类全名，或者使用通配符（通配符表1、2）
#
#       继承关系定义
#           extends         继承指定类
#           implements      实现指定接口
#
#---------------------------------------------------------------------------------------------------
#
#   构造方法：public (android.content.Context, android.util.AttributeSet, int, int);
#
#       用 <init> 加上构造函数的参数来指定，参数类型可用（通配符表1、2、3）匹配
#
#---------------------------------------------------------------------------------------------------
#
#   成员变量：public String name;
#
#       fieldtype   变量类型        可用（通配符表1、2、3）匹配
#       fieldname   变量名称        可用（通配符表1）匹配
#       <fields>    表示类中的任何成员变量
#
#---------------------------------------------------------------------------------------------------
#
#   成员方法：public void *(android.view.View);
#
#       returntype      返回类型    可用（通配符表1、2、3）匹配
#       methodname      方法名称    可用（通配符表1）匹配
#       argumenttype    参数类型    可用（通配符表1、2、3）匹配
#       <methods>       表示类中的任何成员函数
#
#---------------------------------------------------------------------------------------------------
#
#   通配符表1
#       1）？  ：问好代表一个任意字符，但不能是句号（“ . ”，因为句号是包名分隔符）
#       2）*   ：单个星号代表任意个任意字符，但不能代表句号
#
#   通配符表2
#       1）**  ：两个星号代表任意个任意字符，且能代表句号
#
#   通配符表3
#       1）%   ：匹配任何原始类型，如 boolean 、 int 等，但不包括 void
#       2）*** ：匹配任意类型，包括原始类型和非原始类型，数组类型和非数组类型
#       3）... ：匹配任意个数的 任意类型参数
#
#---------------------------------------------------------------------------------------------------

#-keep                          指定类和类成员不能被重命名和删除
#-keepnames                     指定类和类成员不能被重命名，                   如果符合删除规则，则执行删除
#-keepclassmembers              指定类成员不能被重命名和删除
#-keepclassmembernames          指定类成员不能被重命名，                      如果符合删除规则，则执行删除
#-keepclasseswithmembers        指定类和类成员不能被重命名和删除，如果类有类成员
#-keepclasseswithmembernames    指定类和类成员不能被重命名，如果类有类成员       如果符合删除规则，则执行删除


#   指定不警告未解决的引用和其他重要问题，当遇到符合类过滤器的错误时。此设置慎用，可能导致运行时错误
#-dontwarn 类过滤器

#   指定混淆时采用的算法，后面的参数是一个过滤器，这个过滤器是谷歌推荐的算法，一般不改变
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#   会检查每一个引用是否正确，但是第三方库里面往往有些不会用到的类，没有正确引用。如果不配置的话，系统就会报错
#-libraryjars

#   指定打印关于未解决的引用和其他重要问题的警告，但无论如何都要继续处理，此选项慎用，可能导致运行时崩溃
#-ignorewarnings

#   指定混淆字典
#-obfuscationdictionary 文件路径

#   删除指定Log类的引用
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int d(...);
#    public static int w(...);
#    public static int v(...);
#    public static int i(...);
#}


#   指定类和类成员的访问修饰符在处理过程中可以被扩展,在lib项目慎用
#-allowaccessmodification

#   不做预校验，可加快混淆速度
#-dontpreverify

#   不优化输入的类文件
#-dontoptimize

#   将能被混淆的所有类移动到指定目录
#-repackageclasses ''

#   指定类和类成员的访问修饰符在处理过程中可以被扩展,在lib项目慎用
#-allowaccessmodification

#   设置混淆的压缩比率 0 ~ 7
#-optimizationpasses 7

#   混淆时不使用大小写混合，混淆后的类名为小写
#-dontusemixedcaseclassnames

#   生成混淆映射文件
#-verbose

#   指定映射文件的名称
#-printmapping map.txt

#   指定混淆第三方lib类
#-dontskipnonpubliclibraryclasses

#   指定混淆第三方lib的成员
#-dontskipnonpubliclibraryclassmembers

#指定不混淆的选项，选项参考地址：https://www.guardsquare.com/en/proguard/manual/attributes
#-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,LocalVariable*Table,*Annotation*,Synthetic,EnclosingMethod

#-------------------------通用配置------------------------------
-dontpreverify
-dontoptimize
-dontusemixedcaseclassnames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,LocalVariable*Table,*Annotation*,Synthetic,EnclosingMethod
-repackageclasses ''
-allowaccessmodification

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * extends java.io.Serializable {*;}
-keepclassmembers class **.model.** {*;}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * {
   public void *(android.view.View);
}
-keepclassmembers class * {
   private void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

#-------------------------------------------------------------

#moon-sdk-base
-keep class * extends com.meili.moon.sdk.http.IRequestParams
-keep class * extends com.meili.moon.sdk.IComponent
-keep class * extends com.meili.moon.sdk.ComponentsInstaller

-keep class * extends com.meili.moon.sdk.http.common.BaseModel

#kotlin混淆配置
-keep @interface kotlin.Metadata
-keep @kotlin.Metadata class * {  public protected *; }

-keep class kotlin.** {
    public protected *;
}

-keep enum ** {
    public protected *;
}
#end