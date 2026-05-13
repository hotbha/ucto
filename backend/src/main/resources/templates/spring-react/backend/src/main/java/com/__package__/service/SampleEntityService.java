package com.__package__.service;

import com.__package__.entity.SampleEntity;
import com.__package__.repository.SampleEntityRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SampleEntityService {
    private final SampleEntityRepository repo;
    public SampleEntityService(SampleEntityRepository repo) { this.repo = repo; }

    public List<SampleEntity> findAll() { return repo.findAll(); }
    public SampleEntity create(String name, String description) {
        SampleEntity e = new SampleEntity();
        e.setName(name); e.setDescription(description);
        return repo.save(e);
    }
}