package com.marta.university.notatkiservice.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteDto {
    private int noteId;
    private int userId;
    private String note;
    private String createdOn;
    private String modifiedOn;
}
