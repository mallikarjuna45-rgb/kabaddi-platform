package com.kabaddi.kabaddi.controller;
import com.kabaddi.kabaddi.dto.MatchDto;
import com.kabaddi.kabaddi.dto.ScoreCard;
import com.kabaddi.kabaddi.dto.UpdateScoreDto;
import com.kabaddi.kabaddi.entity.MatchStats;
import com.kabaddi.kabaddi.entity.User;
import com.kabaddi.kabaddi.exception.NotfoundException;
import com.kabaddi.kabaddi.repository.UserRepository;
import com.kabaddi.kabaddi.service.MatchStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;



@RestController
@RequestMapping("/matchstats")
@RequiredArgsConstructor
public class MatchStatsController {

    private final MatchStatsService matchStatsService;
    private final UserRepository userRepository;

    @GetMapping("/match/scorecard/{matchId}")
    public ResponseEntity<ScoreCard> MatchScorecard(@PathVariable String matchId) {
        return ResponseEntity.ok(matchStatsService.getMatchScorecard(matchId));
    }
    @GetMapping("/match/livescorecard/{matchId}/user")
    public ResponseEntity<ScoreCard> LiveMatchScorecard(@PathVariable String matchId, Principal
            principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotfoundException(username));
        String userId = user.getId();

        return ResponseEntity.ok(matchStatsService.getMatchScorecard(matchId,userId));
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
