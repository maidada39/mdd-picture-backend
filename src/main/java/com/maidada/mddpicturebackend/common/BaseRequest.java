package com.maidada.mddpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的请求类
 */
@Data
public class BaseRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}