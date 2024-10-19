package com.example.backend.services;

import com.example.backend.models.PembeliModel;
import com.example.backend.models.PenjualModel;
import com.example.backend.repositories.PenjualRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PenjualService {
    @Autowired
    private PenjualRepo repo ;

    public List<PenjualModel> get () {
        return repo.findAll();
    }

}