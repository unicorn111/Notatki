package com.marta.university.notatkiservice.web;

import com.marta.university.notatkiservice.service.NoteService;
import com.marta.university.notatkiservice.web.dto.NoteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController

public class NoteController {
    @Autowired
    private NoteService noteService;

    @PostMapping("/notes")
    public ResponseEntity<NoteDto> createNote(@RequestBody NoteDto dto){
        log.info("Recieved note with userId:{} and text:{}",dto.getUserId(), dto.getNote());
        return ResponseEntity.ok(noteService.createDto(dto));
    }
    @PutMapping("/notes")
    public ResponseEntity<NoteDto> updateNote(@RequestBody NoteDto dto){
        return ResponseEntity.ok(noteService.updateDto(dto));
    }
    @DeleteMapping("/notes/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable("id") int id){
        noteService.deleteDtoByID(id);
        return ResponseEntity.accepted().body("Deleted");
    }
    @GetMapping("/notes/{id}")
    public ResponseEntity<?> getNote(@PathVariable("id") int id){
        return ResponseEntity.ok(noteService.findByIdDto(id));
    }
    @GetMapping("/notes-user/{userId}")
    public ResponseEntity<List<NoteDto>> getNotesByUser(@PathVariable("userId") int userId){
        return ResponseEntity.ok(noteService.getAllByUserId(userId));
    }
    @GetMapping("/notes")
    public ResponseEntity<?> getAllNotes(){
        return ResponseEntity.ok(noteService.getAllNotes());
    }
    @GetMapping("/test")
    public ResponseEntity<?> getTest(){
        return ResponseEntity.ok("Service is working");
    }
}
