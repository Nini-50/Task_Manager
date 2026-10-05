package com.academictaskmanager.controller;

import com.academictaskmanager.model.Note;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.NoteService;
import com.academictaskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST API backing the per-class "Notes" tabs on the Classes view. */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    public NoteController(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping
    public List<Note> byCourse(Authentication authentication, @RequestParam Long courseId) {
        return noteService.findByCourse(courseId, currentUser(authentication));
    }

    @PostMapping
    public ResponseEntity<Note> create(Authentication authentication, @Valid @RequestBody Note note) {
        note.setId(null);
        return ResponseEntity.ok(noteService.save(note, currentUser(authentication)));
    }

    @PutMapping("/{id}")
    public Note update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody Note note) {
        note.setId(id);
        return noteService.save(note, currentUser(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        noteService.delete(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }
}
