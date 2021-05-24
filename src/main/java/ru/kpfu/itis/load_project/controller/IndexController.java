package ru.kpfu.itis.load_project.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.load_project.entity.Category;
import ru.kpfu.itis.load_project.entity.Region;
import ru.kpfu.itis.load_project.entity.Statistic;
import ru.kpfu.itis.load_project.entity.TargetAudience;
import ru.kpfu.itis.load_project.entity.dto.ChartDataDto;
import ru.kpfu.itis.load_project.service.IndexService;
import ru.kpfu.itis.load_project.service.YandexWordstatService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@Log
@RequestMapping(value = "/index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @Autowired
    private YandexWordstatService yandexWordstatService;

    @PostMapping
    public ResponseEntity<ChartDataDto> region(@RequestParam(value = "selectedValues[]") List<Integer> ids,
                                               @RequestParam(value = "selectedTargetAudience") Integer idTargetAudience,
                                               @RequestParam(value = "selectedCategory") Integer idCategory) {
        Long peopleNumber = 0L;
        List<Statistic> fixedNumbers = null;
        try {
            peopleNumber = indexService.getPeopleNumber(ids, idTargetAudience);
            fixedNumbers = yandexWordstatService.getYandexWordstatNumberByRegionAndCategory(ids, idCategory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(indexService.getDistribution(peopleNumber, fixedNumbers), HttpStatus.OK);
    }

    @GetMapping
    public String region(Model model) throws  IOException {
        List<Region> regions = indexService.getAllRegions();
        List<TargetAudience> targetAudiences = indexService.getAllAudiences();
        List<Category> categories = indexService.getAllCategories();

        model.addAttribute("audiences", targetAudiences);
        model.addAttribute("answer", new ArrayList<Integer>());
        model.addAttribute("regions", regions);
        model.addAttribute("categories", categories);
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
