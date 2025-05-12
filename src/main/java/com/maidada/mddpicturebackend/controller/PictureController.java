package com.maidada.mddpicturebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maidada.mddpicturebackend.annotation.AuthCheck;
import com.maidada.mddpicturebackend.common.BaseRequest;
import com.maidada.mddpicturebackend.common.BaseResponse;
import com.maidada.mddpicturebackend.common.ResultUtils;
import com.maidada.mddpicturebackend.dto.picture.PicturePageRequest;
import com.maidada.mddpicturebackend.dto.picture.PictureUpdateRequest;
import com.maidada.mddpicturebackend.dto.picture.PictureUploadRequest;
import com.maidada.mddpicturebackend.enums.UserRoleEnum;
import com.maidada.mddpicturebackend.service.PictureService;
import com.maidada.mddpicturebackend.vo.picture.PictureDetailVO;
import com.maidada.mddpicturebackend.vo.picture.PicturePageVO;
import com.maidada.mddpicturebackend.vo.picture.PictureVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * [图片]控制层
 *
 * @author wulinxuan
 * @date 2025-05-12 20:02
 */
@Slf4j
@RestController
@RequestMapping("/picture")
@RequiredArgsConstructor
public class PictureController {

    private final PictureService pictureService;

    /**
     * 上传图片
     *
     * @param multipartFile 多部件文件
     * @param param         参数
     * @return {@link BaseResponse }<{@link PictureVO }>
     */
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("multipartFile") MultipartFile multipartFile,
                                                 @Valid PictureUploadRequest param) {
        PictureVO result = pictureService.upload(multipartFile, param);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<String> deletePicture(@Valid @RequestBody BaseRequest param) {
        pictureService.delete(param);
        return ResultUtils.success("删除成功");
    }

    @PostMapping("/update")
    public BaseResponse<String> updatePicture(@Valid @RequestBody PictureUpdateRequest param) {
        pictureService.update(param);
        return ResultUtils.success("更新成功");
    }

    @GetMapping("/detail")
    public BaseResponse<PictureDetailVO> queryPictureDetail(@Valid BaseRequest param) {
        PictureDetailVO result = pictureService.queryDetail(param);
        return ResultUtils.success(result);
    }

    @GetMapping("/page")
    public BaseResponse<IPage<PicturePageVO>> queryPicturePage(PicturePageRequest param) {
        IPage<PicturePageVO> result = pictureService.queryPage(param);
        return ResultUtils.success(result);
    }
}
