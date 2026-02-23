package com.example.zooavito.service.Announcement;

import com.example.zooavito.model.Announcement;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AnnouncementService {
    void create(Announcement announcement, MultipartFile file) throws IOException;;
    Announcement findById(Integer id);
}
