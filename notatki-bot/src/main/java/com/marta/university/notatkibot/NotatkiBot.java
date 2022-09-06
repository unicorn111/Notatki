package com.marta.university.notatkibot;

import com.marta.university.notatkibot.dto.AnalyticsDto;
import com.marta.university.notatkibot.dto.NoteDto;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
public class NotatkiBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String username;
    @Value("${telegram.bot.token}")
    private String token;
    private String authorizedUser;
    private Map<String, NoteToUpdate> updateNote = new HashMap<>();
    private final WebClient notatkiService = WebClient.create("http://localhost:8098");
    private final WebClient analyticsService = WebClient.create("http://localhost:8099");

    @Data
    private class NoteToUpdate{
        private String noteId;
    }

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
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        }
    }

    private void handleMessage(Message message) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            authorizedUser = message.getChatId().toString();
            List<Integer> ids;
            switch (message.getText()){
                case("/start"):
                    sendMessage.setText("Hi! Just write down your notes or use /help to see additional options");
                    break;
                case("/getNotes"):
                    List<NoteDto> notes = notatkiService.get()
                            .uri("/notes-user/" + authorizedUser)
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();
                    //notes.stream().map(n -> n.getNoteId() + " " + n.getNote()).collect(Collectors.joining(" "));
                    sendMessage.setText(notes.toString());
                    break;
                case("/getAnalytics"):
                    AnalyticsDto analytics = analyticsService.get()
                            .uri("/analytics/"+ authorizedUser)
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
                            "/getAnalytics - get user analytics\n" +
                            "/delete - delete a note by noteId\n" +
                            "/getNote - get one note\n" +
                            "/update - update a note");
                    break;
                case("/delete"):
                    ids = notatkiService.get()
                            .uri("/notes-ids/" + authorizedUser)
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();
                    if(ids.size() == 0){
                        sendMessage.setText("Sorry, no notes to delete");
                        break;
                    }
                    sendMessage.setText("Please, choose a Note Id:");
                    sendMessage.setReplyMarkup(createKeyboard(ids, "DELETE"));
                    break;
                case("/update"):
                    ids = notatkiService.get()
                            .uri("/notes-ids/" + authorizedUser)
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();
                    if(ids.size() == 0){
                        sendMessage.setText("Sorry, no notes to update");
                        break;
                    }
                    sendMessage.setText("Please, choose a Note Id:");
                    sendMessage.setReplyMarkup(createKeyboard(ids, "UPDATE"));
                    break;
                case("/getNote"):
                    ids = notatkiService.get()
                            .uri("/notes-ids/" + authorizedUser)
                            .retrieve()
                            .bodyToMono(List.class)
                            .block();
                    if(ids.size() == 0){
                        sendMessage.setText("Sorry, no notes");
                        break;
                    }
                    sendMessage.setText("Please, choose a Note Id:");
                    sendMessage.setReplyMarkup(createKeyboard(ids, "GET"));
                    break;
                default:
                    if(updateNote.containsKey(authorizedUser)){
                        NoteDto note = new NoteDto();
                        note.setNote(message.getText());
                        note.setNoteId(Integer.valueOf(updateNote.get(authorizedUser).getNoteId()));
                        notatkiService.put()
                                .uri("/notes")
                                .contentType(MediaType.APPLICATION_JSON).bodyValue(note)
                                .retrieve()
                                .bodyToMono(Void.class)
                                .block();
                        updateNote.remove(authorizedUser);
                        sendMessage.setText("Your note has been successfully updated");
                        break;
                    }
                    NoteDto note = new NoteDto();
                    note.setNote(message.getText());
                    note.setUserId(authorizedUser);
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

    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        String[] param = callbackQuery.getData().split(" ");
        String action = param[0];
        String noteId = param[1];
        switch (action) {
            case "DELETE":
                notatkiService.delete()
                        .uri("/notes/" + noteId)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
                sendMessage.setText("Your note has been successfully deleted");
                break;
            case "UPDATE":
                NoteToUpdate noteToUpdate = new NoteToUpdate();
                noteToUpdate.setNoteId(noteId);
                updateNote.put(authorizedUser, noteToUpdate);
                sendMessage.setText("Your old note: " + notatkiService.get()
                        .uri("/notes/" + updateNote.get(authorizedUser).getNoteId())
                        .retrieve()
                        .bodyToMono(NoteDto.class)
                        .block().getNote() + ". Please, enter new text: ");
                break;
            case "GET":
                sendMessage.setText(notatkiService.get()
                        .uri("/notes/" + noteId)
                        .retrieve()
                        .bodyToMono(NoteDto.class)
                        .block().getNote());
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private InlineKeyboardMarkup createKeyboard(List<Integer> ids, String action){
        InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList= new ArrayList<>(Arrays.asList(new ArrayList<InlineKeyboardButton>()));

        if (ids.size()>10){
            for(int i=1; i<=(ids.size()/10); i++){
                rowList.add(new ArrayList<InlineKeyboardButton>());
            }
        }
        for(int i=0; i<ids.size(); i++){
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(ids.get(i).toString());
            inlineKeyboardButton.setCallbackData(action + " " + ids.get(i).toString());
            rowList.get(i/10).add(inlineKeyboardButton);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}

