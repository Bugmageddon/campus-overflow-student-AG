package com.campusoverflow.shared.page;

/** 分页参数，统一做边界保护：page ≥ 1，1 ≤ size ≤ 50。 */
public record PageRequest(int page, int size) {

    public static final int MAX_SIZE = 50;

    public PageRequest {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), MAX_SIZE);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
