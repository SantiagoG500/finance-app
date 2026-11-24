package com.finance.transaction.domain.model;

import java.util.List;

public record PaginatedMetaAhorro(
        List<MetaAhorro> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
