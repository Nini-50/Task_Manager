package com.academictaskmanager.repository;

import com.academictaskmanager.model.Event;
import com.academictaskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOwnerAndStartDateTimeBetweenOrderByStartDateTimeAsc(
            User owner, LocalDateTime start, LocalDateTime end);

    List<Event> findByOwnerOrderByStartDateTimeAsc(User owner);

    Optional<Event> findByIdAndOwner(Long id, User owner);

    List<Event> findBySeriesIdAndOwner(String seriesId, User owner);
}
