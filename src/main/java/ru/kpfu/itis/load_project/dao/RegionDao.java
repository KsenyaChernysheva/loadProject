package ru.kpfu.itis.load_project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kpfu.itis.load_project.entity.Region;

@Repository
public interface RegionDao extends JpaRepository<Region, Integer> {

    Region findByName(String name);

}
