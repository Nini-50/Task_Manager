package com.academictaskmanager.controller;

import com.academictaskmanager.model.Topic;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.TopicService;
import com.academictaskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST API backing the per-class "Practice" tab's topic list. */
@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;
    private final UserService userService;

    public TopicController(TopicService topicService, UserService userService) {
        this.topicService = topicService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping
    public List<Topic> byCourse(Authentication authentication, @RequestParam Long courseId) {
        return topicService.findByCourse(courseId, currentUser(authentication));
    }

    @PostMapping
    public ResponseEntity<Topic> create(Authentication authentication, @Valid @RequestBody Topic topic) {
        topic.setId(null);
        return ResponseEntity.ok(topicService.save(topic, currentUser(authentication)));
    }

    @PutMapping("/{id}")
    public Topic update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody Topic topic) {
        topic.setId(id);
        return topicService.save(topic, currentUser(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        topicService.delete(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }
}
