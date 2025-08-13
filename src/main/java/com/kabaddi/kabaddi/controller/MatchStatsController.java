package com.kabaddi.kabaddi.controller;
import com.kabaddi.kabaddi.dto.ScoreCard;
import com.kabaddi.kabaddi.dto.SummaryCard;
import com.kabaddi.kabaddi.dto.UpdateScoreDto;
import com.kabaddi.kabaddi.entity.MatchStats;
import com.kabaddi.kabaddi.service.MatchStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matchstats")
@RequiredArgsConstructor
public class MatchStatsController {

    private final MatchStatsService matchStatsService;
    @GetMapping("/match/summary/{matchId}")
    public ResponseEntity<SummaryCard> SummaryCard(@PathVariable String matchId) {
        return ResponseEntity.ok(matchStatsService.getSummaryCard(matchId));
    }

    @GetMapping("/match/scorecard/{matchId}")
    public ResponseEntity<List<ScoreCard>> MatchScorecard(@PathVariable String matchId) {
        return ResponseEntity.ok(matchStatsService.getMatchScorecard(matchId));
    }

    @PutMapping("/update/{matchId}")
    public ResponseEntity<MatchStats> updateMatchStats(@PathVariable String matchId, @RequestBody UpdateScoreDto updateScoreDto) {
        return ResponseEntity.ok(matchStatsService.updateMatchstats(matchId,updateScoreDto.getPlayerId(),updateScoreDto.getPointType(),updateScoreDto.getPoints()));
    }
    @PutMapping("/update/undo/{matchId}")
    public ResponseEntity<MatchStats> undoMatchStats(@PathVariable String matchId, @RequestBody UpdateScoreDto updateScoreDto) {
        return ResponseEntity.ok(matchStatsService.updateMatchstats(matchId,updateScoreDto.getPlayerId(),updateScoreDto.getPointType(),(-1)*updateScoreDto.getPoints()));
    }

}
