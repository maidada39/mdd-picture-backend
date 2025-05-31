package com.maidada.mddpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.maidada.mddpicturebackend.config.CosClientConfig;
import com.maidada.mddpicturebackend.dto.file.UploadPictureResult;
import com.maidada.mddpicturebackend.exception.BusinessException;
import com.maidada.mddpicturebackend.exception.ErrorCode;
import com.maidada.mddpicturebackend.manager.CosManager;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
public abstract class PicutreUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public UploadPictureResult uploadPicture(Object inputSource, String prefix) {
        // 检验图片
        validPicture(inputSource);

        // 获取文件名
        String originalFileName = getOriginFileName(inputSource);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String date = DateUtil.format(new Date(), "yyyy-MM-dd");
        String suffix = FileUtil.getSuffix(originalFileName);
        String fileName = String.format("%s_%s.%s", uuid, date, suffix);

        // 上传文件
        String uploadFilePath = String.format("/%s/%s", prefix, fileName);
        File uploadFile = null;
        try {
            uploadFile = File.createTempFile(uploadFilePath, null);
            // 处理文件
            processsFile(inputSource, uploadFile);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, uploadFile);
            // 获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            double scale = NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue();
            UploadPictureResult result = new UploadPictureResult();
            result.setUrl(cosClientConfig.getHost() + uploadFilePath);
            result.setPicName(FileUtil.mainName(originalFileName));
            result.setPicSize(FileUtil.size(uploadFile));
            result.setPicWidth(imageInfo.getWidth());
            result.setPicHeight(imageInfo.getHeight());
            result.setPicScale(scale);
            result.setPicFormat(suffix);

            return result;
        } catch (Exception e) {
            log.error("上传文件失败: {}", originalFileName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            this.deleteTempFile(uploadFile);
        }
    }

    private void deleteTempFile(File uploadFile) {
        if (uploadFile == null) {
            return;
        }
        boolean deleted = uploadFile.delete();
        if (!deleted) {
            log.error("删除临时文件失败: {}", uploadFile.getAbsolutePath());
        }
    }

    protected abstract void processsFile(Object inputSource, File uploadFile) throws Exception;

    protected abstract String getOriginFileName(Object inputSource);

    protected abstract void validPicture(Object inputSource);
}
