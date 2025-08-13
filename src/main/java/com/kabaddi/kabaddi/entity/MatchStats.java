package com.kabaddi.kabaddi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "match_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchStats {
    @Id
    private String id;
    private String matchId;
    private String playerId;
    private String teamName;
    private Integer raidPoints;
    private Integer defencePoints;
}
