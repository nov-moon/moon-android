package com.meili.component.uploadimg.upload.oss.advance;

/**
 * Author： fanyafeng
 * Date： 17/11/13 上午10:04
 * Email: fanyafeng@live.cn
 */
public class MLDealImgController {

    public static final String RESIZE_LFIT = "lfit";
    public static final String RESIZE_MFIT = "mfit";
    public static final String RESIZE_FILL = "fill";
    public static final String RESIZE_PAD = "pad";
    public static final String RESIZE_FIXED = "fixed";

    public static final int LIMIT_DEAL = 0;
    public static final int LIMIT_NOT_DEAL = 1;

    public static final int FILL_ALL = 1;
    public static final int FILL_NO_USE = 0;

    public static final int ORDER_IMAGE = 0;
    public static final int ORDER_TEXT = 1;

    public static final int ALIGN_TOP = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_BOTTOM = 2;

    public static final String LOCATION_NW = "nw";
    public static final String LOCATION_NORTH = "north";
    public static final String LOCATION_NE = "ne";
    public static final String LOCATION_WEST = "west";
    public static final String LOCATION_CENTER = "center";
    public static final String LOCATION_EAST = "east";
    public static final String LOCATION_SW = "sw";
    public static final String LOCATION_SOUTH = "south";
    public static final String LOCATION_SE = "se";

    public static final String FORMAT_JPG = "jpg";
    public static final String FORMAT_PNG = "png";
    public static final String FORMAT_WDBP = "webp";
    public static final String FORMAT_BMP = "bmp";
    public static final String FORMAT_GIF = "gif";

    private MLDealImgController() {

    }

    private StringBuffer imgUrl;

    public StringBuffer getImgUrl() {
        return imgUrl;
    }

    public MLDealImgController(String imgUrl) {
        this.imgUrl = new StringBuffer(imgUrl);
    }

    /**
     * 图片缩放
     */
    public static class ResizeImgParams {

