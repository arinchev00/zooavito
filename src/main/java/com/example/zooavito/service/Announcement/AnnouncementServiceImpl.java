package com.example.zooavito.service.Announcement;

import com.example.zooavito.model.Announcement;
import com.example.zooavito.model.Image;
import com.example.zooavito.repository.AnnouncementRepository;
import com.example.zooavito.repository.CategoryRepository;
import com.example.zooavito.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementServiceImpl.class);

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void create(Announcement announcement, MultipartFile file) throws IOException {
        try {
            logger.info("=== СОЗДАНИЕ ОБЪЯВЛЕНИЯ ===");
            logger.info("Заголовок: {}", announcement.getTitle());

            // Сначала сохраняем объявление, чтобы получить ID
            Announcement savedAnnouncement = announcementRepository.save(announcement);

            if (file != null && !file.isEmpty()) {
                logger.info("Обработка изображения: {}", file.getOriginalFilename());

                Image image = toImageEntity(file);
                image.setAnnouncement(savedAnnouncement);

                if (savedAnnouncement.getImages() == null) {
                    savedAnnouncement.setImages(new HashSet<>());
                }

                savedAnnouncement.getImages().add(image);

                // Сохраняем объявление снова, чтобы каскадно сохранилось изображение
                announcementRepository.save(savedAnnouncement);
            }

            logger.info("✅ Объявление успешно сохранено с ID: {}", savedAnnouncement.getId());

        } catch (Exception e) {
            logger.error("❌ ОШИБКА: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Announcement findById(Integer id) {
        try {
            logger.info("=== ПОИСК ОБЪЯВЛЕНИЯ ===");
            logger.info("ID: {}", id);

            // Конвертируем Integer в Long (так как в модели Announcement id типа Long)
            Long longId = Long.valueOf(id);
            Announcement announcement = announcementRepository.findById(longId).orElse(null);

            if (announcement != null) {
                logger.info("✅ Объявление найдено:");
                logger.info("   Заголовок: {}", announcement.getTitle());
                logger.info("   Цена: {}", announcement.getPrice());
                logger.info("   Описание: {}", announcement.getDescription());
                logger.info("   Комментарий: {}", announcement.getComment());
                logger.info("   Дата публикации: {}", announcement.getDateOfPublication());

                // Информация о категориях
                int categoriesCount = announcement.getCategories() != null ?
                        announcement.getCategories().size() : 0;
                logger.info("   Категорий: {}", categoriesCount);

                // Информация об изображениях
                int imagesCount = announcement.getImages() != null ?
                        announcement.getImages().size() : 0;
                logger.info("   Изображений: {}", imagesCount);

                if (imagesCount > 0) {
                    logger.info("   Список изображений:");
                    announcement.getImages().forEach(img ->
                            logger.info("      - ID: {}, файл: {}",
                                    img.getId(), img.getOriginalFileName())
                    );
                }
            } else {
                logger.warn("⚠️ Объявление с ID {} не найдено", id);
            }

            return announcement;

        } catch (Exception e) {
            logger.error("❌ ОШИБКА при поиске объявления: {}", e.getMessage());
            logger.error("Тип ошибки: {}", e.getClass().getSimpleName());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Конвертирует MultipartFile в сущность Image
     */
    private Image toImageEntity(MultipartFile file) throws IOException {
        try {
            logger.info("Конвертация файла в Image: {}", file.getOriginalFilename());

            Image image = new Image();
            image.setName(file.getName());
            image.setOriginalFileName(file.getOriginalFilename());
            image.setContentType(file.getContentType());
            image.setSize(file.getSize());
            image.setBytes(file.getBytes());

            logger.info("✅ Изображение сконвертировано:");
            logger.info("   Имя поля: {}", image.getName());
            logger.info("   Оригинальное имя: {}", image.getOriginalFileName());
            logger.info("   Тип: {}", image.getContentType());
            logger.info("   Размер: {} байт", image.getSize());

            return image;

        } catch (IOException e) {
            logger.error("❌ ОШИБКА при конвертации изображения: {}", e.getMessage());
            throw e;
        }
    }
}