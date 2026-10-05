package com.academictaskmanager.dto;

/** A candidate term/definition pair suggested from an uploaded syllabus, pending user review. */
public class KeyTermSuggestion {

    private String term;
    private String definition;

    public KeyTermSuggestion() {
    }

    public KeyTermSuggestion(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
}
