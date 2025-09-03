package com.kabaddi.kabaddi.controller;

import com.kabaddi.kabaddi.dto.CreateMatchRequest;
import com.kabaddi.kabaddi.dto.LiveScorerCard;
import com.kabaddi.kabaddi.dto.MatchDto;
import com.kabaddi.kabaddi.service.MatchService;
import com.kabaddi.kabaddi.service.MatchStatsService;
import com.kabaddi.kabaddi.util.WebSocketEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchStatsService matchStatsService;

    @PostMapping("/create")
    public ResponseEntity<MatchDto> createMach(@Valid @ModelAttribute CreateMatchRequest createMatchRequest) {
        return ResponseEntity.ok(matchService.createMatch(createMatchRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<List<MatchDto>> getAllMatches(){
        return ResponseEntity.ok(matchService.getAllMatches());
    }
    @GetMapping("/live")
    public ResponseEntity<List<MatchDto>> getAllLiveMatches(){
        return ResponseEntity.ok(matchService.getAllLiveMatches());
    }
    @GetMapping("/completed")
    public List<MatchDto> getAllCompletedMatches(){
        return matchService.getAllCompletedMatches();
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable String matchId){
        return ResponseEntity.ok(matchService.getMatchById(matchId));
    }

    @DeleteMapping("/delete/{matchId}")
    public ResponseEntity<Void> deleteMatchById(@PathVariable String matchId){
        matchService.deleteById(matchId);
        //matchStatsService.deleteMatchStatsByMatchId(matchId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/match/update/{matchId}/{createrId}")
    public ResponseEntity<MatchDto> updateMatchById(
            @PathVariable String matchId,
            @PathVariable String createrId,
            @Valid @ModelAttribute CreateMatchRequest createMatchRequest
    ) {
        MatchDto updatedMatch = matchService.updateMatchById(matchId, createrId, createMatchRequest);
        return ResponseEntity.ok(updatedMatch);
    }

    @PutMapping("/match/{setType}/{matchId}/{createrId}")
    public ResponseEntity<MatchDto> setMatch(@PathVariable String setType, @PathVariable String matchId, @PathVariable String createrId) {

        return ResponseEntity.ok(matchService.setMatch(setType,matchId, createrId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MatchDto>> searchByMatchName(@RequestParam String matchName) {
        return ResponseEntity.ok(matchService.searchByMatchName(matchName));
    }





}
