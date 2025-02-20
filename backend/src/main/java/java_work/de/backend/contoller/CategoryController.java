package java_work.de.backend.contoller;

import java_work.de.backend.dto.CategoryDTO;
import java_work.de.backend.model.Category;
import java_work.de.backend.service.CategoryServic;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryServic categoryService;

    public CategoryController(CategoryServic categoryServic, CategoryServic categoryService) {
        this.categoryService = categoryService;
    }

    //Neue Kategorie erstellen
    @PostMapping
    public Category addCategory(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.saveCategory(categoryDTO);
    }

    // Alle Kategorien abrufen
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategory();
    }
}
