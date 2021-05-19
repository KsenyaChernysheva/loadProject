package ru.kpfu.itis.load_project.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.load_project.dao.CategoryDao;
import ru.kpfu.itis.load_project.dao.RegionDao;
import ru.kpfu.itis.load_project.dao.TargetAudienceDao;
import ru.kpfu.itis.load_project.entity.*;
import ru.kpfu.itis.load_project.entity.dto.ChartDataDto;

import javax.servlet.ServletContext;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class IndexService {
    @Autowired
    private RegionDao regionDao;

    @Autowired
    private TargetAudienceDao targetAudienceDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ServletContext context;

    private static final String ROSSTAT_SEARCH_QUERY = "https://rosstat.gov.ru/search?q=:query&date_from=&sections%5B%5D=204&content=doc&date_to=&search_by=all&sort=relevance";
    private static final String ROSSTAT_LINK_PREFIX = "https://rosstat.gov.ru";

    private static final String GEO_PREFIX = "RU-";

    public List<Region> getAllRegions() {
        return regionDao.findAllByOrderByNameAsc();
    }

    public List<TargetAudience> getAllAudiences() {
        return targetAudienceDao.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryDao.findAll();
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

    public ChartDataDto getDistribution(Long peopleNumber, List<Statistic> fixedNumbers) {
        Map<String, Long> pointMap = new HashMap<>();
        for (Statistic fixedNumber : fixedNumbers) {
            //\/\/\/\/ВОТ ЭТО ДЛЯ ДЕБАГА ЕТО НАДО УБРАТЬ ПОТОМ
            if (fixedNumber == null) {
                fixedNumber = Statistic.builder()
                        .regionId(72).numberOfQueries(50000L).categoryId(1)
                        .build();
            }
            //^^^^^^^
            Region region = regionDao.getOne(fixedNumber.getRegionId());
            Category category = categoryDao.getOne(fixedNumber.getCategoryId());

            String googleTrendsJson = getGoogleTrendsJson(context.getRealPath("/WEB-INF/js"),
                    category.getQuery(),
                    LocalDate.now().minusMonths(1L),
                    LocalDate.now(),
                    region.getGeo());
            Gson gson = new Gson();
            List<TimelineData> dayValues = gson.fromJson(googleTrendsJson, new TypeToken<List<TimelineData>>(){}.getType());
            int dayValuesSum = dayValues.stream().map(TimelineData::getValue).reduce((x, y) -> x + y).get();
            BigDecimal dayFixedNumber = BigDecimal.valueOf(dayValues.get(dayValues.size() - 1).getValue()).multiply(
                    (BigDecimal.valueOf(fixedNumber.getNumberOfQueries()).divide(BigDecimal.valueOf(dayValuesSum), 2, BigDecimal.ROUND_DOWN)));

            googleTrendsJson = getGoogleTrendsJson(context.getRealPath("/WEB-INF/js"),
                    category.getQuery(),
                    LocalDate.now().minusDays(1L),
                    LocalDate.now(),
                    region.getGeo());
            List<TimelineData> hourValues = gson.fromJson(googleTrendsJson, new TypeToken<List<TimelineData>>(){}.getType());
            int hourValuesSum = hourValues.stream().map(TimelineData::getValue).reduce((x, y) -> x + y).get();
            for (TimelineData hourValue : hourValues) {
                LocalDateTime time = hourValue.getTime();
                String pointName = time.toLocalTime().format(DateTimeFormatter.ISO_TIME);
                Long pointValue = BigDecimal.valueOf(hourValue.getValue()).multiply(
                        (dayFixedNumber.divide(BigDecimal.valueOf(hourValuesSum), 2, BigDecimal.ROUND_DOWN))).longValue();
                if (pointMap.containsKey(pointName)) {
                    pointMap.put(pointName, pointMap.get(pointName) + pointValue);
                } else {
                    pointMap.put(pointName, pointValue);
                }
            }
        }
        ChartDataDto chartDataDto = new ChartDataDto(new LinkedList<>(), new LinkedList<>());
        for (Map.Entry<String, Long> entry : pointMap.entrySet()) {
            chartDataDto.getPointNames().add(entry.getKey());
            chartDataDto.getPointValues().add(entry.getValue());
        }
        return chartDataDto;
    }

    public String getGoogleTrendsJson(String path, String query, LocalDate from, LocalDate to, String geo) {
        String result = "";
        try {
            String startDate = from.format(DateTimeFormatter.ISO_DATE);
            String endDate = to.format(DateTimeFormatter.ISO_DATE);
            String[] env = null;
            String[] callAndArgs = {"node", "app.js", query, startDate, endDate , GEO_PREFIX + geo};
            Process p = null;

            p = Runtime.getRuntime().exec(callAndArgs, env, new File(path));


            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));//getting the input

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));//getting the error

            result = stdInput.readLine();//reading the output
            String error = stdError.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }
}
