package ru.kpfu.itis.load_project.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.load_project.dao.RegionDao;
import ru.kpfu.itis.load_project.dao.TargetAudienceDao;
import ru.kpfu.itis.load_project.entity.Region;
import ru.kpfu.itis.load_project.entity.TargetAudience;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndexService {
    @Autowired
    private RegionDao regionDao;

    @Autowired
    private TargetAudienceDao targetAudienceDao;

    private static final String ROSSTAT_SEARCH_QUERY = "https://rosstat.gov.ru/search?q=:query&date_from=&sections%5B%5D=204&content=doc&date_to=&search_by=all&sort=relevance";
    private static final String ROSSTAT_LINK_PREFIX = "https://rosstat.gov.ru";

    public List<Region> getAllRegions() {
        return regionDao.findAllByOrderByNameAsc();
    }

    public List<TargetAudience> getAllAudiences() {
        return targetAudienceDao.findAll();
    }

    public Long getPeopleNumber(List<Integer> regionIds, List<Integer> audienceIds) throws IOException {
        List<Region> regions = regionDao.findAllById(regionIds);
        List<TargetAudience> audiences = targetAudienceDao.findAllById(audienceIds);

        Long peopleNumber = 0L;
        for (TargetAudience audience : audiences) {
            peopleNumber += getPeopleNumberInAudience(regions, audience.getQuery());
        }
        return peopleNumber;
    }

    private Long getPeopleNumberInAudience(List<Region> regions, String audienceQuery) throws IOException {
        String query = ROSSTAT_SEARCH_QUERY.replace(":query", audienceQuery);
        Document doc = Jsoup.connect(query).userAgent("Chrome/4.0.249.0 Safari/532.5").get();
        Element statisticsLink = doc.body().getElementsByClass("list__items").get(0)
                .getElementsByTag("a").first();
        URL statisticsURL = new URL(ROSSTAT_LINK_PREFIX + statisticsLink.attr("href"));

        List<Region> regionsWithParent = regions.stream()
                .filter(region -> region.getParentId() != null).collect(Collectors.toList());
        for (Region regionWithParent : regionsWithParent) {
            if (regions.stream().map(region -> region.getId())
                    .anyMatch(regionId -> regionId.equals(regionWithParent.getParentId()))) {
                regions.remove(regionWithParent);
            }
        }

        InputStream in = statisticsURL.openStream();
        Workbook workbook = new HSSFWorkbook(in);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        List<String> regionStrings = regions.stream().map(region -> region.getDbName()).collect(Collectors.toList());
        Long peopleNumberInAudience = 0L;
        while (iterator.hasNext()) {
            String stringToRemove = null;
            Row row = iterator.next();
            Cell cell = row.getCell(0);
            if (cell == null) continue;
            for (String regionString : regionStrings) {
                if (cell.getStringCellValue().contains(regionString)) {
                    Cell dataCell = row.getCell(row.getLastCellNum() - 1);
                    peopleNumberInAudience += new Double(dataCell.getNumericCellValue() * 1000).longValue();
                    stringToRemove = regionString;
                    break;
                }
            }
            if (stringToRemove != null) regionStrings.remove(stringToRemove);
            if (regionStrings.isEmpty()) break;
        }

        return peopleNumberInAudience;
    }

    public Long getDistribution(Long peopleNumber) {
        return peopleNumber;
    }
}
