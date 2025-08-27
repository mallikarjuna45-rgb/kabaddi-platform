package com.kabaddi.kabaddi.controller;
import com.kabaddi.kabaddi.dto.MatchDto;
import com.kabaddi.kabaddi.dto.ScoreCard;
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

    @GetMapping("/match/scorecard/{matchId}")
    public ResponseEntity<ScoreCard> MatchScorecard(@PathVariable String matchId) {
        return ResponseEntity.ok(matchStatsService.getMatchScorecard(matchId));
    }

    @PutMapping("/match/{matchId}/update/{createrId}")
    public ResponseEntity<MatchDto> updateMatchStats(@PathVariable String createrId,@PathVariable String matchId, @RequestBody UpdateScoreDto updateScoreDto) {
        return ResponseEntity.ok(matchStatsService.updateMatchstats(createrId,matchId,updateScoreDto.getPlayerId(),updateScoreDto.getPointType(),updateScoreDto.getPoints(),updateScoreDto.getTeamName()));
    }
//    @PutMapping("/update/{matchId}/undo/{createrId}")
//    public ResponseEntity<MatchDto> undoMatchStats(@PathVariable String createrId,@PathVariable String matchId, @RequestBody UpdateScoreDto updateScoreDto) {
//        return ResponseEntity.ok(matchStatsService.updateMatchstats(createrId,matchId,updateScoreDto.getPlayerId(),updateScoreDto.getPointType(),updateScoreDto.getPoints(),updateScoreDto.getTeamName()));
//    }

}
