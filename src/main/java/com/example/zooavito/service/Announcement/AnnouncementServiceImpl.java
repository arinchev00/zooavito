package com.example.zooavito.service.Announcement;

import com.example.zooavito.model.*;
import com.example.zooavito.repository.*;
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
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, @Nullable List<MultipartFile> files, String userEmail) throws IOException {
        log.info("=== СОЗДАНИЕ ОБЪЯВЛЕНИЯ ===");
        log.info("Заголовок: {}, файлов: {}", request.getTitle(), files != null ? files.size() : 0);

        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден");
        }

        // Получаем подкатегорию по ID
        Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Подкатегория не найдена: " + request.getSubcategoryId()
                ));

        // Получаем категорию из подкатегории
        Category category = subcategory.getCategory();

        // Создаем объявление
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setPrice(request.getPrice());
        announcement.setDescription(request.getDescription());
        announcement.setComment(request.getComment());
        announcement.setDateOfPublication(LocalDate.now());
        announcement.setUser(user);

        // Устанавливаем категорию (достаем из подкатегории)
        Set<Category> categories = new HashSet<>();
        categories.add(category);
        announcement.setCategories(categories);

        // Устанавливаем подкатегорию
        Set<Subcategory> subcategories = new HashSet<>();
        subcategories.add(subcategory);
        announcement.setSubcategories(subcategories);

        // Сохраняем объявление
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        log.info("Объявление сохранено с ID: {}", savedAnnouncement.getId());

        // Обрабатываем изображения
        if (files != null && !files.isEmpty()) {
            log.info("Обработка {} изображений", files.size());

            Set<Image> savedImages = new HashSet<>();

            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    log.info("Конвертация файла: {}", file.getOriginalFilename());

                    Image image = convertToImage(file);
                    image.setAnnouncement(savedAnnouncement);

                    Image savedImage = imageRepository.save(image);
                    savedImages.add(savedImage);

                    log.info("✅ Изображение сохранено: {}, ID: {}, размер: {} байт",
                            file.getOriginalFilename(), savedImage.getId(), file.getSize());
                }
            }

            if (!savedImages.isEmpty()) {
                savedAnnouncement.setImages(savedImages);
                log.info("Всего изображений добавлено: {}", savedImages.size());
            }
        } else {
            log.info("Объявление создано без изображений");
        }

        Announcement fullAnnouncement = announcementRepository.findById(savedAnnouncement.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено"));

        return AnnouncementResponse.from(fullAnnouncement);
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

    @Override
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request, @Nullable List<MultipartFile> newFiles, String userEmail) throws IOException {
        log.info("=== ОБНОВЛЕНИЕ ОБЪЯВЛЕНИЯ ID: {} ===", id);

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Объявление не найдено с id: " + id
                ));

        // Проверка прав
        User currentUser = userRepository.findByEmail(userEmail);
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден");
        }

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getTitle()));

        if (!announcement.getUser().getEmail().equals(userEmail) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет прав на редактирование");
        }

        // Обновляем поля
        announcement.setTitle(request.getTitle());
        announcement.setPrice(request.getPrice());
        announcement.setDescription(request.getDescription());
        announcement.setComment(request.getComment());

        // Получаем новую подкатегорию (если изменилась)
        Subcategory newSubcategory = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Подкатегория не найдена: " + request.getSubcategoryId()
                ));

        // Обновляем категорию и подкатегорию
        Set<Category> categories = new HashSet<>();
        categories.add(newSubcategory.getCategory());
        announcement.setCategories(categories);

        Set<Subcategory> subcategories = new HashSet<>();
        subcategories.add(newSubcategory);
        announcement.setSubcategories(subcategories);

        // Обработка новых изображений
        if (newFiles != null && !newFiles.isEmpty()) {
            log.info("Добавление {} новых изображений", newFiles.size());

            for (MultipartFile file : newFiles) {
                if (file != null && !file.isEmpty()) {
                    Image image = convertToImage(file);
                    image.setAnnouncement(announcement);
                    imageRepository.save(image);
                    announcement.getImages().add(image);
                }
            }
        }

        Announcement updatedAnnouncement = announcementRepository.save(announcement);
        log.info("Объявление обновлено");

        return AnnouncementResponse.from(updatedAnnouncement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id, String userEmail) {
        log.info("=== УДАЛЕНИЕ ОБЪЯВЛЕНИЯ ID: {} ===", id);

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Объявление не найдено с id: " + id
                ));

        // Проверяем права доступа
        User currentUser = userRepository.findByEmail(userEmail);
        if (currentUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Пользователь не найден"
            );
        }

        // Проверка: владелец или админ
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getTitle()));

        if (!announcement.getUser().getEmail().equals(userEmail) && !isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "У вас нет прав на удаление этого объявления"
            );
        }

        announcementRepository.delete(announcement);
        log.info("Объявление удалено");
    }

    private Image convertToImage(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());

        log.info("Изображение сконвертировано: {}, размер: {} байт, тип: {}",
                image.getOriginalFileName(), image.getSize(), image.getContentType());
        return image;
    }
}