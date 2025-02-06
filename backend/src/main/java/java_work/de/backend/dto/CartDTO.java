package java_work.de.backend.dto;

import java_work.de.backend.model.OrderItem;

import java.util.List;

public record CartDTO(
        String id,
        String userEmail,
        List<OrderItem> items
) {
}
