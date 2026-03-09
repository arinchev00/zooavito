package com.example.zooavito.repository;

import com.example.zooavito.model.Subcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    List<Subcategory> findByCategoryIdOrderByTitleAsc(Long categoryId);

    Page<Subcategory> findByCategoryId(Long categoryId, Pageable pageable);

    boolean existsByTitleAndCategoryId(String title, Long categoryId);

    @Query("SELECT s FROM Subcategory s JOIN s.category c ORDER BY c.title ASC, s.title ASC")
    List<Subcategory> findAllByOrderByCategoryTitleAscTitleAsc();
}
