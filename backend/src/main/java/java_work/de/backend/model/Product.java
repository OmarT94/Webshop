package java_work.de.backend.model;

import org.springframework.data.annotation.Id;

public record Product(
        @Id
        String id,
        String name,
        String description,
        Double price,
        Integer stock,
        String image
) {
}
