package com.example.zooavito.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="image")
@Data
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "original_file_name")
    private String originalFileName;

    private Long size;

    @Column(name = "content_type")
    private String contentType;

    @Lob
    private byte[] bytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;
}
