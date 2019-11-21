package com.bear.core.rest;


/**
 * @author shomop
 * @date 2019/7/1 11:44
 */
public class CodeMsg {

    private int status;
    private String msg;

    public static final CodeMsg SUCCESS = new CodeMsg(0, "操作成功");
    public static final CodeMsg SERVER_ERROR = new CodeMsg(500, "操作失败：%s");
    public static final CodeMsg PARAMS_ERROR = new CodeMsg(400, "参数校验异常：%s");

    private CodeMsg(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }


    public CodeMsg fillArgs(Object... args) {
        if (this.msg == null){
            throw new NullPointerException("codeMsg msg can not be null");
        }
        String message = String.format(this.msg, args);
        return new CodeMsg(this.status, message);
    }
}
