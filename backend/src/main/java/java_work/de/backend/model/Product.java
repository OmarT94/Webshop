package java_work.de.backend.model;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public record Product(
        @Id
        ObjectId id, // ✅ MongoDB generiert automatisch `ObjectId`
        String name,
        String description,
        Double price,
        Integer stock,
        String image
)
{

}
