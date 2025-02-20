package java_work.de.backend.service;

import java_work.de.backend.dto.CategoryDTO;
import java_work.de.backend.model.Category;
import java_work.de.backend.repo.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServic {
    private final CategoryRepository categoryRepository;

    public CategoryServic(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category saveCategory(CategoryDTO categoryDTO) {
        Category newCategory= new Category(null,categoryDTO.name());
        return categoryRepository.save(newCategory);
    }

    public List<Category> getAllCategory(){
        return categoryRepository.findAll();
    }
}
