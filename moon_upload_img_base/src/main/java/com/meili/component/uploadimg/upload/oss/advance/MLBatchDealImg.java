package com.meili.component.uploadimg.upload.oss.advance;

/**
 * Author： fanyafeng
 * Date： 17/11/13 下午7:16
 * Email: fanyafeng@live.cn
 */
public class MLBatchDealImg {

    final MLBatchDealImgController mlBatchDealImgController;

    public MLBatchDealImg(String imgUrl) {
        mlBatchDealImgController = new MLBatchDealImgController(imgUrl);
    }


    public static class Builder {

        private final MLBatchDealImgController.BatchImgParams batchImgParams;

        public Builder() {
            batchImgParams = new MLBatchDealImgController.BatchImgParams();
        }


        public Builder setResize(MLDealImg.ResizeBuilder resizeBuilder) {
            batchImgParams.resizeBuilder = resizeBuilder;
            return this;
        }

        public Builder setCircle(MLDealImg.CircleBuilder circleBuilder) {
            batchImgParams.circleBuilder = circleBuilder;
            return this;
        }

        public Builder setCrop(MLDealImg.CropBuilder cropBuilder) {
            batchImgParams.cropBuilder = cropBuilder;
            return this;
        }

        public Builder setIndexCrop(MLDealImg.IndexCropBuilder indexCropBuilder) {
            batchImgParams.indexCropBuilder = indexCropBuilder;
            return this;
        }

        public Builder setRoundedCorners(MLDealImg.RoundedCornersBuilder roundedCornersBuilder) {
            batchImgParams.roundedCornersBuilder = roundedCornersBuilder;
            return this;
        }

        public Builder setAutoOrient(MLDealImg.AutoOrientBuilder autoOrientBuilder) {
            batchImgParams.autoOrientBuilder = autoOrientBuilder;
            return this;
        }

        public Builder setRotate(MLDealImg.RotateBuilder rotateBuilder) {
            batchImgParams.rotateBuilder = rotateBuilder;
            return this;
        }

        public Builder setBlur(MLDealImg.BlurBuilder blurBuilder) {
            batchImgParams.blurBuilder = blurBuilder;
            return this;
        }

        public Builder setBright(MLDealImg.BrightBuilder brightBuilder) {
            batchImgParams.brightBuilder = brightBuilder;
            return this;
        }

        public Builder setContrast(MLDealImg.ContrastBuilder contrastBuilder) {
            batchImgParams.contrastBuilder = contrastBuilder;
            return this;
        }

        public Builder setSharpen(MLDealImg.SharpenBuilder sharpenBuilder) {
            batchImgParams.sharpenBuilder = sharpenBuilder;
            return this;
        }

        public Builder setInterlace(MLDealImg.InterlaceBuilder interlaceBuilder) {
            batchImgParams.interlaceBuilder = interlaceBuilder;
            return this;
        }

        public Builder setQuality(MLDealImg.QualityBuilder qualityBuilder) {
            batchImgParams.qualityBuilder = qualityBuilder;
            return this;
        }

        public Builder setWaterMark(MLDealImg.WaterMarkBuilder waterMarkBuilder) {
            batchImgParams.waterMarkBuilder = waterMarkBuilder;
            return this;
        }

        public Builder setAverageHue(MLDealImg.AverageHueBuilder averageHueBuilder) {
            batchImgParams.averageHueBuilder = averageHueBuilder;
            return this;
        }

        public Builder setInfo(MLDealImg.InfoBuilder infoBuilder) {
            batchImgParams.infoBuilder = infoBuilder;
            return this;
        }

        public MLBatchDealImg build(String imgUrl) {
            final MLBatchDealImg mlBatchDealImg = new MLBatchDealImg(imgUrl);
            batchImgParams.apply(mlBatchDealImg.mlBatchDealImgController);
            return mlBatchDealImg;
        }
    }

    public String create() {
        return new String(mlBatchDealImgController.getImgUrl());
    }
}
