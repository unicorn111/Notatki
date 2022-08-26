package com.marta.university.notatkianalytics.web.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Data
public class NoteDto {
    private int noteId;
    private String userId;
    private String note;
    private String createdOn;
    private String modifiedOn;
}
