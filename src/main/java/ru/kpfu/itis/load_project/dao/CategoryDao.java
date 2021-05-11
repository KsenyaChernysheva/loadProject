package ru.kpfu.itis.load_project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.load_project.entity.Category;

public interface CategoryDao extends JpaRepository<Category, Integer> {

    Category findByName(String name);
}
