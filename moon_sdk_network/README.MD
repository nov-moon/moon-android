## 网络请求库

### 引入方式

    api "com.meili.moon.sdk:http:1.6.0"


### 关于错误

现在错误分为四类：
1. 请求服务器前，会抛出：请求失败，请稍后重试(1004) 为msg的exception。具体错误可参见：**HttpExceptionCode**类
2. 请求服务器时，比如请求服务器404之类的，会抛出：服务器开小差(404)  为msg的exception，具体错误可参见：**SdkHttpLoader**类
3. 请求成功后，数据解析错误，会抛出：数据解析错误 为msg的exception，具体错误可参见**DefHttpLoader**类的95行
4. 请求成功后，业务处理阶段出错的，会抛出：数据处理错误(1) 为的exception，具体错误可参见**TaskProxy**类的最下面