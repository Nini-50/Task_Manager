package com.academictaskmanager.service;

import com.academictaskmanager.model.KeyTerm;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.KeyTermRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeyTermService {

    private final KeyTermRepository keyTermRepository;

    public KeyTermService(KeyTermRepository keyTermRepository) {
        this.keyTermRepository = keyTermRepository;
    }

    public List<KeyTerm> findByTopic(Long topicId, User owner) {
        return keyTermRepository.findByTopicIdAndOwnerOrderByIdAsc(topicId, owner);
    }

    public KeyTerm findById(Long id, User owner) {
        return keyTermRepository.findByIdAndOwner(id, owner).orElseThrow(() ->
                new IllegalArgumentException("Key term not found: " + id));
    }

    public KeyTerm save(KeyTerm keyTerm, User owner) {
        keyTerm.setOwner(owner);
        return keyTermRepository.save(keyTerm);
    }

    /** Bulk-saves a batch of key terms, e.g. the ones a student confirmed from syllabus suggestions. */
    public List<KeyTerm> saveAll(List<KeyTerm> keyTerms, User owner) {
        keyTerms.forEach(keyTerm -> {
            keyTerm.setId(null);
            keyTerm.setOwner(owner);
        });
        return keyTermRepository.saveAll(keyTerms);
    }

    public void delete(Long id, User owner) {
        KeyTerm keyTerm = findById(id, owner);
        keyTermRepository.delete(keyTerm);
    }
}
