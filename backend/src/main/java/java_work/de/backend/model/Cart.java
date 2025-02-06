package java_work.de.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

public record Cart(
        @Id ObjectId id,
        String userEmail, //  Jeder User hat seinen eigenen Warenkorb
        List<OrderItem> items //  Produkte im Warenkorb
) {
}
