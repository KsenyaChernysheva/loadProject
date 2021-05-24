package ru.kpfu.itis.load_project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.load_project.entity.TargetAudience;

import java.util.List;

public interface TargetAudienceDao extends JpaRepository<TargetAudience, Integer> {

    TargetAudience findByName(String name);

    List<TargetAudience> findAllByOrderByNameAsc();
}
