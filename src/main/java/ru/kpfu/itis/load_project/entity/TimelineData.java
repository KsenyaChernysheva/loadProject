package ru.kpfu.itis.load_project.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimelineData {
    private String time;
    private int[] value;

    public LocalDateTime getTime() {
        return LocalDateTime.ofEpochSecond(Long.parseLong(time), 0, ZoneOffset.UTC);
    }

    public int getValue() {
        return value[0];
    }
}
