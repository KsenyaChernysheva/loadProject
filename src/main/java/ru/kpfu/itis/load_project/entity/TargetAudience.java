package ru.kpfu.itis.load_project.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "target_audience")
@Data
@NoArgsConstructor
public class TargetAudience {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column
    private String name;

    @Column
    private String query;

    @Column
    private Integer sheet;

    @Column(name="sub_column")
    private Integer subColumn;

    @Column
    private Integer coefficient;
}
