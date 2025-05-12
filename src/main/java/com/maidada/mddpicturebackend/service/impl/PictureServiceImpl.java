package com.maidada.mddpicturebackend.service.impl;

import java.util.Date;
import java.util.Objects;

import com.maidada.mddpicturebackend.common.BaseRequest;
import com.maidada.mddpicturebackend.dto.file.UploadPictureResult;
import com.maidada.mddpicturebackend.exception.ErrorCode;
import com.maidada.mddpicturebackend.exception.ThrowUtils;
import com.maidada.mddpicturebackend.manager.FileManager;
import com.maidada.mddpicturebackend.service.UserService;
import com.maidada.mddpicturebackend.vo.picture.PictureVO;
import com.maidada.mddpicturebackend.vo.user.UserLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maidada.mddpicturebackend.mapper.PictureMapper;
import com.maidada.mddpicturebackend.dto.picture.PictureUploadRequest;
import com.maidada.mddpicturebackend.dto.picture.PicturePageRequest;
import com.maidada.mddpicturebackend.dto.picture.PictureUpdateRequest;
import com.maidada.mddpicturebackend.entity.Picture;
import com.maidada.mddpicturebackend.vo.picture.PictureDetailVO;
import com.maidada.mddpicturebackend.vo.picture.PicturePageVO;
import com.maidada.mddpicturebackend.service.PictureService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * [图片]服务实现类
 *
 * @author wulinxuan
 * @date 2025-05-12 20:02
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Override
    public PictureVO upload(MultipartFile multipartFile, PictureUploadRequest param) {
        // 校验
        ThrowUtils.throwIf(Objects.isNull(multipartFile), ErrorCode.PARAMS_ERROR, "图片文件不能为空");

        // 如果传递了id表示更新操作，判断是否存在
        Long id = param.getId();
        if (Objects.nonNull(id)) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, id).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "图片不存在");
        }

        // 上传文件
        UserLoginVO loginUser = userService.getLoginUser();
        String prefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, prefix);

        // 字段填充
        Picture entity = new Picture();
        entity.setId(id);
        entity.setUrl(uploadPictureResult.getUrl());
        entity.setName(uploadPictureResult.getPicName());
        entity.setPicSize(uploadPictureResult.getPicSize());
        entity.setPicWidth(uploadPictureResult.getPicWidth());
        entity.setPicHeight(uploadPictureResult.getPicHeight());
        entity.setPicScale(uploadPictureResult.getPicScale());
        entity.setPicFormat(uploadPictureResult.getPicFormat());
        entity.setUserId(loginUser.getId());
        if (id != null) {
            entity.setEditTime(new Date());
        }

        // 操作数据库
        boolean result = saveOrUpdate(entity);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "上传失败，操作数据库失败");

        return PictureVO.objToVo(entity);
    }

    @Override
    public void delete(BaseRequest param) {
        removeById(param.getId());
    }

    @Override
    public void update(PictureUpdateRequest param) {
        Picture entity = getById(param.getId());
        if (Objects.isNull(entity)) {
            throw new RuntimeException("数据不存在");
        }

        // 字段填充
        BeanUtils.copyProperties(param, entity);

        updateById(entity);
    }

    @Override
    public PictureDetailVO queryDetail(BaseRequest param) {
        PictureDetailVO result = new PictureDetailVO();

        Picture entity = getById(param.getId());
        if (Objects.isNull(entity)) {
            throw new RuntimeException("该数据不存在");
        }

        // 字段填充
        BeanUtils.copyProperties(entity, result);

        return result;
    }

    @Override
    public IPage<PicturePageVO> queryPage(PicturePageRequest param) {
        IPage<Picture> page = new Page<>(param.getPageNo(), param.getPageSize());

        // 查询条件
        LambdaQueryWrapper<Picture> wrapper = Wrappers.lambdaQuery();

        // 查询数据并转换
        IPage<Picture> pageResult = page(page, wrapper);
        return pageResult.convert(item -> {
            PicturePageVO vo = new PicturePageVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        });
    }
}
