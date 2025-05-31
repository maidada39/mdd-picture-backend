package com.maidada.mddpicturebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maidada.mddpicturebackend.common.BaseRequest;
import com.maidada.mddpicturebackend.dto.picture.PictureUploadRequest;
import com.maidada.mddpicturebackend.dto.picture.PicturePageRequest;
import com.maidada.mddpicturebackend.dto.picture.PictureUpdateRequest;
import com.maidada.mddpicturebackend.entity.Picture;
import com.maidada.mddpicturebackend.vo.picture.PictureDetailVO;
import com.maidada.mddpicturebackend.vo.picture.PicturePageVO;
import com.maidada.mddpicturebackend.vo.picture.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * [图片]服务接口
 *
 * @author wulinxuan
 * @date 2025-05-12 20:02
 */
public interface PictureService extends IService<Picture> {

    PictureVO upload(Object inputSource, PictureUploadRequest param);

    void delete(BaseRequest param);

    void update(PictureUpdateRequest param);

    PictureDetailVO queryDetail(BaseRequest param);

    IPage<PicturePageVO> queryPage(PicturePageRequest param);
}
