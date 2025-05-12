package com.maidada.mddpicturebackend.dto.picture;

import com.maidada.mddpicturebackend.common.BasePageRequest;
import lombok.Data;

import java.util.Date;

/**
 * [图片]列表请求
 *
 * @author wulinxuan
 * @date 2025-05-12 20:02
 */
@Data
public class PicturePageRequest extends BasePageRequest {

    /**
     * 图片名称
     */
    private String name;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;
}
