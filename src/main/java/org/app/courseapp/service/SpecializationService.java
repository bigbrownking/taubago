package org.app.courseapp.service;

import org.app.courseapp.model.Specialization;

import java.util.List;

public interface SpecializationService {
    List<Specialization> getAll();
    Specialization getById(Long id);
    Specialization create(String name);
    void delete(Long id);
}
