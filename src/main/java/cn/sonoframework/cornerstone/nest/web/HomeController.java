package cn.sonoframework.cornerstone.nest.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private static final String template = "fxxk u %s!";

    @RequestMapping(value = "/")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format(template, name);
    }

    @RequestMapping(value = "/test")
    public String test(@RequestParam(value = "name") String name) {
        return "asdf1";
    }
}