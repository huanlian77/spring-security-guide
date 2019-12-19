package com.liuqs.spring.security.jwt.vo;

import com.liuqs.spring.security.jwt.constant.ResultConstant;
import lombok.Data;

@Data
public class Result<T> {
    /**
     * 成功状态码
     */
    private static final int SUCCESS_CODE = 200;

    private int code;
    private String message;
    private T data;


    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultConstant.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> success(T data) {
        ResultConstant resultConstant = ResultConstant.SUCCESS;
        return new Result<>(resultConstant.getCode(), resultConstant.getDesc(), data);
    }

    public static Result error(ResultConstant resultConstant, String message) {
        return new Result(resultConstant.getCode(), resultConstant.getDesc() + ":" + message);
    }

    public static Result error(ResultConstant resultConstant) {
        return new Result(resultConstant.getCode(), resultConstant.getDesc());
    }


}