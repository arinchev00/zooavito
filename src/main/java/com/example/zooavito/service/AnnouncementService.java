package com.example.zooavito.service;

import com.example.zooavito.model.Announcement;
import com.example.zooavito.repository.AnnouncementRepository;
import com.example.zooavito.model.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    public List<Announcement> findAll() {
        List<Announcement> announcement = announcementRepository.findAll();
        Collections.sort(announcement, Comparator.comparing(Announcement::getDateOfPublication));
        return announcement;
    }

    public Announcement create(Announcement announcement) throws IOException{
        return announcementRepository.save(announcement);
    }

    private Image toImageEntity (MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());
        return image;
    }

    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }
}
