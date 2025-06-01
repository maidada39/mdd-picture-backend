package com.maidada.mddpicturebackend.service.impl;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.maidada.mddpicturebackend.common.BaseRequest;
import com.maidada.mddpicturebackend.dto.file.UploadPictureResult;
import com.maidada.mddpicturebackend.dto.picture.PictureUploadByBatchRequest;
import com.maidada.mddpicturebackend.exception.BusinessException;
import com.maidada.mddpicturebackend.exception.ErrorCode;
import com.maidada.mddpicturebackend.exception.ThrowUtils;
import com.maidada.mddpicturebackend.manager.FileManager;
import com.maidada.mddpicturebackend.manager.upload.FilePictureUpload;
import com.maidada.mddpicturebackend.manager.upload.PicutreUploadTemplate;
import com.maidada.mddpicturebackend.manager.upload.UrlPictureUpload;
import com.maidada.mddpicturebackend.service.UserService;
import com.maidada.mddpicturebackend.vo.picture.PictureVO;
import com.maidada.mddpicturebackend.vo.user.UserLoginVO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import org.springframework.util.DigestUtils;
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

    @Resource
    private FilePictureUpload  filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 本地缓存
     */
    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L) // 最大 10000 条
            // 缓存 5 分钟后移除
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    @Override
    public PictureVO upload(Object inputSource, PictureUploadRequest param) {
        // 校验
        ThrowUtils.throwIf(Objects.isNull(inputSource), ErrorCode.PARAMS_ERROR, "图片文件不能为空");

        // 如果传递了id表示更新操作，判断是否存在
        Long id = param.getId();
        if (Objects.nonNull(id)) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, id).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "图片不存在");
        }

        // 上传文件
        UserLoginVO loginUser = userService.getLoginUser();
        String prefix = String.format("public/%s", loginUser.getId());
        PicutreUploadTemplate uploadTemplate;
        if (inputSource instanceof String) {
            uploadTemplate =  urlPictureUpload;
        } else if (inputSource instanceof MultipartFile) {
            uploadTemplate = filePictureUpload;
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件类型异常");
        }
        UploadPictureResult uploadPictureResult = uploadTemplate.uploadPicture(inputSource, prefix);

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
    public Integer batchSearch(PictureUploadByBatchRequest param) {
        // 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", param.getSearchText());
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");

        // 遍历元素，依次处理上传图片
        int uploadCount = 0;
        Integer count = param.getCount();
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过：{}", fileUrl);
                continue;
            }
            // 处理图片的地址，防止转义或者和对象存储冲突的问题
            // codefather.cn?yupi=dog，应该只保留 codefather.cn
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setUrl(fileUrl);
            try {
                PictureVO pictureVO = this.upload(fileUrl, pictureUploadRequest);
                log.info("图片上传成功，id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
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

        // 查询缓存没有在查询数据库
        String condition = JSONUtil.toJsonStr(param);
        condition = DigestUtils.md5DigestAsHex(condition.getBytes());
        String key = String.format("mddpicutre:picutre:page:%s", condition);
        String cache;
        // 先查本地缓存
        cache = LOCAL_CACHE.getIfPresent(key);
        if (cache != null) {
            log.info("从本地缓存中获取数据,查询条件: {}", condition);
            return JSONUtil.toBean(cache, Page.class);
        }
        // 再差分布式缓存
        cache = stringRedisTemplate.opsForValue().get(key);
         if (cache != null) {
            log.info("从分布式缓存中获取数据,查询条件: {}", condition);
            Page<PicturePageVO> result = JSONUtil.toBean(cache, Page.class);
            // 写入本地缓存
            LOCAL_CACHE.put(key, JSONUtil.toJsonStr(result));

            return result;
        }

        // 查询条件
        LambdaQueryWrapper<Picture> wrapper = Wrappers.lambdaQuery();
        // 查询数据并转换
        IPage<Picture> pageResult = page(page, wrapper);
        IPage<PicturePageVO> result = pageResult.convert(item -> {
            PicturePageVO vo = new PicturePageVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        });

        // 缓存，设置随机过期时间，防止缓存雪崩
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(result), 300 + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS);
        LOCAL_CACHE.put(key, JSONUtil.toJsonStr(result));

        return result;
    }
}
