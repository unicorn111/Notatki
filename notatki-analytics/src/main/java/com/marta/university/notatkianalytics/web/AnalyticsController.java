package com.marta.university.notatkianalytics.web;

import com.marta.university.notatkianalytics.service.AnalyticsService;
import com.marta.university.notatkianalytics.web.dto.AnalyticsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {
    @Autowired
    AnalyticsService analyticsService;

    @GetMapping("/analytics/{id}")
    public ResponseEntity<AnalyticsDto> getAnalytics(@PathVariable("id") String userId){
        return ResponseEntity.ok(analyticsService.getAnalytics(userId));
    }
}
