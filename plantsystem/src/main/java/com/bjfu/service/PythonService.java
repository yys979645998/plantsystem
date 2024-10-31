package com.bjfu.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;

@Service
public class PythonService {

    private Process pythonProcess;

    @PostConstruct
    public void startPythonService() {
        new Thread(() -> {
            try {
                String pythonPath = System.getenv("PYTHON_HOME");
                if (pythonPath == null) {
                    pythonPath = "python";
                } else {
                    pythonPath = pythonPath + File.separator + "python.exe";
                }

                String scriptPath = "flaskServe.py";
                String projectRoot = System.getProperty("user.dir");

                ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath);
                processBuilder.directory(new File(projectRoot, "src/main/resources/static"));
                processBuilder.redirectErrorStream(true);
                pythonProcess = processBuilder.start();

                // Python服务输出日志
                new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))
                        .lines().forEach(System.out::println);

                System.out.println("Python service started successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @PreDestroy
    public void stopPythonService() {
        if (pythonProcess != null) {
            pythonProcess.destroy();
            System.out.println("Python service stopped successfully.");
        }
    }
}
