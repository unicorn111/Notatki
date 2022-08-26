package com.marta.university.notatkibot;

import com.marta.university.notatkibot.dto.AnalyticsDto;
import com.marta.university.notatkibot.dto.NoteDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class NotatkiBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String username;
    @Value("${telegram.bot.token}")
    private String token;
    private WebClient notatkiService = WebClient.create("http://localhost:8098");
    private WebClient analyticsService = WebClient.create("http://localhost:8099");

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            switch (update.getMessage().getText()){
                case("/start"):
                    sendMessage.setText("Hi! Just write down your notes or use /help to see additional options");
                    break;
                case("/getNotes"):
                    List<NoteDto> notes = notatkiService.get()
                            .uri("/notes-user/" + update.getMessage().getChatId())
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();
                    sendMessage.setText(notes.toString());
                    break;
                case("/getAnalytics"):
                    sendMessage.setText(analyticsService.get().uri("/analytics/"+ update.getMessage().getChatId())
                            .retrieve()
                            .bodyToMono(AnalyticsDto.class)
                            .block().toString());
                    break;
                case("/help"):
                    sendMessage.setText("/start - greetings\n" +
                            "/getNotes - get all notes\n" +
                            "/getAnalytics - get user analytics");
                    break;
                default:
                    NoteDto note = new NoteDto();
                    note.setNote(update.getMessage().getText());
                    note.setUserId(update.getMessage().getChatId().toString());
                    notatkiService.post()
                            .uri("/notes")
                            .contentType(MediaType.APPLICATION_JSON).bodyValue(note)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();
                    sendMessage.setText("Your note has been successfully saved");
                    break;
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
