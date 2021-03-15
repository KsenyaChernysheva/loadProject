package ru.kpfu.itis.load_project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kpfu.itis.load_project.dao.RegionDao;
import ru.kpfu.itis.load_project.entity.Region;

import java.util.ArrayList;
import java.util.List;

@Controller
@Log
@RequestMapping(value = "/index")
public class IndexController {

    @Autowired
    private RegionDao regionDao;

    @PostMapping
    public ResponseEntity<Integer> region(@RequestParam(value="selectedValues[]") Integer[] ids) {
        return new ResponseEntity<Integer>(ids.length, HttpStatus.OK);
    }

    @GetMapping
    public String region(Model model) {
        List<Region> regions = regionDao.findAll();

        model.addAttribute("answer", new ArrayList<Integer>());
        model.addAttribute("regions", regions);
        return "index";
    }

}
