package com.example.zooavito.response;

import com.example.zooavito.model.Announcement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private int price;
    private String description;
    private String comment;
    private LocalDate dateOfPublication;
    private Set<CategoryResponse> categories;
    private Set<ImageResponse> images;

    public static AnnouncementResponse from(Announcement announcement) {
        Set<CategoryResponse> categoryResponses = null;
        if (announcement.getCategories() != null) {
            categoryResponses = announcement.getCategories().stream()
                    .map(CategoryResponse::from)
                    .collect(Collectors.toSet());
        }

        Set<ImageResponse> imageResponses = null;
        if (announcement.getImages() != null) {
            imageResponses = announcement.getImages().stream()
                    .map(ImageResponse::from)
                    .collect(Collectors.toSet());
        }

        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .price(announcement.getPrice())
                .description(announcement.getDescription())
                .comment(announcement.getComment())
                .dateOfPublication(announcement.getDateOfPublication())
                .categories(categoryResponses)
                .images(imageResponses)
                .build();
    }
}
