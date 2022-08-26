package com.marta.university.notatkianalytics.service;

import com.marta.university.notatkianalytics.configs.MQConfig;
import com.marta.university.notatkianalytics.persistance.AnalyticsRepository;
import com.marta.university.notatkianalytics.persistance.model.Analytics;
import com.marta.university.notatkianalytics.web.dto.AnalyticsDto;
import com.marta.university.notatkianalytics.web.dto.NoteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnalyticsService {
    @Autowired
    private AnalyticsRepository analyticsRepository;

    @RabbitListener(queues = MQConfig.QUEUE)
    public void listener(NoteDto message) {
        log.info(message.toString());
        if(getAnalytics(message.getUserId()) == null){
            createAnalytics(message);
        }
        else{
            updateAnalytics(message, true);
        }
    }

    @RabbitListener(queues = MQConfig.SECOND_QUEUE)
    public void secondListener(NoteDto message) {
        log.info(message.toString());
        updateAnalytics(message, false);
    }

    public AnalyticsDto createAnalytics(NoteDto note){
        Analytics analytics = new Analytics();
        analytics.setUserId(note.getUserId());
        analytics.setCreatedQuantity(1);
        analytics.setTimesModified(1);
        analytics.setAvarageTextSize(note.getNote().length());
        analyticsRepository.save(analytics);
        log.info("Analytic was created id: {} createdQuantity: {} TimesModified: {} AvarageTextSize: {}",
                analytics.getUserId(), analytics.getCreatedQuantity(), analytics.getTimesModified(),
                analytics.getAvarageTextSize());
        return mapAnaliticsDto(analytics);
    }
    public AnalyticsDto updateAnalytics(NoteDto note, boolean isCreatingNote){
        Analytics analytics = analyticsRepository.findById(note.getUserId()).get();
        if(isCreatingNote){
            analytics.setCreatedQuantity(analytics.getCreatedQuantity() + 1);
        }
        analytics.setTimesModified(analytics.getTimesModified() + 1);
        analytics.setAvarageTextSize((analytics.getAvarageTextSize() + note.getNote().length())/2);
        analyticsRepository.deleteById(note.getUserId());
        analyticsRepository.save(analytics);
        log.info("Analytic was updated id: {} createdQuantity: {} TimesModified: {} AvarageTextSize: {}",
                analytics.getUserId(), analytics.getCreatedQuantity(), analytics.getTimesModified(),
                analytics.getAvarageTextSize());
        return mapAnaliticsDto(analytics);
    }
    private AnalyticsDto mapAnaliticsDto(Analytics analytics){
        AnalyticsDto analyticsDto = new AnalyticsDto();
        analyticsDto.setUserId(analytics.getUserId());
        analyticsDto.setCreatedQuantity(analytics.getCreatedQuantity());
        analyticsDto.setAvarageTextSize(analytics.getAvarageTextSize());
        analyticsDto.setTimesModified(analytics.getTimesModified());
        return analyticsDto;
    }
    public AnalyticsDto getAnalytics(String userId){
        if(analyticsRepository.findById(userId).isPresent()){
            return mapAnaliticsDto(analyticsRepository.findById(userId).get());
        }
        return null;
    }
}
