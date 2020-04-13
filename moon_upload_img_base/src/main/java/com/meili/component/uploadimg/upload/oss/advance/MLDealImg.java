package com.meili.component.uploadimg.upload.oss.advance;

/**
 * Author： fanyafeng
 * Date： 17/11/13 上午9:50
 * Email: fanyafeng@live.cn
 */
public class MLDealImg {

    final MLDealImgController mlDealImgController;

    public MLDealImg(String imgUrl) {
        mlDealImgController = new MLDealImgController(imgUrl);
    }

    public static class InfoBuilder {
        private final MLDealImgController.InfoParams infoParams;

        public InfoBuilder() {
            infoParams = new MLDealImgController.InfoParams();
        }

        public MLDealImg infoBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            infoParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class AverageHueBuilder {
        private final MLDealImgController.AverageHueParams averageHueParams;

        public AverageHueBuilder() {
            averageHueParams = new MLDealImgController.AverageHueParams();
        }

        public MLDealImg averageHueBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            averageHueParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }

    }

    public static class WaterMarkBuilder {
        private final MLDealImgController.WaterMarkParams waterMarkParams;

        public WaterMarkBuilder() {
            waterMarkParams = new MLDealImgController.WaterMarkParams();
        }

        public WaterMarkBuilder setTransparency(int transparency) {
            waterMarkParams.transparency = transparency;
            return this;
        }

        public WaterMarkBuilder setLocation(String location) {
            waterMarkParams.location = location;
            return this;
        }

        public WaterMarkBuilder setXPoint(int xPoint) {
            waterMarkParams.xPoint = xPoint;
            return this;
        }

        public WaterMarkBuilder setYPoint(int yPoint) {
            waterMarkParams.yPoint = yPoint;
            return this;
        }

        public WaterMarkBuilder setVOffset(int vOffset) {
            waterMarkParams.vOffset = vOffset;
            return this;
        }

        public WaterMarkBuilder setImage(String image) {
            waterMarkParams.image = image;
            return this;
        }

        public WaterMarkBuilder setText(String text) {
            waterMarkParams.text = text;
            return this;
        }

        public WaterMarkBuilder setType(String type) {
            waterMarkParams.type = type;
            return this;
        }

        public WaterMarkBuilder setColor(String color) {
            waterMarkParams.color = color;
            return this;
        }

        public WaterMarkBuilder setSize(int size) {
            waterMarkParams.size = size;
            return this;
        }

        public WaterMarkBuilder setShadow(int shadow) {
            waterMarkParams.shadow = shadow;
            return this;
        }

        public WaterMarkBuilder setRotate(int rotate) {
            waterMarkParams.rotate = rotate;
            return this;
        }

        public WaterMarkBuilder setFill(int fill) {
            waterMarkParams.fill = fill;
            return this;
        }

        public WaterMarkBuilder setOrder(int order) {
            waterMarkParams.order = order;
            return this;
        }

        public WaterMarkBuilder setAlign(int align) {
            waterMarkParams.align = align;
            return this;
        }

        public WaterMarkBuilder setInterval(int interval) {
            waterMarkParams.interval = interval;
            return this;
        }

