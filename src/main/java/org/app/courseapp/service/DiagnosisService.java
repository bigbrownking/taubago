package org.app.courseapp.service;

import org.app.courseapp.model.Diagnosis;

import java.util.List;

public interface DiagnosisService {
    List<Diagnosis> getAll();
    Diagnosis getById(Long id);
    Diagnosis create(String name);
    void delete(Long id);
}
