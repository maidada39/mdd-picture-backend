package com.maidada.mddpicturebackend.common;

import lombok.Data;

/**
 * 通用的分页请求类
 */
@Data
public class BasePageRequest {

    /**
     * 当前页号
     */
    private int pageNo = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";
}