package com.marta.university.notatkianalytics.web.dto;

import lombok.Data;

@Data
public class AnalyticsDto {
    private String userId;
    private int avarageTextSize;
    private int createdQuantity;
    private int timesModified;
}
