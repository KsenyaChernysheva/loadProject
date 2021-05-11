package ru.kpfu.itis.load_project.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.load_project.dao.CategoryDao;
import ru.kpfu.itis.load_project.dao.RegionDao;
import ru.kpfu.itis.load_project.dao.StatisticDao;
import ru.kpfu.itis.load_project.entity.Category;
import ru.kpfu.itis.load_project.entity.Region;
import ru.kpfu.itis.load_project.entity.Statistic;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class YandexWordstatService {

    private static final String YANDEX_WORDSTAT_LINK = "https://wordstat.yandex.ru/";
    private static final String EMAIL = "gudddar@yandex.ru";
    private static final String PASSWORD = "17032010";

    @Autowired
    private StatisticDao statisticDao;

    @Autowired
    private RegionDao regionDao;

    @Autowired
    private CategoryDao categoryDao;

    @PostConstruct
    public void init() {
        //fillStatisticsTable();
    }

    public List<Statistic> getYandexWordstatNumberByRegionAndCategory(List<Integer> regionIds, Integer categoryId) {
        List<Statistic> statistics = new LinkedList<>();
        for (Integer regionId : regionIds) {
            Statistic statistic = statisticDao.findByRegionIdAndCategoryId(regionId, categoryId);
            statistics.add(statistic);
        }
        return statistics;
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void fillStatisticsTable() {
        List<Region> regions = new LinkedList<>();
        regions.add(regionDao.findByName("Тульская область"));
        List<Category> categories = categoryDao.findAll();
        String regionStrings = regions.stream().map(Region::getName).reduce((x, y) -> x + "," + y).get();
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\ksch2\\IdeaProjects\\geckodriver0291.exe");
        FirefoxOptions ffo = new FirefoxOptions();
        ffo.setCapability("marionette", true);

        WebDriver driver = new FirefoxDriver(ffo);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Map<String, Object> vars = new HashMap<String, Object>();

        WebDriverWait wait = new WebDriverWait(driver, 10);

        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        driver.get(YANDEX_WORDSTAT_LINK);
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".b-search__type[value=regions]")));
        driver.findElement(By.cssSelector(".b-search__type[value=regions]")).click();
        driver.findElement(By.cssSelector("#b-domik_popup-username")).sendKeys(EMAIL);
        driver.findElement(By.cssSelector("#b-domik_popup-password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector(".b-form-button__input:not([hidefocus=true])")).click();
        for (Category category : categories) {
            driver.findElement(By.cssSelector(".b-form-input__input[name=text]")).sendKeys(category.getQuery());
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".b-search__type[value=regions]")));
            driver.findElement(By.cssSelector(".b-form-button > .b-form-button__input")).click();

            elem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".b-regions-statistic__list")));
            WebElement element = driver.findElement(By.cssSelector(".b-regions-statistic__table"));
            List<WebElement> rows = element.findElements(By.cssSelector(".b-regions-statistic__tr"));
            Map<String, WebElement> filteredRows = rows.stream()
                    .filter(row -> (row.findElements(By.cssSelector(".b-regions-statistic__td_type_cities")).size() > 0 &&
                            regionStrings.contains(row.findElement(By.cssSelector(".b-regions-statistic__td_type_cities")).getText())) ||
                            (row.findElements(By.cssSelector(".b-regions-statistic__td_type_regions")).size() > 0 &&
                                    regionStrings.contains(row.findElement(By.cssSelector(".b-regions-statistic__td_type_regions")).getText())))
                    .collect(Collectors.toMap((row -> row.findElements(By.cssSelector(".b-regions-statistic__td_type_cities")).size() > 0 ?
                                    row.findElement(By.cssSelector(".b-regions-statistic__td_type_cities")).getText() : row.findElement(By.cssSelector(".b-regions-statistic__td_type_regions")).getText()),
                            (row -> row)));

            for (Map.Entry<String, WebElement> filteredRow : filteredRows.entrySet()) {
                Integer regionId = regions.stream().filter(region -> region.getName().equals(filteredRow.getKey())).findAny().get().getId();
                Long numberOfQueries = Long.parseLong(filteredRow.getValue().findElement(By.cssSelector(".b-regions-statistic__td_type_number")).getText().replaceAll(" ", ""));
                Statistic statistic = statisticDao.findByRegionIdAndCategoryId(regionId, category.getId());
                if (statistic == null) {
                    statistic = Statistic.builder().categoryId(category.getId())
                            .regionId(regionId).numberOfQueries(numberOfQueries).build();
                } else {
                    statistic.setNumberOfQueries(numberOfQueries);
                }
                statisticDao.save(statistic);
            }
        }
    }

}
