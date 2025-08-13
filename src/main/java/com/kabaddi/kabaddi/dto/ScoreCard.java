package com.kabaddi.kabaddi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreCard {

    private String matchId;
    private String playerId;
    private String playerName;
    private Integer raidPoints;
    private Integer defencePoints;


}
