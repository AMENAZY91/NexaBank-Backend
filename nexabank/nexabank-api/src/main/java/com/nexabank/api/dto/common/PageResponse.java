package com.nexabank.api.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta paginada genérica.
 *
 * <p>No retornar colecciones financieras ilimitadas.
 *
 * <pre>
 * {
 *   "data": [],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 5000,
 *   "totalPages": 250
 * }
 * </pre>
 *
 * @param <T> el tipo de los elementos en la página
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    /**
     * Crea una PageResponse a partir de una lista total y parámetros de paginación.
     */
    public static <T> PageResponse<T> of(List<T> allItems, int page, int size) {
        int totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<T> pageData = allItems.subList(fromIndex, toIndex);

        return PageResponse.<T>builder()
                .data(pageData)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
