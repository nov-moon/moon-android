# 上传图片基础组件

## 简介
> sdk提供上传图片到阿里OSS的能力 <s>([oss文档地址][1])</s>

> sdk内置了默认的测试服务器地址和线上服务器地址，客户端可以通过设置构建类型控制sdk上传地址类型。
客户端也可以通过配置进行自定义host设置，具体的配置方式，参见下面的文档介绍

> sdk默认先压缩，后上传，压缩配置默认使用高清压缩，你也可以通过配置，自定义全局和每次上传的压缩策略。
压缩配置详见：[压缩文档][4]

> 图片上传大小没有限制(5G)，但是如果要使用阿里的图片服务则有如下限制：
* 图片大小：20M
* 图片格式：jpg、png、bmp、gif、webp、tiff
* 对缩略后的图片大小有限制，目标缩略图的宽与高的乘积不能超过4096 x 4096， 且单边的长度不能超过4096 x 4

> 文件的ObjectKey格式如下：
* 正常格式：**channelId** + **/** + **用户自定义前缀** + **文件md5** + **当前毫秒时间戳** + **上传平台标示(android端为:0)** + **.** + **文件后缀**
* md5异常格式：**channelId** + **/** + **用户自定义前缀** + **当前纳秒时间戳** + **md5失败标示(_)** + **上传平台标示(android端为:0)** + **.** + **文件后缀**

## 一、导入SDK

以下接入方式，只针对AndroidStudio

### 配置仓库（引入公司私服）

在project的build.gradle或者module的build.gradle中的repositories节点添加如下配置

    repositories {
        maven {
            url "http://maven.youjie.com/content/groups/public/"
        }
    }

这时就引入了公司的maven私服。
<s>如果你已经有依赖的maven地址，只需要在那个maven代码块的上面再添加上此配置</s>
***

### 配置依赖库
在module的build.gradle中添加库依赖：

    compile 'com.meili.moon:upload-img-base:版本号' 

查看版本号：[byTag][2]或者[byWiki][3]

<b>如果你使用的版本<0.4版本，则引入方式为：compile 'com.meili.mobile.component:upload-img-base:版本号' </b>

***

## 二、开始使用

当前你应该已经配置完成了所有库和依赖。

本sdk的业务逻辑为：
1. 从影像件服务初始化token、oss需要的bucketName、endpoint等参数
2. 如果获取token失败，并且errorCode为-1，则切换为直传影像件服务逻辑
3. 根据配置，尝试转换图片格式到支持的格式
4. 根据配置，尝试压缩图片
5. 如果token获取成功：则开始上传图片到oss
6. 如果token获取失败：则开始上传图片到影像件服务
7. 根据配置，删除压缩缓存
8. 所有图片上传完成后，如果是上传到oss的情况，则开始绑定数据到影像件服务
9. 回调success、finish

中间如果有错误情况，也会进行错误回调。接口要求的callback只支持批量处理的回调结果，至于单独的item的回调请参考callback章节的介绍

### 初始化sdk


sdk当前提供 MLUploadImg和MLUploadVideo，他们分别提供了上传图片和上传视频的功能。

MLUploadImg主要针对上传图片，MLUploadVideo针对上传视频。如果看两个类的实现，不难发现，他们的真实实现都在MLUploadFile中，
这个类定义了不同上传的配置，你可以直接使用MLUploadFile进行文件上传，但是这个类定义的方法会多一个targetFlag参数，用来区分上传目标。
MLUploadImg和MLUploadVideo分别使用了内置的TargetFlags.DEF_IMG和TargetFlags.DEF_VIDEO。
下面以MLUploadImg为例，同样适用于MLUploadVideo。

