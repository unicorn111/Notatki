package com.marta.university.notatkiservice.persistence;

import com.marta.university.notatkiservice.persistence.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Integer> {
    default List<Note> findAllByUserId(int userId){
        return this.findAll().stream().filter(n -> n.getUserId() == userId ).toList();
    };

}
