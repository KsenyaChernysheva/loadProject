package ru.kpfu.itis.load_project.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "statistic")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Statistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "number_of_queries")
    private Long numberOfQueries;
}