<b>sdk中提到的channelId和影像件系统的caller是同一个东西</b>

    // 初始化SDK，需要在主线程中执行，需要三个参数：当前app的Application对象、当前app的channelId、publicKey(需要找业务的后端同学要，他们会去找影像件服务申请)
    MLUploadImg.init(mActivity.getApplication(), "CARFIN-CRM", "业务端的publicKey");

    // 生成配置sdk的option
    MLConfigOptions options = new MLConfigOptions.Build()

            // 当前构建是否为debug，默认为release，不打印log，使用线上服务器
            // 如果为debug则默认打印log，使用测试服务器
            .setBuildType(MLBuildType.DEBUG)

            // 设置当前渠道号
            .setChannel("渠道号")

            // 设置是否打印log，可在release构建中使用
            .setLog(true)

            // 设置自定义host，一般情况不需要
            //.setHostConfig(null)
            
            //使用默认上传策略，配置上传的最大重试次数
            .setUploadRetryMaxTimes(2)
            
            //配置上传重试策略，如果设置了此参数，则设置的maxTimes参数无效
            .setUploadRetryHandler(IRetryHandler接口的实现)
            
            //配置自定义压缩配置，如果当前参数设置为null，则不进行压缩操作
            //具体的MLCompressOptions参数，参见[压缩文档][4]
            .setCompressOpts(new MLCompressOptions.Builder().build())
            
            // 设置是否在上传结束后删除压缩缓存，默认删除
            .setAutoDelCompressCache(false)
            
            // 生成options
            .build();

    // 设置option到sdk
    MLUploadImg.config(options);

***

### 二、开始上传

使用sdk上传图片，需要两步改造：
* 将要上传的本地图片对象实现 MLUploadModel 接口
* 调用上传方法，并在回调中更新ui


sdk当前提供两种上传方法，要求在主线程中调用，上传方法定义在了 MLUploadImg和MLUploadVideo 中。都叫 upload() 方法。

##### 第一种：

入参要求一个 MLUploadModel 类型的参数。本参数为了减少客户端接入成本设计，
客户端只需要在自己的原有model中加上此接口，并实现相应方法即可在上传成功后自动绑定上传结果到model中

MLUploadModel接口提供两个能力：
* 让sdk获取上传文件的路径
* sdk将上传结果(uuid)绑定到model中，绑定操作在主线程中进行

如果客户端需要oss的objectKey，可以在model中实现**MLOSSUploadInfo**接口，用来得到oss信息

上传接口定义如下：

    /***
     * 上传，上传一张图片。
     * @param model 需要上传的model
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(MLUploadModel model, MLCallback.MLUploadCallback<? extends MLUploadModel> callback)

    /***
     * 上传，上传一组图片。
     * @param data 需要上传的model列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(List<? extends MLUploadModel> data, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback)

##### 第二种：

入参要求图片以图片路径的方式给sdk

上传接口定义如下：

    /***
     * 上传，上传一张图片.结果回调的model为默认实现MLUploadModel.DefUploadModelImpl，可进行强转使用
     * @param imgFilePath 需要上传的图片路径
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(String imgFilePath, MLCallback.MLUploadCallback<MLUploadModel> callback)

    /***
     * 上传，上传一组图片.结果回调的model为默认实现MLUploadModel.DefUploadModelImpl，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(ArrayList<String> imgFilePaths, MLCallback.MLUploadCallback<MLUploadModel> callback)

    /***
     * 上传，上传一组图片.结果回调的model为默认实现MLUploadModel.DefUploadModelImpl，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(String[] imgFilePaths, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback)


#### 回调**Callback**
你可以以自定义的方式，实现Callback参数，以继承的方式，获得更多回调能力。

主体能力如下：

* MLProgressCallback 获取上传列表的进度回调，例如（1/3）
* MLItemCallback 获取上传Item的进度，例如（512/1024）、获得item的成功失败回调
* MLFinishCallback 获取所有上传结束回调

**MLCallback**中定义了**SimpleCallback**(单图上传)、**SimpleListCallback**(多图上传)，用来简化callback的继承关系。

具体回调定义，可参见**MLCallback**类。

#### 上传配置

在**0.1.4**版本每个上传接口都新增了**MLUploadOption**参数配置，用来自定义当前上传，细节参见**MLUploadOption**类

**MLUploadOption**类中的大概的配置如下：

    /**
     * 上传图片id的前缀，最大不能超过700字节
     * <p>
     * 最终id格式为：channelId + / + 前缀 + 文件md5 + 当前时间戳 + . + 文件后缀
     */
    public String idPrefix;

    /**
     * 是否开启压缩， 如果不开启压缩，则{@link #compressOptions}参数设置无效，默认开启。
     * <p>
     * 如果开启了压缩，但是{@link #compressOptions}=null，则使用默认配置的option进行压缩，
     * <p>
     * 如果默认配置为null，则不进行压缩
     */
    public boolean isCompress = true;

    /** 压缩算法的配置选项，依赖{@link #isCompress}参数开启 */
    public ICompressRule compressOptions;

    /** 是否删除上传成功后的压缩图片，默认使用全局配置，全局配置默认为true */
    public boolean isAutoDelCompressCache
    
    /**
     * 默认的最大上传重试次数，如果此参数<0，则参数设置失效，如果=0，则不做重试
     * <p>
     * 如果设置了{@link #uploadRetryHandler}，则此参数设置无效，使用handler配置
     */
    public int maxRetryTimes = -1;

    /** 上传的重试配置 */
    public IRetryHandler uploadRetryHandler;

