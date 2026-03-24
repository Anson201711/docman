package com.docman.common.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Page result wrapper
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<T> {

    private Pagination pagination;

    public PageResult() {
        super();
    }

    public PageResult(T data, Pagination pagination) {
        super(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
        this.pagination = pagination;
    }

    public static <T> PageResult<T> of(T data, long page, long pageSize, long total) {
        return new PageResult<>(data, new Pagination(page, pageSize, total));
    }

    public static <T> PageResult<T> of(T data, long page, long pageSize, long total, long totalPages) {
        return new PageResult<>(data, new Pagination(page, pageSize, total, totalPages));
    }

    @Data
    public static class Pagination {
        private long page;
        private long pageSize;
        private long total;
        private long totalPages;

        public Pagination() {
        }

        public Pagination(long page, long pageSize, long total) {
            this.page = page;
            this.pageSize = pageSize;
            this.total = total;
            this.totalPages = (total + pageSize - 1) / pageSize;
        }

        public Pagination(long page, long pageSize, long total, long totalPages) {
            this.page = page;
            this.pageSize = pageSize;
            this.total = total;
            this.totalPages = totalPages;
        }
    }
}
