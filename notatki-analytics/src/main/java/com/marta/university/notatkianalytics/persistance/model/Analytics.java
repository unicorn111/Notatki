package com.marta.university.notatkianalytics.persistance.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "analytics")
@Data
public class Analytics {
    @Id
    @Column(name="user_id")
    private String userId;
    @Column(name="avarage_text_size")
    private int avarageTextSize;
    @Column(name="created_quantity")
    private int createdQuantity;
    @Column(name="times_modified")
    private int timesModified;
}
