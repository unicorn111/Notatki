package com.marta.university.notatkiservice.persistence.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "note")
@Data
public class Note {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="note_id")
    private int noteId;
    @Column(name="user_id")
    private int userId;
    private String note;
    @Column(name="created_on")
    private LocalDateTime createdOn;
    @Column(name="modified_on")
    private LocalDateTime modifiedOn;
}
