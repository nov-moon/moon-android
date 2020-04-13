# 基础工具库

> moon_sdk_kit(以下简称kit)库主要提供两类功能：在CommonSdk中声明的功能和一些工具集。

kit库作为皓月库的链接中心，提供以下两个主要功能：

1. 给其他组件提供通用工具，例如日志打印
2. 通过接口方式定义能力标准，从而其他组件库基于标准编程，解耦组件之间的依赖。

基于这个设计初衷，其他大部分组件库都需要依赖此kit库，基于kit编程。一般对于应用来说，可以忽略此库的存在。

## 1. 快速集成
    
    api "com.meili.moon.sdk:kit:x.x.x"
    
具体的kit版本，请参见wiki中的[版本日志](http://gitlab.mljr.com/moon-android/moon_sdk_kit/wikis/home)

## 2. 使用方式
### 2.1. CommonSdk中声明的功能

CommonSdk是kit库的主要能力入口，绝大部分功能都会在CommonSdk中声明便捷的访问入口。
CommonSdk中主要提供的功能分为两类：

kit本身已经实现或集成的能力，这些能力不需要其他的依赖配置和安装，如下：

1. task() 获取task执行器，同步post一个runnable或者异步执行一个task
2. executor() 获取app中线程池的管理对象，用来统一管理线程池
3. json() 获取json解析工具类
4. event() 获取事件订阅管理对象

kit中只定义了标准，并没有做具体实现的功能如下：

1. environment() 获取当前上下文环境
2. app() 获取当前程序Application对象
3. db() 获取数据库管理对象
4. http() 获取网络请求管理对象
5. fileCache() 获取文件缓存管理对象
6. cache() 获取通用String缓存管理对象


### 2.2. kit库中的常用工具集

#### Logcat，统一的日志打印

kit中实现了日志打印工具Logcat。Logcat提供了常用的log日志打印功能，
例如：Logcat.d()、Logcat.e()等等

#### Util包下的工具集

1. encrypt，常用的数据加密

    encrypt提供常用的加密解密功能，主要有：AESUtils、Base64、MD5

2. AtomicHelper，原子操作工具类

    AtomicHelper类是对原子操作的功能扩展类，当前提供两种方法，setOnce和setOnceNoError方法。两个方法都表示只能设置一次，而第一种方法会在以后设置时报错

3. Checker，通用的对象检查工具

    Checker中提供了针对常见集合类型、String类型的空检查、大小检查、文件存在检查，主要方法为isEmpty()、largerSize()、sameSize()、exists()。
    针对String数组但态度提供了hasEmpty()方法用来检查是否含有空内容

4. IOUtil，提供常用io操作工具

5. KotlinExtend，部分功能扩展类

    当前有针对Android的SparseArray等的for循环扩展和判断当前线程是否是主线程的方法isMainThread
    
6. Asserts，断言帮助类

    Asserts中会提供几种断言，用来判断参数值是否是预计参数值，如果不是会直接报错，分别为：assertTrue、assertFalse和assertNonNull
    
7. ParameterizedTypeUtil，泛型获取帮助工具

    ParameterizedTypeUtil提供了基于目标类的泛型获取工具
    
8. ProcessLock，app内的进程锁

    通过文件锁机制实现的app内的进程锁

9. StringUtil，常用的文本工具集

10. ThrowableUtils，错误工具集

    当前只提供了一个方法：throwOnDebug，如果当前是debug模式，则抛出错误，否则不做任何操作