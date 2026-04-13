package org.example.springredis.domain.ch4.location.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private double longitude;
    private double latitude;
}
