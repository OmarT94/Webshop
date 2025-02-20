package java_work.de.backend.model;

import org.springframework.data.annotation.Id;

public record Category(
        @Id String id,
        String name
) {
}
