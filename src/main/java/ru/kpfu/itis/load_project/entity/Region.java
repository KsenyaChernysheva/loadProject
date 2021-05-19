package ru.kpfu.itis.load_project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "region")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column
    private String name;

    @Column(name = "db_name")
    private String dbName;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "geo")
    private String geo;
}
