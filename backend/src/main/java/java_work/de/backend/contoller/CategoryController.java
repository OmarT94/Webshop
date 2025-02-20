package java_work.de.backend.contoller;

import jakarta.validation.Valid;
import java_work.de.backend.dto.CategoryDTO;
import java_work.de.backend.service.CategoryServic;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class CategoryController {
    private final CategoryServic categoryService;

    public CategoryController(CategoryServic categoryServic, CategoryServic categoryService) {
        this.categoryService = categoryService;
    }

    //Neue Kategorie erstellen
    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CategoryDTO> addCategory(@Valid @RequestBody CategoryDTO categoryDTO, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Fehler, wenn kein Admin
        }
        CategoryDTO savedCategory = categoryService.addCategory(categoryDTO);
        return ResponseEntity.ok(savedCategory);
    }

    // Alle Kategorien abrufen
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategory();

        if (categories.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>()); // Leere Liste zurückgeben, statt null
        }

        return ResponseEntity.ok(categories);
    }
}
