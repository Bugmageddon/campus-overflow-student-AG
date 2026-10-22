package com.campusoverflow.shared.page;

import java.util.List;
import java.util.function.Function;

/** 与框架无关的分页结果。page 从 1 开始。 */
public record PageResult<T>(List<T> items, long total, int page, int size) {

    public PageResult {
        items = List.copyOf(items);
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), 0, page, size);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(items.stream().map(mapper).toList(), total, page, size);
    }

    public long totalPages() {
        return size == 0 ? 0 : (total + size - 1) / size;
    }
}
