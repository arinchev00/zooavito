package com.example.zooavito.controller;

import com.example.zooavito.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
}
