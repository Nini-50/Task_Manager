package com.academictaskmanager.service;

import com.academictaskmanager.model.Topic;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.KeyTermRepository;
import com.academictaskmanager.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final KeyTermRepository keyTermRepository;

    public TopicService(TopicRepository topicRepository, KeyTermRepository keyTermRepository) {
        this.topicRepository = topicRepository;
        this.keyTermRepository = keyTermRepository;
    }

    public List<Topic> findByCourse(Long courseId, User owner) {
        return topicRepository.findByCourseIdAndOwnerOrderByNameAsc(courseId, owner);
    }

    public Topic findById(Long id, User owner) {
        return topicRepository.findByIdAndOwner(id, owner).orElseThrow(() ->
                new IllegalArgumentException("Topic not found: " + id));
    }

    public Topic save(Topic topic, User owner) {
        topic.setOwner(owner);
        return topicRepository.save(topic);
    }

    /** Deletes a topic and every key term that belongs to it. */
    public void delete(Long id, User owner) {
        Topic topic = findById(id, owner);
        keyTermRepository.deleteAll(keyTermRepository.findByTopicAndOwner(topic, owner));
        topicRepository.delete(topic);
    }
}
