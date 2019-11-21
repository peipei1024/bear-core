package com.bear.core.page;

/**
 * @author shomop
 * @date 2019/6/28 11:03
 */
public class PageQuery {
    private Integer page;
    private Integer size;

    public Integer getPage() {
        return page == null ? 1 : page;
    }

    public Integer getSize() {
        return size == null ? 10 : size;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
