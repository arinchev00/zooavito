package com.example.zooavito.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "announcement")
@Data
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int prace;

    private String description;

    private String category;

    private String comment;

    private LocalDate DateOfPublication;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "announcement")
    private Set<Image> images;

}
