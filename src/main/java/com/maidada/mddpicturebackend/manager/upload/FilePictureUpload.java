package com.maidada.mddpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.maidada.mddpicturebackend.exception.ErrorCode;
import com.maidada.mddpicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author wulinxuan
 * @date 2025/5/31 15:57
 */
@Slf4j
@Component
public class FilePictureUpload extends PicutreUploadTemplate {


    @Override
    protected void processsFile(Object inputSource, File uploadFile) throws Exception {
        MultipartFile file = (MultipartFile) inputSource;
        file.transferTo(uploadFile);
    }

    @Override
    protected String getOriginFileName(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        return file.getOriginalFilename();
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀列表（或者集合）
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }
}
