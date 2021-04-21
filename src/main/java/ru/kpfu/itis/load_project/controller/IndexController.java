package ru.kpfu.itis.load_project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.load_project.dao.RegionDao;
import ru.kpfu.itis.load_project.entity.Region;
import ru.kpfu.itis.load_project.entity.TargetAudience;
import ru.kpfu.itis.load_project.service.IndexService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@Log
@RequestMapping(value = "/index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @Autowired
    ServletContext context;

    @PostMapping
    public ResponseEntity<List<Long>> region(@RequestParam(value = "selectedValues[]") List<Integer> ids,
                                         @RequestParam(value = "selectedValuesTargetAudience[]") List<Integer> idsTargetAudience) {
        Long peopleNumber = 0L;
        Map<Region, Long> fixedNumbers = null;
        try {
            peopleNumber = indexService.getPeopleNumber(ids, idsTargetAudience);
            fixedNumbers = indexService.getYandexWordstatNumberByRegion(ids);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(indexService.getDistribution(peopleNumber, fixedNumbers), HttpStatus.OK);
    }

    @GetMapping
    public String region(Model model) throws IOException {
        String result = indexService.getGoogleTrendsJson(context.getRealPath("/WEB-INF/js"));

        List<Region> regions = indexService.getAllRegions();
        List<TargetAudience> targetAudiences = indexService.getAllAudiences();

        model.addAttribute("audiences", targetAudiences);
        model.addAttribute("answer", new ArrayList<Integer>());
        model.addAttribute("regions", regions);
        model.addAttribute("resultString", result);
        return "index";
    }

//    public boolean waitForJSandJQueryToLoad(WebDriver driver) {
//
//        WebDriverWait wait = new WebDriverWait(driver, 30);
//
//        // wait for jQuery to load
//        ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
//            @Override
//            public Boolean apply(WebDriver driver) {
//                try {
//                    return ((Long)((JavascriptExecutor)driver).executeScript("return jQuery.active") == 0);
//                }
//                catch (Exception e) {
//                    // no jQuery present
//                    return true;
//                }
//            }
//        };
//
//        // wait for Javascript to load
//        ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
//            @Override
//            public Boolean apply(WebDriver driver) {
//                return ((JavascriptExecutor)driver).executeScript("return document.readyState")
//                        .toString().equals("complete");
//            }
//        };
//
//        return wait.until(jQueryLoad) && wait.until(jsLoad);
//    }

}
