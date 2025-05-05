package ru.roms2002.messenger.server.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ru.roms2002.messenger.server.entity.FileEntity;
import ru.roms2002.messenger.server.entity.MessageEntity;
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
		return fileRepository.findFileUrlByMessageId(id);
	}

	public void deleteFileFromMessage(MessageEntity messageEntity) {
		String url = messageEntity.getFile().getUrl();
		Path path = Paths.get(url);
		try {
			Files.delete(path);
		} catch (IOException e) {

		}
	}

	public boolean saveFileOnDisk(String url, String filename, MultipartFile file) {
		Path path = Paths.get(url);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				return false;
			}
		}
		Path filepath = path.resolve(filename);
		try {
			Files.write(filepath, file.getBytes());
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
