package com.maidada.mddpicturebackend.dto.picture;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * [图片]新增请求
 *
 * @author wulinxuan
 * @date 2025-05-12 20:02
 */
@Data
public class PictureUploadRequest {

    /**
     * id
     */
    private Long id;
}
