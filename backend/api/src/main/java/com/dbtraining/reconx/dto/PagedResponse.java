package com.dbtraining.reconx.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
    List<T> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static <E, T> PagedResponse<T> of(Page<E> springPage, Function<E, T> entityMapper) {
        return new PagedResponse<>(
            springPage.getContent().stream().map(entityMapper).toList(),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements(),
            springPage.getTotalPages()
        );
    }
}
