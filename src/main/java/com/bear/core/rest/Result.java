package com.bear.core.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author shomop
 * @date 2019/7/1 11:44
 */
public class Result<T> {
    private int code;
    private String info;
    private T data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] stack;

    public static <T> Result<T> success(T data) {
        return new Result<T>(CodeMsg.SUCCESS, data);
    }

    public static <T> Result<T> success() {
        return new Result<T>(CodeMsg.SUCCESS);
    }

    public static <T> Result<T> error(CodeMsg codeMsg) {
        return new Result<T>(codeMsg);
    }

    public static <T> Result<T> error(CodeMsg codeMsg, T data) {
        return new Result<T>(codeMsg, data);
    }


    private Result(CodeMsg codeMsg) {
        if (codeMsg == null){
            throw new NullPointerException("codeMsg can not be null");
        }
        this.code = codeMsg.getStatus();
        this.info = codeMsg.getMsg();
    }

    private Result(CodeMsg codeMsg, T data) {
        this(codeMsg);
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String[] getStack() {
        return stack;
    }

    public void setStack(String[] stack) {
        this.stack = stack;
    }
}