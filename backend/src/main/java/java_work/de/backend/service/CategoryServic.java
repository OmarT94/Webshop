package java_work.de.backend.service;

import java_work.de.backend.dto.CategoryDTO;
import java_work.de.backend.model.Category;
import java_work.de.backend.repo.CategoryRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServic {
    private final CategoryRepository categoryRepository;

    public CategoryServic(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO addCategory(CategoryDTO categoryDTO) {
        Category category = new Category(new ObjectId().toHexString(), categoryDTO.name());
        Category savedCategory = categoryRepository.save(category);

        return mapToDTO(savedCategory); //  Hier wandeln wir die gespeicherte `Category` in `CategoryDTO` um
    }

    public List<CategoryDTO> getAllCategory() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(this::mapToDTO )
                .collect(Collectors.toList());
    }


    // Hilfsmethode zur Umwandlung von Category → CategoryDTO
    private CategoryDTO mapToDTO(Category category) {
        return new CategoryDTO(category.id(), category.name());
    }
}
