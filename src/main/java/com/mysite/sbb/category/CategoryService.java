package com.mysite.sbb.category;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        this.categoryRepository.save(category);
        return category;
    }

    public List<Category> getCategoryList() {
        return this.categoryRepository.findAll();
    }
    
    public Category getCategoryByName(String name) {
        Optional<Category> category = this.categoryRepository.findByName(name);
        if (category .isPresent()) {
            return category .get();
        } else {
            throw new DataNotFoundException("category not found");
        }
    }

    public Category categoryModify(Category category, String name) {
        category.setName(name);
        this.categoryRepository.save(category);
        return category;
    }
    
    public void categoryDelete(Category category) {
        this.categoryRepository.delete(category);
    }
}
