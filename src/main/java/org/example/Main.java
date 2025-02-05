package org.example;

import java.util.Map;

import static org.example.LogMonitor.analyzeJobs;
import static org.example.LogMonitor.parseLogFile;

public class Main {
    public static void main(String[] args) {
        String filePath = "src/test/input/logs.log";

        Map<String, JobEntry> jobs = parseLogFile(filePath);
        analyzeJobs(jobs);
    }
}