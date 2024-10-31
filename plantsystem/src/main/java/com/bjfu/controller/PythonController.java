package com.bjfu.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class PythonController {

    private final RestTemplate restTemplate = new RestTemplate();
    private Process pythonProcess;

    @PostConstruct
    public void startPythonServer() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "C:\\Users\\97964\\Desktop\\system\\plantsystem\\src\\main\\resources\\static\\flaskServe.py");
            processBuilder.redirectErrorStream(true);  // 将错误输出重定向到标准输出
            pythonProcess = processBuilder.start();
            System.out.println("Python service started successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/retrieve")
    public String retrieve(@RequestParam String queryText) {
        String pythonServiceUrl = "http://localhost:5000/retrieve?query_text=" + queryText;
        return restTemplate.getForObject(pythonServiceUrl, String.class);
    }

    @PreDestroy
    public void stopPythonServer() {
        if (pythonProcess != null) {
            pythonProcess.destroy();
            System.out.println("Python service stopped successfully.");
        }
    }
}
