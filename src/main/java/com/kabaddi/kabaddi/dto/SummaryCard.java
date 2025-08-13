package com.kabaddi.kabaddi.dto;

import com.kabaddi.kabaddi.util.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SummaryCard {
    private String matchId;
    private String matchName;
    private Integer team1Score;
    private Integer team2Score;
    private String team1PhotoUrl;
    private String team2PhotoUrl;
    private String team1Name;
    private String team2Name;
    private Integer remainingTime;
    private String location;
    private MatchStatus matchStatus;
}
