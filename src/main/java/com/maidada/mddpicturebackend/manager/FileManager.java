package com.maidada.mddpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.maidada.mddpicturebackend.config.CosClientConfig;
import com.maidada.mddpicturebackend.dto.file.UploadPictureResult;
import com.maidada.mddpicturebackend.exception.BusinessException;
import com.maidada.mddpicturebackend.exception.ErrorCode;
import com.maidada.mddpicturebackend.exception.ThrowUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * @author wulinxuan
 * @date 2025/5/12 20:01
 */
@Slf4j
@Component
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    @Resource
    private CosManager cosManager;

    public UploadPictureResult uploadPicture(MultipartFile file, String prefix) {
        // 检验图片
        validPicture(file);

        // 上传地址拼接
        String originalFileName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String date = DateUtil.format(new Date(), "yyyy-MM-dd");
        String suffix = FileUtil.getSuffix(originalFileName);
        String fileName = String.format("%s_%s.%s", uuid, date, suffix);

        // 上传文件
        String uploadFilePath = String.format("/%s/%s", prefix, fileName);
        File uploadFile = null;
        try {
            uploadFile = File.createTempFile(uploadFilePath, null);
            file.transferTo(uploadFile);
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
        } catch (IOException e) {
            log.error("上传文件失败: {}", originalFileName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            deleteFile(uploadFile);
        }
    }

    private void validPicture(MultipartFile file) {
        // 非空判断
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 大小判断
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(file.getSize() > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
        // 格式判断
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        final List<String> allowFileType = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
        ThrowUtils.throwIf(!allowFileType.contains(suffix), ErrorCode.PARAMS_ERROR, "文件格式错误");
    }

    private void deleteFile(File file) {
        if (file == null) {
            return;
        }

        boolean deleted = file.delete();
        if (!deleted) {
            log.error("文件删除失败: {}", file.getAbsolutePath());
        }
    }
}
