package com.example.zooavito.service.Announcement;

import com.example.zooavito.request.AnnouncementRequest;
import com.example.zooavito.response.AnnouncementResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AnnouncementService {
    AnnouncementResponse createAnnouncement(AnnouncementRequest request, MultipartFile file) throws IOException;
    AnnouncementResponse getAnnouncementById(Long id);
}
