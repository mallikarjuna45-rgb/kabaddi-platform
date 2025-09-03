package com.kabaddi.kabaddi.dto;

import com.kabaddi.kabaddi.util.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class
MatchDto {
    private String id;

    private String matchName;

    private String team1Name;

    private String team2Name;

    private Integer team1Score;
    private Integer team2Score;

    private String team1PhotoUrl;

    private String team2PhotoUrl;

    private MatchStatus status;

    private LocalDate createdAt;

    private String createdBy;

    private Integer totalDuration;

    private Integer remainingDuration;
    private String creatorName;
    private String Location;
}