        public MLDealImg watermarkBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            waterMarkParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class QualityBuilder {
        private final MLDealImgController.QualityParams qualityParams;


        public QualityBuilder() {
            qualityParams = new MLDealImgController.QualityParams();
        }

        public QualityBuilder setRelativeQuality(int relativeQuality) {
            qualityParams.relativeQuality = relativeQuality;
            return this;
        }

        public QualityBuilder setAbsoluteQuality(int absoluteQuality) {
            qualityParams.absoluteQuality = absoluteQuality;
            return this;
        }

        public MLDealImg qualityBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            qualityParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class InterlaceBuilder {
        private final MLDealImgController.InterlaceParams interlaceParams;

        public InterlaceBuilder() {
            interlaceParams = new MLDealImgController.InterlaceParams();
        }

        public InterlaceBuilder setInterlaceValue(int value) {
            interlaceParams.value = value;
            return this;
        }

        public MLDealImg interlaceBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            interlaceParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class SharpenBuilder {
        private final MLDealImgController.SharpenImgParams sharpenImgParams;

        public SharpenBuilder() {
            sharpenImgParams = new MLDealImgController.SharpenImgParams();
        }

        public SharpenBuilder setSharpenValue(int value) {
            sharpenImgParams.value = value;
            return this;
        }

        public MLDealImg sharpenBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            sharpenImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class ContrastBuilder {
        private final MLDealImgController.ContrastImgParams contrastImgParams;

        public ContrastBuilder() {
            contrastImgParams = new MLDealImgController.ContrastImgParams();
        }

        public ContrastBuilder setContrastValue(int value) {
            contrastImgParams.value = value;
            return this;
        }

        public MLDealImg contrastBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            contrastImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class BrightBuilder {
        private final MLDealImgController.BrightImgParams brightImgParams;

        public BrightBuilder() {
            brightImgParams = new MLDealImgController.BrightImgParams();
        }

        public BrightBuilder setBrightValue(int value) {
            brightImgParams.value = value;
            return this;
        }

        public MLDealImg brightBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            brightImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class BlurBuilder {
        private final MLDealImgController.BlurImgParams blurImgParams;

        public BlurBuilder() {
            blurImgParams = new MLDealImgController.BlurImgParams();
        }

        public BlurBuilder setRadius(int radius) {
            blurImgParams.radius = radius;
            return this;
        }

        public BlurBuilder setStandard(int standard) {
            blurImgParams.standard = standard;
            return this;
        }

        public MLDealImg blurBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            blurImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class RotateBuilder {
        private final MLDealImgController.RotateImgParams rotateImgParams;

        public RotateBuilder() {
            rotateImgParams = new MLDealImgController.RotateImgParams();
        }

        public RotateBuilder setRotateValue(int value) {
            rotateImgParams.value = value;
            return this;
        }

        public MLDealImg rotateBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            rotateImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class AutoOrientBuilder {
        private final MLDealImgController.AutoOrientImgParams autoOrientImgParams;

        public AutoOrientBuilder() {
            autoOrientImgParams = new MLDealImgController.AutoOrientImgParams();
        }

        public AutoOrientBuilder setOrientValue(boolean value) {
            autoOrientImgParams.value = value;
            return this;
        }

        public MLDealImg autoOrientBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            autoOrientImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }


    public static class RoundedCornersBuilder {
        private final MLDealImgController.RoundedCornersImgParams roundedCornersImgParams;


        public RoundedCornersBuilder() {
            roundedCornersImgParams = new MLDealImgController.RoundedCornersImgParams();
        }

        public RoundedCornersBuilder setRadiusAndToPng(int radius, boolean isFormatToPng) {
            roundedCornersImgParams.radius = radius;
            roundedCornersImgParams.isFormatToPng = isFormatToPng;
            return this;
        }

        public MLDealImg roundedCornersBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            roundedCornersImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class IndexCropBuilder {
        private final MLDealImgController.IndexCropImgParams indexCropImgParams;

        public IndexCropBuilder() {
            indexCropImgParams = new MLDealImgController.IndexCropImgParams();
        }

        public IndexCropBuilder setXPoint(int xPoint) {
            indexCropImgParams.xPoint = xPoint;
            return this;
        }

        public IndexCropBuilder setYPoint(int yPoint) {
            indexCropImgParams.yPoint = yPoint;
            return this;
        }

        public IndexCropBuilder setPosition(int position) {
            indexCropImgParams.position = position;
            return this;
        }

        public MLDealImg indexCropBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            indexCropImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class CropBuilder {
        private final MLDealImgController.CropImgParams cropImgParams;

        public CropBuilder() {
            cropImgParams = new MLDealImgController.CropImgParams();
        }

        public CropBuilder setWidth(int width) {
            cropImgParams.width = width;
            return this;
        }

        public CropBuilder setHeight(int height) {
            cropImgParams.height = height;
            return this;
        }

        public CropBuilder setXPoint(int xPoint) {
            cropImgParams.xPoint = xPoint;
            return this;
        }

        public CropBuilder setYPoint(int yPoint) {
            cropImgParams.yPoint = yPoint;
            return this;
        }

        public CropBuilder setLocation(String location) {
            cropImgParams.location = location;
            return this;
        }

        public MLDealImg cropBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            cropImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class CircleBuilder {
        private final MLDealImgController.CircleImgParams circleImgParams;

        public CircleBuilder() {
            circleImgParams = new MLDealImgController.CircleImgParams();
        }

        public CircleBuilder setRadiusAndToPng(int radius, boolean isFormatToPng) {
            circleImgParams.radius = radius;
            circleImgParams.isFromatToPng = isFormatToPng;
            return this;
        }

        public MLDealImg circleBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            circleImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }
    }

    public static class ResizeBuilder {

        private final MLDealImgController.ResizeImgParams resizeImgParams;

        public ResizeBuilder() {
            resizeImgParams = new MLDealImgController.ResizeImgParams();
        }

        public ResizeBuilder setModel(String model) {
            resizeImgParams.model = model;
            return this;
        }

        public ResizeBuilder setWidth(int width) {
            resizeImgParams.width = width;
            return this;
        }

        public ResizeBuilder setHeight(int height) {
            resizeImgParams.height = height;
            return this;
        }

        public ResizeBuilder setLimit(int limit) {
            resizeImgParams.limit = limit;
            return this;
        }

        public ResizeBuilder setColor(String color) {
            resizeImgParams.color = color;
            return this;
        }

        public ResizeBuilder setPrecent(int percent) {
            resizeImgParams.percent = percent;
            return this;
        }

        public MLDealImg resizeBuild(String imgUrl) {
            final MLDealImg mlDealImg = new MLDealImg(imgUrl);
            resizeImgParams.apply(mlDealImg.mlDealImgController);
            return mlDealImg;
        }

    }


    public String create() {
        return new String(mlDealImgController.getImgUrl());
    }

}
