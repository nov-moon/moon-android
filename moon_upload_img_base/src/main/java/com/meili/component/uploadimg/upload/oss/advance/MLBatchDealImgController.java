package com.meili.component.uploadimg.upload.oss.advance;

/**
 * Author： fanyafeng
 * Date： 17/11/13 下午7:25
 * Email: fanyafeng@live.cn
 */
public class MLBatchDealImgController {

    private StringBuffer imgUrl;

    public StringBuffer getImgUrl() {
        return imgUrl;
    }

    public MLBatchDealImgController(String imgUrl) {
        this.imgUrl = new StringBuffer(imgUrl);
    }

    private MLBatchDealImgController() {
    }

    static class BatchImgParams {
        public MLDealImg.ResizeBuilder resizeBuilder;

        public MLDealImg.CircleBuilder circleBuilder;

        public MLDealImg.CropBuilder cropBuilder;

        public MLDealImg.IndexCropBuilder indexCropBuilder;

        public MLDealImg.RoundedCornersBuilder roundedCornersBuilder;

        public MLDealImg.AutoOrientBuilder autoOrientBuilder;

        public MLDealImg.RotateBuilder rotateBuilder;

        public MLDealImg.BlurBuilder blurBuilder;

        public MLDealImg.BrightBuilder brightBuilder;

        public MLDealImg.ContrastBuilder contrastBuilder;

        public MLDealImg.SharpenBuilder sharpenBuilder;

        public MLDealImg.InterlaceBuilder interlaceBuilder;

        public MLDealImg.QualityBuilder qualityBuilder;

        public MLDealImg.WaterMarkBuilder waterMarkBuilder;

        public MLDealImg.AverageHueBuilder averageHueBuilder;

        public MLDealImg.InfoBuilder infoBuilder;

        public void apply(MLBatchDealImgController mlBatchDealImgController) {
            mlBatchDealImgController.setResizeImgParams(resizeBuilder);
            mlBatchDealImgController.setCircleImgParams(circleBuilder);

            mlBatchDealImgController.setCropImgParams(cropBuilder);

            mlBatchDealImgController.setIndexCropParams(indexCropBuilder);
            mlBatchDealImgController.setAutoOrientImgParams(autoOrientBuilder);
            mlBatchDealImgController.setRotateImgParams(rotateBuilder);
            mlBatchDealImgController.setBlurImgParams(blurBuilder);
            mlBatchDealImgController.setBrightImgParams(brightBuilder);
            mlBatchDealImgController.setContrastImgParams(contrastBuilder);
            mlBatchDealImgController.setSharpenImgParams(sharpenBuilder);
            mlBatchDealImgController.setInterlaceImgParams(interlaceBuilder);
            mlBatchDealImgController.setQuality(qualityBuilder);
            mlBatchDealImgController.setWaterMark(waterMarkBuilder);
            mlBatchDealImgController.setAverageHue(averageHueBuilder);
        }
    }

    private void setInfo(MLDealImg.InfoBuilder infoBuilder) {
        if (infoBuilder != null) {
            imgUrl.append(infoBuilder.infoBuild("").create());
        }
    }

    private void setAverageHue(MLDealImg.AverageHueBuilder averageHueBuilder) {
        if (averageHueBuilder != null) {
            imgUrl.append(averageHueBuilder.averageHueBuild("").create());
        }
    }

    private void setWaterMark(MLDealImg.WaterMarkBuilder waterMarkBuilder) {
        if (waterMarkBuilder != null) {
            imgUrl.append(waterMarkBuilder.watermarkBuild("").create());
        }
    }

    private void setQuality(MLDealImg.QualityBuilder qualityBuilder) {
        if (qualityBuilder != null) {
            imgUrl.append(qualityBuilder.qualityBuild("").create());
        }
    }

    private void setInterlaceImgParams(MLDealImg.InterlaceBuilder interlaceBuilder) {
        if (interlaceBuilder != null) {
            imgUrl.append(interlaceBuilder.interlaceBuild("").create());
        }
    }

    private void setSharpenImgParams(MLDealImg.SharpenBuilder sharpenBuilder) {
        if (sharpenBuilder != null) {
            imgUrl.append(sharpenBuilder.sharpenBuild("").create());
        }
    }

    private void setContrastImgParams(MLDealImg.ContrastBuilder contrastBuilder) {
        if (contrastBuilder != null) {
            imgUrl.append(contrastBuilder.contrastBuild("").create());
        }
    }

    private void setBrightImgParams(MLDealImg.BrightBuilder brightBuilder) {
        if (brightBuilder != null) {
            imgUrl.append(brightBuilder.brightBuild("").create());
        }
    }

    private void setBlurImgParams(MLDealImg.BlurBuilder blurBuilder) {
        if (blurBuilder != null) {
            imgUrl.append(blurBuilder.blurBuild("").create());
        }
    }

    private void setRotateImgParams(MLDealImg.RotateBuilder rotateBuilder) {
        if (rotateBuilder != null) {
            imgUrl.append(rotateBuilder.rotateBuild("").create());
        }
    }

    private void setAutoOrientImgParams(MLDealImg.AutoOrientBuilder autoOrientBuilder) {
        if (autoOrientBuilder != null) {
            imgUrl.append(autoOrientBuilder.autoOrientBuild("").create());
        }
    }

    private void setRoundedCornersImgParams(MLDealImg.RoundedCornersBuilder roundedCornersBuilder) {
        if (roundedCornersBuilder != null) {
            imgUrl.append(roundedCornersBuilder.roundedCornersBuild("").create());
        }
    }

    private void setResizeImgParams(MLDealImg.ResizeBuilder resizeBuilder) {
        if (resizeBuilder != null) {
            imgUrl.append(resizeBuilder.resizeBuild("").create());
        }
    }

    private void setCircleImgParams(MLDealImg.CircleBuilder circleBuilder) {
        if (circleBuilder != null) {
            imgUrl.append(circleBuilder.circleBuild("").create());
        }
    }

    private void setCropImgParams(MLDealImg.CropBuilder cropBuilder) {
        if (cropBuilder != null) {
            imgUrl.append(cropBuilder.cropBuild("").create());
        }
    }

    private void setIndexCropParams(MLDealImg.IndexCropBuilder indexCropBuilder) {
        if (indexCropBuilder != null) {
            imgUrl.append(indexCropBuilder.indexCropBuild("").create());
        }
    }


}
