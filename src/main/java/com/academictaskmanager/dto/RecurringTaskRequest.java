package com.academictaskmanager.dto;

import com.academictaskmanager.model.AcademicTask;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Request body for creating a recurring series of tasks: the template task plus the rule. */
public class RecurringTaskRequest {

    @NotNull
    @Valid
    private AcademicTask task;

    @NotNull
    @Valid
    private RecurrenceRequest recurrence;

    public AcademicTask getTask() { return task; }
    public void setTask(AcademicTask task) { this.task = task; }

    public RecurrenceRequest getRecurrence() { return recurrence; }
    public void setRecurrence(RecurrenceRequest recurrence) { this.recurrence = recurrence; }
}
