package com.marta.university.notatkiservice.service;

import com.marta.university.notatkiservice.configs.MQConfig;
import com.marta.university.notatkiservice.persistence.NoteRepository;
import com.marta.university.notatkiservice.persistence.model.Note;
import com.marta.university.notatkiservice.web.dto.NoteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class NoteService {
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private RabbitTemplate template;

    public NoteDto createDto(NoteDto dto){
        Note note = new Note();
        note.setCreatedOn(LocalDateTime.now());
        note.setNote(dto.getNote());
        note.setModifiedOn(LocalDateTime.now());
        note.setUserId(dto.getUserId());
        Note newNote = noteRepository.save(note);
        log.info("Saved note id: {}, text: {}", newNote.getNoteId(), newNote.getNote());
        NoteDto noteDto = mapNoteDto(newNote);
        template.convertAndSend(MQConfig.EXCHANGE,
                MQConfig.ROUTING_KEY, noteDto);
        return noteDto;
    }
    public NoteDto updateDto(NoteDto dto){
        Note note = noteRepository.findById(dto.getNoteId()).get();
        note.setNote(dto.getNote());
        note.setModifiedOn(LocalDateTime.now());
        Note updatedNote = noteRepository.save(note);
        log.info("Updated note id: {}, text: {}", updatedNote.getNoteId(), updatedNote.getNote());
        template.convertAndSend(MQConfig.EXCHANGE,
                MQConfig.SECOND_ROUTING_KEY, mapNoteDto(updatedNote));
        return mapNoteDto(updatedNote);
    }

    public void deleteDtoByID(int id){
        noteRepository.deleteById(id);
    }

    public NoteDto findByIdDto(int id){
        return mapNoteDto(noteRepository.findById(id).get());
    }

    private NoteDto mapNoteDto(Note newNote){
        NoteDto newNoteDto = new NoteDto();
        newNoteDto.setCreatedOn(newNote.getCreatedOn().toString());
        newNoteDto.setNote(newNote.getNote());
        newNoteDto.setModifiedOn(newNote.getModifiedOn().toString());
        newNoteDto.setUserId(newNote.getUserId());
        newNoteDto.setNoteId(newNote.getNoteId());
        return newNoteDto;
    }
    private Note mapDtoNote(NoteDto dto){
        Note note = new Note();
        note.setCreatedOn(LocalDateTime.parse(dto.getCreatedOn()));
        note.setNote(dto.getNote());
        note.setModifiedOn(LocalDateTime.parse(dto.getModifiedOn()));
        note.setUserId(dto.getUserId());
        note.setNoteId(dto.getNoteId());
        log.info("Note id {}", note.getNoteId());
        return note;
    }
    public List<NoteDto> getAllByUserId(int userId){
        return noteRepository.findAllByUserId(userId).stream().map(this::mapNoteDto).toList();
    }

    public List<Integer> getAllNoteIdByUserId(int userId){
        return getAllByUserId(userId).stream().map(i -> i.getNoteId()).toList();
    }

    public List<NoteDto> getAllNotes(){
        Iterable<Note> notes = noteRepository.findAll();
        Spliterator<Note> spliterator = notes.spliterator();
        return StreamSupport.stream(spliterator, false).map(n -> mapNoteDto(n)).toList();
    }
}
