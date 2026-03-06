package com.example.zooavito.controller;

import com.example.zooavito.config.ApiResponseAnnotations;
import com.example.zooavito.request.AnnouncementRequest;
import com.example.zooavito.response.AnnouncementResponse;
import com.example.zooavito.service.Announcement.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v1/api/announcement")
@RequiredArgsConstructor
@Tag(name = "Объявления", description = "API для работы с объявлениями")
@Slf4j
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создание нового объявления с фото")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Объявление успешно создано")
    })
    @ApiResponseAnnotations.CommonPostResponses
    @ApiResponseAnnotations.UnsupportedMediaResponse
    public AnnouncementResponse createAnnouncement(
            @Valid @RequestPart("announcement") AnnouncementRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication
    ) throws IOException {
        log.info("Создание объявления: title={}, hasImage={}",
                request.getTitle(), image != null);
        return announcementService.createAnnouncement(request, image);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить объявление по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(schema = @Schema(implementation = AnnouncementResponse.class)))
    })
    @ApiResponseAnnotations.CommonGetResponses
    public AnnouncementResponse getAnnouncement(@PathVariable Long id) {
        return announcementService.getAnnouncementById(id);
    }
}