package com.academictaskmanager.service;

import com.academictaskmanager.model.KeyTerm;
import com.academictaskmanager.model.Topic;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.KeyTermRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyTermServiceTest {

    @Mock
    private KeyTermRepository keyTermRepository;

    private KeyTermService keyTermService;
    private User owner;
    private Topic topic;

    @BeforeEach
    void setUp() {
        keyTermService = new KeyTermService(keyTermRepository);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
        topic = new Topic();
        topic.setId(4L);
        topic.setName("Recursion");
    }

    @Test
    void findByTopicDelegatesToRepository() {
        KeyTerm term = new KeyTerm();
        term.setTerm("Base case");
        when(keyTermRepository.findByTopicIdAndOwnerOrderByIdAsc(4L, owner)).thenReturn(List.of(term));

        assertThat(keyTermService.findByTopic(4L, owner)).containsExactly(term);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(keyTermRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> keyTermService.findById(5L, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }

    @Test
    void saveSetsOwner() {
        KeyTerm term = new KeyTerm();
        term.setTerm("Base case");
        term.setDefinition("The condition that stops recursion.");
        term.setTopic(topic);
        when(keyTermRepository.save(any(KeyTerm.class))).thenAnswer(inv -> inv.getArgument(0));

        KeyTerm saved = keyTermService.save(term, owner);

        assertThat(saved.getOwner()).isEqualTo(owner);
    }

    @Test
    void saveAllClearsIdsAndSetsOwner() {
        KeyTerm term = new KeyTerm();
        term.setId(99L);
        term.setTerm("Base case");
        term.setDefinition("Stops recursion.");
        term.setTopic(topic);
        List<KeyTerm> terms = new ArrayList<>(List.of(term));
        when(keyTermRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<KeyTerm> saved = keyTermService.saveAll(terms, owner);

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getId()).isNull();
        assertThat(saved.get(0).getOwner()).isEqualTo(owner);
    }

    @Test
    void deleteDelegatesToRepository() {
        KeyTerm term = new KeyTerm();
        term.setId(3L);
        when(keyTermRepository.findByIdAndOwner(3L, owner)).thenReturn(Optional.of(term));

        keyTermService.delete(3L, owner);

        verify(keyTermRepository).delete(term);
    }
}
