package com.example.zooavito.service.Category;

import com.example.zooavito.model.Category;
import com.example.zooavito.repository.CategoryRepository;
import com.example.zooavito.request.CategoryRequest;
import com.example.zooavito.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        // Проверка на существование категории с таким названием
        if (categoryRepository.findByTitle(request.getTitle()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Категория с названием '" + request.getTitle() + "' уже существует"
            );
        }

        Category category = new Category();
        category.setTitle(request.getTitle());

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponse.from(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Категория не найдена с id: " + id
                ));

        return CategoryResponse.from(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Категория не найдена с id: " + id
                ));

        // Проверка, не занято ли новое название другой категорией
        categoryRepository.findByTitle(request.getTitle())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Категория с названием '" + request.getTitle() + "' уже существует"
                        );
                    }
                });

        category.setTitle(request.getTitle());
        Category updatedCategory = categoryRepository.save(category);

        return CategoryResponse.from(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Категория не найдена с id: " + id
                ));

        // Проверка, есть ли объявления, использующие эту категорию
        if (!category.getAnnouncements().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Нельзя удалить категорию, которая используется в объявлениях"
            );
        }

        categoryRepository.delete(category);
    }
}
