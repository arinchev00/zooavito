package com.example.zooavito.service.Announcement;

import com.example.zooavito.model.Announcement;
import com.example.zooavito.model.Category;
import com.example.zooavito.model.Image;
import com.example.zooavito.repository.AnnouncementRepository;
import com.example.zooavito.repository.CategoryRepository;
import com.example.zooavito.repository.ImageRepository;
import com.example.zooavito.request.AnnouncementRequest;
import com.example.zooavito.response.AnnouncementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, @Nullable MultipartFile file) throws IOException {
        log.info("=== СОЗДАНИЕ ОБЪЯВЛЕНИЯ ===");
        log.info("Заголовок: {}", request.getTitle());

        // Создаем объявление
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setPrice(request.getPrice());
        announcement.setDescription(request.getDescription());
        announcement.setComment(request.getComment());
        announcement.setDateOfPublication(LocalDate.now());

        // Добавляем категории
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Категория не найдена: " + categoryId
                        ));
                categories.add(category);
            }
            announcement.setCategories(categories);
        }

        // Сохраняем объявление и ПРОВЕРЯЕМ ID
        Announcement savedAnnouncement = announcementRepository.saveAndFlush(announcement);
        log.info("Объявление сохранено. ID: {}, exists in DB: {}",
                savedAnnouncement.getId(),
                announcementRepository.existsById(savedAnnouncement.getId()));

        // Обрабатываем изображение
        if (file != null && !file.isEmpty()) {
            log.info("Обработка изображения: {}", file.getOriginalFilename());

            Image image = convertToImage(file);
            image.setAnnouncement(savedAnnouncement);

            // ПРОВЕРЯЕМ, что ID объявления не null
            if (savedAnnouncement.getId() == null) {
                log.error("ID объявления null! Невозможно сохранить изображение");
                throw new RuntimeException("ID объявления не сгенерирован");
            }

            // Сохраняем изображение
            Image savedImage = imageRepository.save(image);
            log.info("Изображение сохранено с ID: {}, для announcement_id: {}",
                    savedImage.getId(), savedImage.getAnnouncement().getId());

            // Добавляем изображение в коллекцию объявления
            if (savedAnnouncement.getImages() == null) {
                savedAnnouncement.setImages(new HashSet<>());
            }
            savedAnnouncement.getImages().add(savedImage);
        }

        // Возвращаем финальное состояние
        Announcement finalAnnouncement = announcementRepository.findById(savedAnnouncement.getId()).orElseThrow();
        log.info("Объявление с изображениями: {}", finalAnnouncement.getImages().size());

        return AnnouncementResponse.from(finalAnnouncement);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementResponse getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .map(AnnouncementResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Объявление не найдено с id: " + id
                ));
    }

    private Image convertToImage(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());

        log.info("Изображение сконвертировано: {}", image.getOriginalFileName());
        return image;
    }
}