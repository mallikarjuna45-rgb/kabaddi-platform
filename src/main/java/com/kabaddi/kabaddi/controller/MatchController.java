package com.kabaddi.kabaddi.controller;

import com.kabaddi.kabaddi.dto.CreateMatchRequest;
import com.kabaddi.kabaddi.dto.MatchDto;
import com.kabaddi.kabaddi.service.MatchService;
import com.kabaddi.kabaddi.util.WebSocketEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final SimpMessagingTemplate messagingTemplate;
    @PostMapping("/create")
    public ResponseEntity<MatchDto> createMach(@Valid @ModelAttribute CreateMatchRequest createMatchRequest) {
        return ResponseEntity.ok(matchService.createMatch(createMatchRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<List<MatchDto>> getAllMatches(){
        return ResponseEntity.ok(matchService.getAllMatches());
    }
    @GetMapping("/all/live")
    public ResponseEntity<List<MatchDto>> getAllLiveMatches(){
        return ResponseEntity.ok(matchService.getAllLiveMatches());
    }
    @GetMapping("/all/completed")
    public List<MatchDto> getAllCompletedMatches(){
        return matchService.getAllCompletedMatches();
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable String matchId){
        return ResponseEntity.ok(matchService.getMatchById(matchId));
    }

    @DeleteMapping("/delete/{matchId}")
    public ResponseEntity<Void> deleteMatchById(@PathVariable String matchId){
        matchService.deleteById(matchId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{matchId}/{userId}")
    public ResponseEntity<MatchDto> updateMatchById(
            @PathVariable String matchId,
            @PathVariable String userId,
            @Valid @ModelAttribute CreateMatchRequest createMatchRequest
    ) {
        MatchDto updatedMatch = matchService.updateMatchById(matchId, userId, createMatchRequest);
        messagingTemplate.convertAndSend(
                "/topic/match/" + matchId,
                new WebSocketEvent("MATCH_UPDATED", matchId, updatedMatch)
        );
        return ResponseEntity.ok(updatedMatch);
    }

    @PutMapping("/start/{matchId}/{userId}")
    public ResponseEntity<MatchDto> startMatch(@PathVariable String matchId, @PathVariable String userId) {
        messagingTemplate.convertAndSend(
                "/topic/match/" + matchId,
                new WebSocketEvent("MATCH_STARTED", matchId)
        );
        return ResponseEntity.ok(matchService.startMatch(matchId, userId));
    }

    @PutMapping("/pause/{matchId}/{userId}")
    public ResponseEntity<MatchDto> pauseMatch(@PathVariable String matchId, @PathVariable String userId) {
        messagingTemplate.convertAndSend(
                "/topic/match/" + matchId,
                new WebSocketEvent("MATCH_PAUSED", matchId)
        );
        return ResponseEntity.ok(matchService.pauseMatch(matchId, userId));
    }

    @PutMapping("/resume/{matchId}/{userId}")
    public ResponseEntity<MatchDto> resumeMatch(@PathVariable String matchId, @PathVariable String userId) {
        messagingTemplate.convertAndSend(
                "/topic/match/" + matchId,
                new WebSocketEvent("MATCH_RESUMED", matchId)
        );
        return ResponseEntity.ok(matchService.resumeMatch(matchId, userId));
    }

    @PutMapping("/end/{matchId}/{userId}")
    public ResponseEntity<MatchDto> endMatch(@PathVariable String matchId, @PathVariable String userId) {
        messagingTemplate.convertAndSend(
                "/topic/match/" + matchId,
                new WebSocketEvent("MATCH_ENDED", matchId)
        );
        return ResponseEntity.ok(matchService.endMatch(matchId, userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MatchDto>> searchByMatchName(@RequestParam String matchName) {
        return ResponseEntity.ok(matchService.searchByMatchName(matchName));
    }





}
