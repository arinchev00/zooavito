package com.example.zooavito.controller;

import com.example.zooavito.model.Announcement;
import com.example.zooavito.model.Image;
import com.example.zooavito.service.Announcement.AnnouncementService;
import com.example.zooavito.service.Security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;

@Controller
@RequestMapping("/announcement")
public class AnnouncementController {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementController.class);

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private SecurityService securityService;

    /**
     * Показать форму создания объявления
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        logger.info("=== ОТОБРАЖЕНИЕ ФОРМЫ СОЗДАНИЯ ОБЪЯВЛЕНИЯ ===");

        String loggedInEmail = securityService.findLoggedInEmail();
        logger.info("Текущий пользователь: {}", loggedInEmail);

        Announcement announcement = new Announcement();
        announcement.setDateOfPublication(LocalDate.now());

        model.addAttribute("announcement", announcement);
        return "createAnnouncement";
    }

    /**
     * Обработка создания объявления
     */
    @PostMapping("/create")
    public String createAnnouncement(
            @ModelAttribute Announcement announcement,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            logger.info("=== ОБРАБОТКА СОЗДАНИЯ ОБЪЯВЛЕНИЯ ===");
            logger.info("Заголовок: {}", announcement.getTitle());
            logger.info("Цена: {}", announcement.getPrice());
            logger.info("Описание: {}", announcement.getDescription());

            if (announcement.getDateOfPublication() == null) {
                announcement.setDateOfPublication(LocalDate.now());
            }

            announcementService.create(announcement, file);

            logger.info("✅ Объявление успешно создано!");
            redirectAttributes.addFlashAttribute("successMessage",
                    "Объявление успешно создано!");

            return "redirect:/announcement/" + announcement.getId();

        } catch (IOException e) {
            logger.error("❌ Ошибка при загрузке файла: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при загрузке файла: " + e.getMessage());
            model.addAttribute("announcement", announcement);
            return "createAnnouncement";
        } catch (Exception e) {
            logger.error("❌ Ошибка при создании объявления: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при создании объявления: " + e.getMessage());
            model.addAttribute("announcement", announcement);
            return "createAnnouncement";
        }
    }

    /**
     * Просмотр конкретного объявления
     */
    @GetMapping("/{id}")
    public String viewAnnouncement(@PathVariable Integer id, Model model) {
        try {
            logger.info("=== ПРОСМОТР ОБЪЯВЛЕНИЯ ===");
            logger.info("ID: {}", id);

            Announcement announcement = announcementService.findById(id);

            if (announcement != null) {
                // Конвертируем изображения в Base64 для отображения
                if (announcement.getImages() != null && !announcement.getImages().isEmpty()) {
                    for (Image image : announcement.getImages()) {
                        if (image.getBytes() != null && image.getBytes().length > 0) {
                            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
                            image.setBase64Image(base64);
                            logger.info("Изображение ID: {} сконвертировано в Base64", image.getId());
                        }
                    }
                }

                model.addAttribute("announcement", announcement);
                return "announcementDetails";
            } else {
                model.addAttribute("error", "Объявление не найдено");
                return "error";
            }

        } catch (Exception e) {
            logger.error("❌ Ошибка при просмотре объявления: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при загрузке объявления");
            return "error";
        }
    }
}