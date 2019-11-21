package com.bear.core.page;


import java.util.List;

/**
 * @author shomop
 * @date 2019/7/1 17:27
 */
public class PageHelpUtils {

    private PageHelpUtils(){}

    public static PageInfo create(PageQuery pageQuery, List result){
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNum(pageQuery.getPage());
        pageInfo.setPageSize(pageQuery.getSize());
        pageInfo.setTotal(result.size());
        int from = (pageQuery.getPage() - 1) * pageQuery.getSize();
        int to = from + pageQuery.getSize();
        if (result.size() > from) {
            if (result.size() <= to) {
                to = result.size();
            }
            pageInfo.setList(result.subList(from, to));
        }
        return pageInfo;
    }
}
