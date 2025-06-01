package com.maidada.mddpicturebackend.dto.picture;

import lombok.Data;

/**
 * @author wulinxuan
 * @date 2025/5/31 17:45
 */
@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;
}

