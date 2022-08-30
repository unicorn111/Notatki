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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class NotatkiBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String username;
    @Value("${telegram.bot.token}")
    private String token;
    private static boolean toDelete = false;
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
                    AnalyticsDto analytics = analyticsService.get().uri("/analytics/"+ update.getMessage().getChatId())
                            .retrieve()
                            .bodyToMono(AnalyticsDto.class)
                            .block();
                    if(analytics == null){
                        sendMessage.setText("Sorry, no info about you at the moment");
                        break;
                    }
                    sendMessage.setText(analytics.toString());
                    break;
                case("/help"):
                    sendMessage.setText("/start - greetings\n" +
                            "/getNotes - get all notes\n" +
                            "/getAnalytics - get user analytics\n"+
                            "/delete - delete a note by noteId");
                    break;
                case("/delete"):
                    List<Integer> ids = notatkiService.get()
                            .uri("/notes-ids/" + update.getMessage().getChatId())
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();
                    if(ids.size() == 0){
                        sendMessage.setText("Sorry, no notes to delete");
                        break;
                    }
                    sendMessage.setText("Please, choose a Note Id:");
                    sendMessage.setReplyMarkup(createKeyboard(ids));
                    toDelete = true;
                    break;
                default:
                    if(toDelete){
                        notatkiService.delete()
                                .uri("/notes/" + update.getMessage().getText())
                                .retrieve()
                                .bodyToMono(Void.class)
                                .block();
                        toDelete = false;
                        sendMessage.setText("Your note has been successfully deleted");
                        break;
                    }
                    else {
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
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private ReplyKeyboardMarkup createKeyboard(List<Integer> ids){
        List<KeyboardRow> rows = new ArrayList<>(Arrays.asList(new KeyboardRow()));
        if (ids.size()>10){
            for(int i=1; i<=(ids.size()/10); i++){
                rows.add(new KeyboardRow());
            }
        }
        for(int i=0; i<ids.size(); i++){
            rows.get(i/10).add(ids.get(i).toString());
        }
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }
}
