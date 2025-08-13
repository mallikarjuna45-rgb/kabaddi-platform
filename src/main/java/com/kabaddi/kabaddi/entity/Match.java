package com.kabaddi.kabaddi.entity;


import com.kabaddi.kabaddi.util.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    @Id
    private String id;

    private String matchName;
    private String team1Name;
    private String team2Name;
    private String team1PhotoUrl;
    private String team2PhotoUrl;

    private MatchStatus status; // SCHEDULED, LIVE, PAUSED,COMPLETED

    private LocalDateTime createdAt;
    private String createdBy;

    private Integer totalDuration;
    private Integer remainingDuration;

    private LocalDateTime startTime;
    private LocalDateTime pauseTime;

    private String location;
    private String matchStats;

}

