package ru.kpfu.itis.load_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.load_project.dao.RegionDao;
import ru.kpfu.itis.load_project.dao.TargetAudienceDao;
import ru.kpfu.itis.load_project.entity.Region;
import ru.kpfu.itis.load_project.entity.TargetAudience;

import java.util.List;

@Service
public class IndexService {
    @Autowired
    private RegionDao regionDao;

    @Autowired
    private TargetAudienceDao targetAudienceDao;

    public List<Region> getAllRegions() {
        return regionDao.findAll();
    }

    public List<TargetAudience> getAllAudiences() {
        return targetAudienceDao.findAll();
    }

    public Long getPeopleNumber(Integer[] regionIds, Integer[] audienceIds) {
        //TODO:API
        return Long.valueOf(1000);
    }

    public Double getDistribution(Long peopleNumber) {
        return peopleNumber * Math.random();
    }
}