#### 压缩

压缩配置可以参见**关于上传配置**中的介绍，这里要说的是如何获取压缩结果相关信息。

两种上传方式的压缩信息获取：

1. 第一种上传，直接在你的model上实现**MLCompressInfoModel**接口，相关压缩信息会在此接口返回。
2. 第二种上传，返回的callback中的model参数是**DefUploadModelImpl**实例，此类已经实现了**MLCompressInfoModel**接口

**MLCompressInfoModel**接口描述能力主要对应当前**美利影像件**需求，如下：

    /** 是否被压缩，isCompress：1.压缩，0.没有压缩 */
    void setIsCompress(int isCompress);
    
    /** 压缩后大小，如果没有压缩，则此值=原始大小，compressSize：具体大小 */
    void setCompressSize(long compressSize);
    
    /** 压缩后文件Md5值，如果没有压缩，则为原文件MD5 */
    void setCompressHashCode(String compressHashCode);
    
    /** 原始大小 */
    void setOriginSize(long originSize);
    
    /** 原始文件md5 */
    void setOriginHashCode(String originHashCode);
    
这几个setter方法对应的字段名称为：

    private int isCompress;
    private long compressSize;
    private String compressHashCode;
    private long originSize;
    private String originHashCode;


#### 支持的文件格式

上传文件，**强制要求为图片文件格式，否则将上传失败**，支持Android手机通用的四种图片格式，分别为：**BMP、PNG、JPG、WEBP**。
为了和IOS平台通用，SDK默认将**BMP、WEBP**格式转换为**JPG**格式。**PNG、JPG**格式保留原格式。
你可以通过全局配置，配置自定义转换器，如下：

    //配置自定义转换器，当前为默认转换器列表，SDK内置的就是这个设置
    .setUploadFileConverters(MLConfigOptions.Build.DEF_FILE_CONVERTER_LIST)

### 停止上传

停止上传方法定义在了 MLUploadImg 类中：

    /** 停止上传，并且正在上传的不会得到回调,如果没有正在上传的内容，则什么都不操作 */
    @MainThread
    public static void stop()

### 获取带处理效果的图片url(工具类采用构建者模式)：

单个图片属性的处理:
    
    String dealImgUrl = new MLDealImg.ResizeBuilder()
        .setLimit(1)//相应的参数配置
        .setHeight(200)
        .resizeBuild("图片的url")
        .create();
  
批量图片属性的处理:
    
    String batchDealImgUrl = new MLBatchDealImg.Builder()
        .setResize(new MLDealImg.ResizeBuilder()
                        .setColor("FFF")
                        .setHeight(2))
        .setCircle(new MLDealImg.CircleBuilder()
                        .setRadiusAndToPng(4, true))
        .build("图片的url")
        .create();


[1]:https://help.aliyun.com/document_detail/32042.html?spm=5176.doc44687.6.696.MJRa9X               "oss文档"
[2]:http://gitlab.mljr.com/moon-android/moon_upload_img_base/tags                                         "tag地址"
[3]:http://gitlab.mljr.com/moon-android/moon_upload_img_base/wikis/home                                   "wiki地址"
[4]:http://gitlab.mljr.com/moon-android/moon_img_compress                                                "压缩sdk地址"