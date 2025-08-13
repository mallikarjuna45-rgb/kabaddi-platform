package com.kabaddi.kabaddi.service;
import com.kabaddi.kabaddi.config.MatchWebSocketBroadcaster;
import com.kabaddi.kabaddi.dto.MatchDto;
import com.kabaddi.kabaddi.dto.ScoreCard;
import com.kabaddi.kabaddi.dto.SummaryCard;
import com.kabaddi.kabaddi.dto.UserDto;
import com.kabaddi.kabaddi.entity.MatchStats;
import com.kabaddi.kabaddi.entity.User;
import com.kabaddi.kabaddi.exception.NotfoundException;
import com.kabaddi.kabaddi.repository.MatchStatsRepository;
import com.kabaddi.kabaddi.repository.UserRepository;
import com.kabaddi.kabaddi.util.PointType;
import com.kabaddi.kabaddi.util.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchStatsService {
    private final MatchService matchService;
    private final MatchStatsRepository  matchStatsRepository;
    private final UserRepository userRepository;
    private final MatchWebSocketBroadcaster webSocketBroadcaster;

    public SummaryCard getSummaryCard(String matchId) {
        MatchDto matchDto = matchService.getMatchById(matchId);
        return SummaryCard.builder()
                .matchId(matchId)
                .matchName(matchDto.getMatchName())
                .location(matchDto.getLocation())
                .remainingTime(matchDto.getRemainingDuration())
                .team1Name(matchDto.getTeam1Name())
                .team2Name(matchDto.getTeam2Name())
                .team1PhotoUrl(matchDto.getTeam1PhotoUrl())
                .team2PhotoUrl(matchDto.getTeam2PhotoUrl())
                .team1Score(getScore(matchId,matchDto.getTeam1Name()))
                .team2Score(getScore(matchId,matchDto.getTeam2Name()))
                .matchStatus(matchDto.getStatus())
                .build();
    }

    private Integer getScore(String matchId, String teamName) {
        List<MatchStats> statsList = matchStatsRepository.findByMatchIdAndTeamNameIgnoreCase(matchId, teamName);
        int totalScore = 0;
        for (MatchStats stats : statsList) {
            if (stats.getRaidPoints() != null) {
                totalScore += stats.getRaidPoints();
            }
            if (stats.getDefencePoints() != null) {
                totalScore += stats.getDefencePoints();
            }
        }
        return totalScore;
    }

    public List<ScoreCard> getMatchScorecard(String matchId) {
        MatchDto matchDto = matchService.getMatchById(matchId);
        List<MatchStats> matchStats = matchStatsRepository.findByMatchId(matchId);
        List<ScoreCard> scoreCards = new ArrayList<>();
        for (MatchStats stats : matchStats) {
            ScoreCard scoreCard = new ScoreCard();
            scoreCard.setMatchId(matchId);
            scoreCard.setPlayerId(stats.getPlayerId());
            scoreCard.setPlayerName(getUserNameById(stats.getPlayerId()));
            scoreCard.setRaidPoints(stats.getRaidPoints());
            scoreCard.setDefencePoints(stats.getDefencePoints());
            scoreCards.add(scoreCard);
        }
        log.info("scoreCards: {}", scoreCards);
        return scoreCards;
    }

    public MatchStats updateMatchstats(String matchId, String playerId, PointType type, Integer points) {
        MatchStats stat = matchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId);
        log.info("updateMatchstats: {}", stat);
        if (type == PointType.RAID_POINT) {
            stat.setRaidPoints(stat.getRaidPoints() + points);
        } else {
            stat.setDefencePoints(stat.getDefencePoints() + points);
        }
        log.info("saved to database: {}", stat);
        matchStatsRepository.save(stat);

        // Create and send updated summary to all connected clients
        log.info("send to connected  clients", stat);
        SummaryCard updatedSummary = getSummaryCard(matchId);
        webSocketBroadcaster.sendScoreUpdate(matchId, updatedSummary);
        int raidPoints = getUserRaidPoints(playerId);
        int defencePoints = getUserDefenceoints(playerId);

        // 3. Prepare WebSocket event with updated user score info
        WebSocketEvent userScoreEvent = new WebSocketEvent("SCORE_UPDATED", null, Map.of(
                "raidPoints", raidPoints,
                "defencePoints", defencePoints
        ));

        // 4. Send the user score update to clients subscribed to this user
        webSocketBroadcaster.sendUserScoreUpdate(playerId, userScoreEvent);
        return stat;
    }

    public Integer getUserRaidPoints(String  playerId) {
        List<MatchStats> playerPoints = matchStatsRepository.findByPlayerId(playerId);
        int totalScore = 0;
        for (MatchStats stats : playerPoints) {
            if (stats.getRaidPoints() != null) {
                totalScore += stats.getRaidPoints();
            }
        }
        return totalScore;
    }
    public Integer getUserDefenceoints(String  playerId) {
        List<MatchStats> playerPoints = matchStatsRepository.findByPlayerId(playerId);
        int totalScore = 0;
        for (MatchStats stats : playerPoints) {
            if (stats.getRaidPoints() != null) {
                totalScore += stats.getDefencePoints();
            }
        }
        return totalScore;
    }
    public String getUserNameById(String id) {
        User user =  userRepository.findById(id).orElseThrow(()-> new NotfoundException("user not found"));
        return user.getName();
    }

}
