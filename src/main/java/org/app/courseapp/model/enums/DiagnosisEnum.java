package org.app.courseapp.model.enums;

import lombok.Getter;

@Getter
public enum DiagnosisEnum {
    NO_DIAGNOSIS("No diagnosis yet"),
    SPEECH_DELAY("Speech delay (ZRR)"),
    DEVELOPMENTAL_DELAY("Developmental delay (ZPR)"),
    AUTISM_SPECTRUM("Autism Spectrum (ASD)"),
    OTHER("Other");
    private final String name;

    DiagnosisEnum(String name) {
        this.name = name;
    }
}
