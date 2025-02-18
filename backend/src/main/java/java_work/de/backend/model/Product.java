package java_work.de.backend.model;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

public record Product(
        @Id
        ObjectId id, //  MongoDB generiert automatisch `ObjectId`
        String name,
        String description,
        Double price,
        Integer stock,
        List<String> images // Bild als Base64 speichern
)
{

}
