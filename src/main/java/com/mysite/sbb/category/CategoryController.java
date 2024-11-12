package com.mysite.sbb.category;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("category")
public class CategoryController {
    @PostMapping("/create")
    public String createCategory(CategoryForm categoryForm, BindingResult bindingResult) {
        return "";
    }
}
