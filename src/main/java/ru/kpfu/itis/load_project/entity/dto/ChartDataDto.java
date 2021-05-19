package ru.kpfu.itis.load_project.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataDto {
    private List<String> pointNames;
    private List<Long> pointValues;
}
