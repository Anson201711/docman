package com.docman.common.util;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Pagination utility
 */
public class PageUtil {

    private PageUtil() {
    }

    /**
     * Convert MyBatis-Plus Page to list
     */
    public static <T> Page<T> of(int page, int pageSize) {
        return new Page<>(page, pageSize);
    }

    /**
     * Convert list to page
     */
    public static <T> Page<T> of(List<T> list, long current, long size) {
        Page<T> page = new Page<>(current, size);
        page.setRecords(list);
        page.setTotal(list.size());
        return page;
    }

    /**
     * Convert page records
     */
    public static <T, R> Page<R> convert(Page<T> page, Function<T, R> converter) {
        Page<R> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList()));
        return result;
    }

    /**
     * Copy page with BeanUtil
     */
    public static <T, R> Page<R> copyPage(Page<T> page, Class<R> targetClass) {
        Page<R> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(BeanUtil.copyToList(page.getRecords(), targetClass));
        return result;
    }
}
