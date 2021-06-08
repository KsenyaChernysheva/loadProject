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
import org.springframework.scheduling.annotation.EnableScheduling;
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
@EnableScheduling
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
        List<Region> regions = regionDao.findAll();
        List<Category> categories = categoryDao.findAll();
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\ksch2\\IdeaProjects\\geckodriver0291.exe");
        FirefoxOptions ffo = new FirefoxOptions();
        ffo.setCapability("marionette", true);

        WebDriver driver = new FirefoxDriver(ffo);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Map<String, Object> vars = new HashMap<String, Object>();

        WebDriverWait wait = new WebDriverWait(driver, 10);

        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        LoginThread loginThread = new LoginThread(driver, wait);
        loginThread.run();
        for (Category category : categories) {
            List<String> regionStrings = regions.stream().map(Region::getName).collect(Collectors.toList());
            driver.findElement(By.cssSelector(".b-form-input__input[name=text]")).clear();
            driver.findElement(By.cssSelector(".b-form-input__input[name=text]")).sendKeys(category.getQuery());
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".b-search__type[value=regions]")));
            driver.findElement(By.cssSelector(".b-form-button > .b-form-button__input")).click();

            Waiter waiter = new Waiter(10000);
            waiter.run();

            WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".b-regions-statistic__list")));
            WebElement element = driver.findElement(By.cssSelector(".b-regions-statistic__table"));
            List<WebElement> rows = element.findElements(By.cssSelector(".b-regions-statistic__tr"));
            for (WebElement row : rows) {
                if (regionStrings.isEmpty()) break;
                if (row.findElements(By.cssSelector(".b-regions-statistic__td_type_cities")).size() > 0 &&
                        regionStrings.contains(row.findElement(By.cssSelector(".b-regions-statistic__td_type_cities")).getText())) {
                    String regionName = row.findElement(By.cssSelector(".b-regions-statistic__td_type_cities")).getText();
                    Integer regionId = regions.stream().filter(region -> region.getName().equals(regionName)).findAny().get().getId();
                    Long numberOfQueries = Long.parseLong(row.findElement(By.cssSelector(".b-regions-statistic__td_type_number")).getText().replaceAll(" ", ""));
                    saveStatistic(regionId, category, numberOfQueries);
                    regionStrings.remove(regionName);
                } else if (row.findElements(By.cssSelector(".b-regions-statistic__td_type_regions")).size() > 0 &&
                        regionStrings.contains(row.findElement(By.cssSelector(".b-regions-statistic__td_type_regions")).getText())) {
                    String regionName = row.findElement(By.cssSelector(".b-regions-statistic__td_type_regions")).getText();
                    Integer regionId = regions.stream().filter(region -> region.getName().equals(regionName)).findAny().get().getId();
                    Long numberOfQueries = Long.parseLong(row.findElement(By.cssSelector(".b-regions-statistic__td_type_number")).getText().replaceAll(" ", ""));
                    saveStatistic(regionId, category, numberOfQueries);
                    regionStrings.remove(regionName);
                }
            }
        }
        driver.quit();
    }

    private void saveStatistic(Integer regionId, Category category, Long numberOfQueries) {
        Statistic statistic = statisticDao.findByRegionIdAndCategoryId(regionId, category.getId());
        if (statistic == null) {
            statistic = Statistic.builder().categoryId(category.getId())
                    .regionId(regionId).numberOfQueries(numberOfQueries).build();
        } else {
            statistic.setNumberOfQueries(numberOfQueries);
        }
        statisticDao.save(statistic);
        System.out.println("saved for region " + regionId + " and category " + category.getName());
    }

    private class LoginThread extends Thread {
        WebDriver driver;
        WebDriverWait wait;

        public LoginThread(WebDriver driver, WebDriverWait wait) {
            this.driver = driver;
            this.wait = wait;
        }

        @Override
        public void run() {
            try {
                driver.get(YANDEX_WORDSTAT_LINK);
                boolean isLogged = false;
                while (!isLogged) {
                    if (driver.findElement(By.cssSelector(".b-popupa__refresh-button")).isDisplayed()) {
                        driver.navigate().refresh();
                        Thread.sleep(10000);
                        continue;
                    } else {
                        isLogged = true;
                    }
                    WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".b-search__type[value=regions]")));
                    driver.findElement(By.cssSelector(".b-search__type[value=regions]")).click();
                    if (driver.findElements(By.cssSelector("#b-domik_popup-username")).size() > 0 &&
                            driver.findElement(By.cssSelector("#b-domik_popup-username")).isDisplayed()) {
                        for (char c : EMAIL.toCharArray()) {
                            driver.findElement(By.cssSelector("#b-domik_popup-username")).sendKeys(Character.toString(c));
                            Thread.sleep(100);
                        }
                        for (char c : PASSWORD.toCharArray()) {
                            driver.findElement(By.cssSelector("#b-domik_popup-password")).sendKeys(Character.toString(c));
                            Thread.sleep(100);
                        }
                        driver.findElement(By.cssSelector(".b-domik__button")).findElements(By.cssSelector(".b-form-button__input:not([hidefocus=true])")).get(0).click();
                    }
                    if (driver.findElement(By.cssSelector(".b-popupa__refresh-button")).isDisplayed()) {
                        driver.navigate().refresh();
                        Thread.sleep(10000);
                    } else {
                        isLogged = true;
                    }
                }
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class Waiter extends Thread {
        long millis;

        public Waiter(long millis) {
            this.millis = millis;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
