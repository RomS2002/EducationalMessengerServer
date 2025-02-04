package ru.roms2002.messenger.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.roms2002.messenger.server.entity.FileEntity;
import ru.roms2002.messenger.server.repository.FileRepository;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    public FileEntity save(FileEntity f) {
        return fileRepository.save(f);
    }

    public FileEntity findByFkMessageId(int id) {
        return fileRepository.findByMessageId(id);
    }

    public String findFileUrlByMessageId(int id) {
        return  fileRepository.findFileUrlByMessageId(id);
    }
}
