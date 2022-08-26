package com.marta.university.notatkianalytics.persistance;

import com.marta.university.notatkianalytics.persistance.model.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsRepository extends JpaRepository<Analytics, String> {
}