        /**
         * [lfit,mfit,fill,pad,fixed]，默认为lfit
         * <p>
         * 指定缩略的模式：
         * - lfit：等比缩放，限制在设定在指定w与h的矩形内的最大图片。
         * - mfit：等比缩放，延伸出指定w与h的矩形框外的最小图片。
         * - fill：固定宽高，将延伸出指定w与h的矩形框外的最小图片进行居中裁剪。
         * - pad：固定宽高，缩略填充。
         * - fixed：固定宽高，强制缩略
         */
        public String model;
        /**
         * 指定目标缩略图的宽度。	1-4096
         */
        public int width;
        /**
         * 指定目标缩略图的高度。	1-4096
         */
        public int height;
        /**
         * 0/1, 默认是 1
         * <p>
         * 指定当目标缩略图大于原图时是否处理。值是 1 表示不处理；值是 0 表示处理。
         */
        public int limit;
        /**
         * [000000-FFFFFF]
         * <p>
         * 当缩放模式选择为pad（缩略填充）时，可以选择填充的颜色(默认是白色)参数的填写方式：采用16进制颜色码表示，如00FF00（绿色）。
         */
        public String color;
        /**
         * 1-1000
         * <p>
         * 倍数百分比。 小于100，即是缩小，大于100即是放大。
         */
        public int percent;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.resizeImg();
            mlDealImgController.setModel(model);
            mlDealImgController.setWidth(width);
            mlDealImgController.setHeight(height);
            mlDealImgController.setLimit(limit);
            mlDealImgController.setColor(color);
        }
    }

    /**
     * 内切圆
     */
    static class CircleImgParams {
        /**
         * 从图片取出的圆形区域的半径
         * <p>
         * 半径 r 不能超过原图的最小边的一半。如果超过，则圆的大小仍然是原圆的最大内切圆。
         * 如果指定半径大于原图最大内切圆的半径，则圆的大小仍然是图片的最大内切圆。
         */
        public int radius;

        /**
         * 是否吧图片保存为png
         * <p>
         * 如果图片的最终格式是 png、webp、 bmp 等支持透明通道的图片，那么图片非圆形区域的地方将会以透明填充。
         * 如果图片的最终格式是 jpg，那么非圆形区域是以白色进行填充。推荐保存成 png 格式。
         */
        public boolean isFromatToPng;


        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.circleImg();
            mlDealImgController.setRadius(radius, isFromatToPng);
        }

    }

    static class CropImgParams {
        /**
         * 指定裁剪宽度
         * <p>
         * [0-图片宽度]
         */
        public int width;
        /**
         * 指定裁剪高度
         * <p>
         * [0-图片高度]
         */
        public int height;
        /**
         * 指定裁剪起点横坐标（默认左上角为原点）
         * <p>
         * [0-图片边界]
         */
        public int xPoint;
        /**
         * 指定裁剪起点纵坐标（默认左上角为原点）
         * <p>
         * [0-图片边界]
         */
        public int yPoint;
        /**
         * 设置裁剪的原点位置，由九宫格的格式，一共有九个地方可以设置，每个位置位于每个九宫格的左上角
         * <p>
         * [nw,north,ne,west,center,east,sw,south,se]
         */
        public String location;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.cropImg();
            mlDealImgController.setWidth(width);
            mlDealImgController.setHeight(height);
            mlDealImgController.setXPoint(xPoint);
            mlDealImgController.setYPoint(yPoint);
            mlDealImgController.setLocation(location);
        }
    }

    static class IndexCropImgParams {
        /**
         * 进行水平切割，每块图片的长度。x 参数与 y 参数只能任选其一。
         * <p>
         * [1,图片宽度]
         */
        public int xPoint;
        /**
         * 进行垂直切割，每块图片的长度。x 参数与 y 参数只能任选其一。
         * <p>
         * [1,图片高度]
         */
        public int yPoint;
        /**
         * 选择切割后第几个块。（0表示第一块）
         * <p>
         * [0,最大块数)。如果超出最大块数，返回原图。
         */
        public int position;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.indexCropImg();
            mlDealImgController.setXPoint(xPoint);
            mlDealImgController.setYPoint(yPoint);
            mlDealImgController.setPosition(position);
        }
    }

    static class RoundedCornersImgParams {
        /**
         * 将图片切出圆角，指定圆角的半径。
         * <p>
         * [1, 4096]
         * 生成的最大圆角的半径不能超过原图的最小边的一半。
         */
        public int radius;
        /**
         * 是否吧图片保存为png
         * <p>
         * 如果图片的最终格式是 png、webp、 bmp 等支持透明通道的图片，那么图片非圆形区域的地方将会以透明填充。
         * 如果图片的最终格式是 jpg，那么非圆形区域是以白色进行填充。推荐保存成 png 格式。
         */
        public boolean isFormatToPng;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.roundedCornersImg();
            if (radius >= 1 && radius <= 4096) {
                mlDealImgController.setRadius(radius, isFormatToPng);
            }
        }
    }

    static class AutoOrientImgParams {
        /**
         * 进行自动旋转
         * 0：表示按原图默认方向，不进行自动旋转。
         * 1：先进行图片进行旋转，然后再进行缩略
         * <p>
         * [0, 1]对应false，true
         */
        public boolean value;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.autoOrient();
            mlDealImgController.setValue(value);
        }
    }

    static class RotateImgParams {
        /**
         * 图片按顺时针旋转的角度
         * <p>
         * [0, 360]
         * 默认值为 0，表示不旋转。
         */
        public int value;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.rotate();
            mlDealImgController.setValue(value);
        }
    }

    static class BlurImgParams {
        /**
         * 模糊半径
         * <p>
         * [1,50]
         * r 越大图片越模糊。
         */
        public int radius;
        /**
         * 正态分布的标准差
         * <p>
         * [1,50]
         * s 越大图片越模糊。
         */
        public int standard;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.blur();
            mlDealImgController.setRadius(radius);
            mlDealImgController.setStandard(standard);
        }
    }

    static class BrightImgParams {
        /**
         * 亮度调整。0 表示原图亮度，小于 0 表示低于原图亮度，大于 0 表示高于原图亮度。
         * <p>
         * [-100, 100]
         */
        public int value;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.bright();
            mlDealImgController.setValue(value);
        }
    }

    static class ContrastImgParams {
        /**
         * 对比度调整。0 表示原图对比度，小于 0 表示低于原图对比度，大于 0 表示高于原图对比度。
         * <p>
         * [-100, 100]
         */
        public int value;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.contrast();
            mlDealImgController.setValue(value);
        }
    }

    static class SharpenImgParams {
        /**
         * 表示进行锐化处理。取值为锐化参数，参数越大，越清晰。
         * <p>
         * [50, 399]
         * 为达到较优效果，推荐取值为 100。
         */
        public int value;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.sharpen();
            mlDealImgController.setValue(value);
        }
    }

    static class FormatImgParams {
        /**
         * jpg
         * 将原图保存成jpg格式，如果原图是png、webp、bmp存在透明通道，默认会把透明填充成白色。
         * <p>
         * png
         * 将原图保存成png格式。
         * <p>
         * webp
         * 将原图保存成webp格式。
         * <p>
         * bmp
         * 将原图保存成bmp格式。
         * <p>
         * gif
         * 将gif格式保存成gif格式，非gif格式是按原图格式保存。
         */
        public String format;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.format();
            mlDealImgController.setFormat(format);
        }
    }

    static class InterlaceParams {
        /**
         * 1 表示保存成渐进显示的 jpg 格式
         * 0 表示保存成普通的 jpg 格式
         * <p>
         * [0, 1]
         */
        public int value;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.interlace();
            mlDealImgController.setValue(value);
        }
    }

    static class QualityParams {
        /**
         * 决定图片的相对质量，对原图按照 q% 进行质量压缩。如果原图质量是 100%，使用 90q 会得到质量为 90％ 的图片；
         * 如果原图质量是 80%，使用 90q 会得到质量72%的图片。
         * 只能在原图是 jpg 格式的图片上使用，才有相对压缩的概念。如果原图为 webp，那么相对质量就相当于绝对质量。
         * <p>
         * 1-100
         */
        public int relativeQuality;
        /**
         * 决定图片的绝对质量，把原图质量压到Q%，如果原图质量小于指定数字，则不压缩。
         * 如果原图质量是100%，使用”90Q”会得到质量90％的图片；如果原图质量是95%，使用“90Q”还会得到质量90%的图片；
         * 如果原图质量是80%，使用“90Q”不会压缩，返回质量80%的原图。
         * 只能在保存格式为jpg/webp效果上使用，其他格式无效果。 如果同时指定了q和Q，按Q来处理。
         * <p>
         * 1-100
         */
        public int absoluteQuality;

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.quality();
            mlDealImgController.setRelativeQuality(relativeQuality);
            mlDealImgController.setAbsoluteQuality(absoluteQuality);
        }
    }

    static class WaterMarkParams {
        //----------------以下为基础参数，都是可选---------------------
        /**
         * 参数意义：透明度, 如果是图片水印，就是让图片变得透明，如果是文字水印，就是让水印变透明。
         * 默认值：100， 表示 100%（不透明） 取值范围: [0-100]
         * <p>
         * 可选参数
         */
        public int transparency;
        /**
         * 参数意义：位置，水印打在图的位置，详情参考下方区域数值对应图。
         * 取值范围：[nw,north,ne,west,center,east,ne,south]
         * <p>
         * 可选参数
         */
        public String location;
        /**
         * 参数意义：水平边距, 就是距离图片边缘的水平距离， 这个参数只有当水印位置是左上，左中，左下， 右上，右中，右下才有意义
         * 默认值：10
         * 取值范围：[0 – 4096]
         * 单位：像素（px）
         * <p>
         * 可选参数
         */
        public int xPoint;
        /**
         * 参数意义：垂直边距, 就是距离图片边缘的垂直距离， 这个参数只有当水印位置是左上，中上， 右上，左下，中下，右下才有意义
         * 默认值：10
         * 取值范围：[0 – 4096]
         * 单位：像素(px)
         * <p>
         * 可选参数
         */
        public int yPoint;
        /**
         * 参数意义： 中线垂直偏移，当水印位置在左中，中部，右中时，可以指定水印位置根据中线往上或者往下偏移。
         * 默认值：0
         * 取值范围：[-1000, 1000]
         * 单位：像素(px)
         * <p>
         * 可选参数
         */
        public int vOffset;

        //----------------以上为基础参数，都是可选---------------------

        //----------------以下为图片水印参数，必选---------------------
        /**
         * 参数意义： 水印图片的object名字(必须编码)
         * 注意：内容必须是URL安全base64编码 encodedObject = url_safe_base64_encode(object) 如object为”panda.png”,
         * 编码过后的内容为”cGFuZGEucG5n”
         * <p>
         * 必选参数
         */
        public String image;
        //----------------以上为图片水印参数，必选---------------------

        //----------------以下为文字水印参数，是否必选看注释---------------------
        /**
         * 参数意义：表示文字水印的文字内容(必须编码)
         * 注意：必须是URL安全base64编码 encodeText = url_safe_base64_encode(fontText)
         * 最大长度为64个字符(即支持汉字最多20个左右)
         * <p>
         * 必选参数
         */
        public String text;
        /**
         * 参数意义：表示文字水印的文字类型(必须编码)
         * 注意：必须是URL安全base64编码 encodeText = url_safe_base64_encode(fontType)
         * 取值范围：见下表（文字类型编码对应表）
         * 默认值：wqy-zenhei ( 编码后的值：d3F5LXplbmhlaQ)
         * <p>
         * 可选参数
         */
        public String type;
        /**
         * 参数意义：文字水印文字的颜色
         * 参数的构成必须是：六个十六进制数 如：000000表示黑色。 000000每两位构成RGB颜色， FFFFFF表示的是白色
         * 默认值：000000黑色
         * <p>
         * 可选参数
         */
        public String color;
        /**
         * 参数意义：文字水印文字大小(px)
         * 取值范围：(0，1000]
         * 默认值：40
         * <p>
         * 可选参数
         */
        public int size;
        /**
         * 参数意义：文字水印的阴影透明度
         * 取值范围：(0,100]
         * <p>
         * 可选参数
         */
        public int shadow;
        /**
         * 参数意义：文字顺时针旋转角度
         * 取值范围：[0,360]
         * <p>
         * 可选参数
         */
        public int rotate;
        /**
         * 参数意义：进行水印铺满的效果；
         * 取值范围：[0,1]，1表示铺满，0表示效果无效
         * <p>
         * 可选参数
         */
        public int fill;
        //----------------以上为文字水印参数，是否必选看注释---------------------

        //----------------以下为文图混合，是否必选看注释---------------------
        /**
         * 参数意义： 文字，图片水印前后顺序
         * 取值范围：[0, 1] order = 0 图片在前(默认值)； order = 1 文字在前。
         * <p>
         * 可选参数
         */
        public int order;
        /**
         * 参数意义：文字、图片对齐方式
         * 取值范围：[0, 1, 2] align = 0 上对齐(默认值) align = 1 中对齐 align = 2 下对齐
         * <p>
         * 可选参数
         */
        public int align;
        /**
         * 参数意义：文字和图片间的间距 取值范围: [0, 1000]
         * <p>
         * 可选参数
         */
        public int interval;
        //----------------以上为文图混合，是否必选看注释---------------------

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.waterMark();
            mlDealImgController.setTransparency(transparency);
            mlDealImgController.setLocation(location);
            mlDealImgController.setXPoint(xPoint);
            mlDealImgController.setYPoint(yPoint);
            mlDealImgController.setVOffset(vOffset);
            mlDealImgController.setImage(image);
            mlDealImgController.setText(text);
            mlDealImgController.setType(type);
            mlDealImgController.setColor(color);
            mlDealImgController.setSize(size);
            mlDealImgController.setShadow(shadow);
            mlDealImgController.setRotate(rotate);
            mlDealImgController.setFill(fill);
            mlDealImgController.setOrder(order);
            mlDealImgController.setAlign(align);
            mlDealImgController.setInterval(interval);
        }
    }

    static class AverageHueParams {

        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.averageHue();
        }

    }

    static class InfoParams {
        public void apply(MLDealImgController mlDealImgController) {
            mlDealImgController.info();
        }
    }

    private void info() {
        imgUrl.append("/info");
    }

    private void averageHue() {
        imgUrl.append("/average-hue");
    }

    /**
     * 图片水印
     */
    private void waterMark() {
        imgUrl.append("/watermark");
    }

    /**
     * 质量变换
     */
    private void quality() {
        imgUrl.append("/quality");
    }

    /**
     * 渐进显示
     */
    private void interlace() {
        imgUrl.append("/interlace");
    }

    /**
     * 格式转换
     */
    private void format() {
        imgUrl.append("/format");
    }


    /**
     * 锐化操作
     */
    private void sharpen() {
        imgUrl.append("/sharpen");
    }

    /**
     * 对比度操作
     */
    private void contrast() {
        imgUrl.append("/contrast");
    }

    /**
     * 亮度操作
     */
    private void bright() {
        imgUrl.append("/bright");
    }

    /**
     * 模糊操作
     */
    private void blur() {
        imgUrl.append("/blur");
    }

    /**
     * 旋转操作
     */
    private void rotate() {
        imgUrl.append("/rotate");
    }

    /**
     * 自适应方向操作
     */
    private void autoOrient() {
        imgUrl.append("/rounded-corners");
    }

    /**
     * 圆角矩形操作
     */
    private void roundedCornersImg() {
        imgUrl.append("/rounded-corners");
    }

    /**
     * 图片索引切割操作
     */
    private void indexCropImg() {
        imgUrl.append("/indexcrop");
    }

    /**
     * 图片裁剪操作
     */
    private void cropImg() {
        imgUrl.append("/crop");
    }

    /**
     * 图片缩放操作
     */
    private void resizeImg() {
        imgUrl.append("/resize");
    }

    /**
     * 图片内切圆操作
     */
    private void circleImg() {
        imgUrl.append("/circle");
    }

    private void setImage(String image) {
        if (image != null && !image.equals("")) {
            imgUrl.append(",image_" + image.replace("+", "-").replace("/", "_"));
        }
    }

    private void setTransparency(int transparency) {
        if (transparency >= 0 && transparency <= 100) {
            imgUrl.append(",t_" + transparency);
        }
    }

    private void setVOffset(int vOffset) {
        if (vOffset >= -1000 && vOffset <= 1000) {
            imgUrl.append(",voffset_" + vOffset);
        }
    }

    private void setRelativeQuality(int quality) {
        if (quality >= 1 && quality <= 100) {
            imgUrl.append(",q_" + quality);
        }
    }

    private void setAbsoluteQuality(int quality) {
        if (quality >= 1 && quality <= 100) {
            imgUrl.append(",Q_" + quality);
        }
    }

    private void setFormat(String format) {
        if (format != null && !format.equals("")) {
            imgUrl.append("," + format);
        }
    }

    private void setStandard(int standard) {
        if (standard >= 1 && standard <= 50) {
            imgUrl.append("s_" + standard);
        }
    }

    private void setValue(int value) {
        imgUrl.append("," + value);
    }

    private void setValue(boolean value) {
        if (value) {
            imgUrl.append(",1");
        }
    }

    private void setModel(String model) {
        if (model != null && !model.equals("")) {
            imgUrl.append(",m_" + model);
        }
    }

    private void setWidth(int width) {
        if (width >= 1 && width <= 4096) {
            imgUrl.append(",w_" + width);
        }
    }

    private void setHeight(int height) {
        if (height >= 1 && height <= 4096) {
            imgUrl.append(",h_" + height);
        }

    }

    private void setLimit(int limit) {
        if (limit == LIMIT_DEAL || limit == LIMIT_NOT_DEAL) {
            imgUrl.append(",limit_" + limit);
        }
    }

    private void setColor(String color) {
        if (color != null && !color.equals("")) {
            imgUrl.append(",color_" + color);
        }
    }

    private void setPercent(int percent) {
        if (percent >= 1 && percent <= 1000) {
            imgUrl.append(",p_" + percent);
        }
    }

    private void setRadius(int radius) {
        setRadius(radius, false);
    }

    private void setRadius(int radius, boolean isFormatToPng) {
        if (radius > 0) {
            imgUrl.append(",r_" + radius);
            if (isFormatToPng) {
                imgUrl.append("/format,png");
            }
        }
    }

    private void setXPoint(int xPoint) {
        if (xPoint >= 0) {
            imgUrl.append(",x_" + xPoint);
        }
    }

    private void setYPoint(int yPoint) {
        if (yPoint >= 0) {
            imgUrl.append(",y_" + yPoint);
        }
    }

    private void setLocation(String location) {
        if (location != null && !location.equals("")) {
            imgUrl.append(",g_" + location);
        }
    }

    private void setPosition(int position) {
        if (position >= 0) {
            imgUrl.append(",i_" + position);
        }
    }

    private void setInterval(int interval) {
        if (interval >= 0 && interval <= 1000) {
            imgUrl.append(",interval_" + interval);
        }
    }

    private void setAlign(int align) {
        if (align == ALIGN_TOP || align == ALIGN_CENTER || align == ALIGN_BOTTOM) {
            imgUrl.append(",align_" + align);
        }
    }

    private void setOrder(int order) {
        if (order == ORDER_IMAGE || order == ORDER_TEXT) {
            imgUrl.append(",order_" + order);
        }
    }

    private void setFill(int fill) {
        if (fill == FILL_ALL || fill == FILL_NO_USE) {
            imgUrl.append(",fill_" + fill);
        }
    }

    private void setRotate(int rotate) {
        if (rotate >= 0 && rotate <= 360) {
            imgUrl.append(",rotate_" + rotate);
        }
    }

    private void setShadow(int shadow) {
        if (shadow > 0 && shadow <= 100) {
            imgUrl.append(",shadow_" + shadow);
        }
    }

    private void setSize(int size) {
        if (size > 0 && size <= 1000) {
            imgUrl.append(",size_" + size);
        }
    }

    private void setType(String type) {
        if (type != null && !type.equals("")) {
            imgUrl.append(",type_" + type.replace("+", "-").replace("/", "_"));
        }
    }

    private void setText(String text) {
        if (text != null && !text.equals("")) {
            imgUrl.append(",text_" + text.replace("+", "-").replace("/", "_"));
        }
    }


}
