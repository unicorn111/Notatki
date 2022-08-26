package com.marta.university.notatkibot.dto;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class NoteDto {
    private int noteId;
    private String userId;
    private String note;
    private String createdOn;
    private String modifiedOn;
}
