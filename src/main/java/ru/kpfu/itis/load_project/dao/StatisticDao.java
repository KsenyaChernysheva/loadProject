package ru.kpfu.itis.load_project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.load_project.entity.Statistic;

public interface StatisticDao extends JpaRepository<Statistic, Integer> {

    Statistic findByRegionIdAndCategoryId(Integer regionId, Integer categoryId);

}
