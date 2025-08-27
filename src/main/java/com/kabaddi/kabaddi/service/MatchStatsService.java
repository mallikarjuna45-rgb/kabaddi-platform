package com.kabaddi.kabaddi.service;
import com.kabaddi.kabaddi.config.MatchWebSocketBroadcaster;
import com.kabaddi.kabaddi.dto.*;
import com.kabaddi.kabaddi.entity.Match;
import com.kabaddi.kabaddi.entity.MatchStats;
import com.kabaddi.kabaddi.entity.User;
import com.kabaddi.kabaddi.exception.NotfoundException;
import com.kabaddi.kabaddi.repository.MatchStatsRepository;
import com.kabaddi.kabaddi.repository.UserRepository;
import com.kabaddi.kabaddi.util.MatchStatus;
import com.kabaddi.kabaddi.util.PlayerResponse;
import com.kabaddi.kabaddi.util.PointType;
import com.kabaddi.kabaddi.util.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
  //  private final UserService userService;

//    public SummaryCard getSummaryCard(String matchId) {
//        MatchDto matchDto = matchService.getMatchById(matchId);
//        return matchDto;
//    }

//    private Integer getScore(String matchId, String teamName) {
//        List<MatchStats> statsList = matchStatsRepository.findByMatchIdAndTeamNameIgnoreCase(matchId, teamName);
//        int totalScore = 0;
//        for (MatchStats stats : statsList) {
//            if (stats.getRaidPoints() != null) {
//                totalScore += stats.getRaidPoints();
//            }
//            if (stats.getDefencePoints() != null) {
//                totalScore += stats.getDefencePoints();
//            }
//        }
//        return totalScore;
//    }

    public ScoreCard getMatchScorecard(String matchId) {
        MatchDto matchDto = matchService.getMatchById(matchId);
        List<MatchStats> matchStats = matchStatsRepository.findByMatchId(matchId);
        ScoreCard scoreCard = new ScoreCard();
        scoreCard.setMatchId(matchId);
        scoreCard.setMatchName(matchDto.getMatchName());
        scoreCard.setTeam1Name(matchDto.getTeam1Name());
        scoreCard.setTeam2Name(matchDto.getTeam2Name());
        scoreCard.setLocation(matchDto.getLocation());
        scoreCard.setCreatedBy(matchDto.getCreatedBy());
        scoreCard.setCreatedAt(matchDto.getCreatedAt());
        scoreCard.setStatus(matchDto.getStatus());
        scoreCard.setRemainingDuration(matchDto.getRemainingDuration());
        List<TeamStats> team1Stats = new ArrayList<>();
        List<TeamStats> team2Stats = new ArrayList<>();
        for (MatchStats stats : matchStats) {
            if(stats.getTeamName().equals(matchDto.getTeam1Name())){
                String name = userRepository.findById(stats.getPlayerId())
                        .orElseThrow(() -> new NotfoundException("User not found with id: " + stats.getPlayerId()))
                        .getName();

                TeamStats teamStats = TeamStats.builder()
                        .playerId(stats.getPlayerId())
                        .playerName(name)
                        .raidPoints(stats.getRaidPoints())
                        .tacklePoints(stats.getTacklePoints())
                        .build();
                team1Stats.add(teamStats);
            }
            else if(stats.getTeamName().equals(matchDto.getTeam2Name())){
                String name = userRepository.findById(stats.getPlayerId())
                        .orElseThrow(() -> new NotfoundException("User not found with id: " + stats.getPlayerId()))
                        .getName();
                TeamStats teamStats = TeamStats.builder()
                        .playerId(stats.getPlayerId())
                        .playerName(name)
                        .raidPoints(stats.getRaidPoints())
                        .tacklePoints(stats.getTacklePoints())
                        .build();
                team2Stats.add(teamStats);
            }
            scoreCard.setTeam1(team1Stats);
            scoreCard.setTeam2(team2Stats);

        }
        log.info("scoreCards: {}", scoreCard);
        return scoreCard;
    }

    public MatchDto updateMatchstats(String createrId,String matchId, String playerId, PointType type, Integer points,String teamName) {
        MatchStats stat = matchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId);
        matchService.getMatchIfCreator(matchId, createrId);
        if(stat == null){
            throw new NotfoundException("User not Found");
        }
        if(!(matchService.getMatchById(matchId).getStatus() == MatchStatus.LIVE)){
            throw new NotfoundException("Match Status Not Live ");
        }
        if (type == PointType.RAID_POINT) {
            stat.setRaidPoints(stat.getRaidPoints() + points);
        } else {
            stat.setTacklePoints(stat.getTacklePoints() + points);
        }
        matchStatsRepository.save(stat);


        // Create and send updated summary to all connected clients
//        log.info("send to connected  clients", stat);
//        SummaryCard updatedSummary = getSummaryCard(matchId);
//        webSocketBroadcaster.sendScoreUpdate(matchId, updatedSummary);
       // int raidPoints = getUserRaidPoints(playerId);
        //int defencePoints = getUserDefenceoints(playerId);

        // 3. Prepare WebSocket event with updated user score info
//        WebSocketEvent userScoreEvent = new WebSocketEvent("SCORE_UPDATED", null, Map.of(
//                "raidPoints", raidPoints,
//                "defencePoints", defencePoints
//        ));

        // 4. Send the user score update to clients subscribed to this user
        //webSocketBroadcaster.sendUserScoreUpdate(playerId, userScoreEvent);
        return matchService.updateTeamScore(matchId,teamName,points);
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
            if (stats.getTacklePoints() != null) {
                totalScore += stats.getTacklePoints();
            }
        }
        return totalScore;
    }

    public Integer getTotalMatchesPlayedByUser(String userId) {
        return matchStatsRepository.findByPlayerId(userId).size();
    }

    public LocalDate  getDebutMatch(String userId) {
        MatchStats debutMatch = matchStatsRepository.findFirstByPlayerIdOrderByMatchIdAsc(userId);
//        if(debutMatch == null){ throw new NotfoundException("To see stats you have to pal"); }
        if (debutMatch == null) {
            return null;
        }
        MatchDto matchDto = matchService.getMatchById(debutMatch.getMatchId());
    return matchDto.getCreatedAt();
    }

    public List<UserMatch> getUserTopMatches(String userId) {
        List<MatchStats> matchStats = matchStatsRepository.findTop5ByPlayerIdOrderByMatchIdDesc(userId);
        List<UserMatch> userMatchs = new ArrayList<>();
        for (MatchStats stats : matchStats) {
            UserMatch userMatch = new UserMatch();
            userMatch.setMatchId(stats.getMatchId());
            MatchDto matchDto = matchService.getMatchById(stats.getMatchId());
            String oppositeTeam = matchDto.getTeam1Name().equals(stats.getTeamName())? matchDto.getTeam2Name():matchDto.getTeam1Name() ;
            userMatch.setOppositeTeamName(oppositeTeam);
            userMatch.setLocation(matchDto.getLocation());
            userMatch.setTeam1Score(matchDto.getTeam1Score());
            userMatch.setTeam2Score(matchDto.getTeam2Score());
            userMatch.setTotalPoints(stats.getTacklePoints() + stats.getRaidPoints());
            userMatch.setRaidPoints(stats.getRaidPoints());
            userMatch.setTacklePoints(stats.getTacklePoints());
            userMatchs.add(userMatch);
        }
        return userMatchs;
    }

    public UserStats getUserStats(String userId) {

        return UserStats.builder()
                .userId(userId)
                .raidPoints(getUserRaidPoints(userId))
                .tacklePoints(getUserDefenceoints(userId))
                .totalMatches(getTotalMatchesPlayedByUser(userId))
                .debutMatch(getDebutMatch(userId))
                .totalPoints(getUserRaidPoints(userId)+getUserDefenceoints(userId))
                .matches(getUserTopMatches(userId))
                .build();
    }

    public List<MatchDto> getMatchesPlayedByUser(String playerId) {
        List<MatchStats>stats = matchStatsRepository.findByPlayerId(playerId);
        List<MatchDto> matchDtos = new ArrayList<>();
        if(stats==null){ throw new NotfoundException("No Matches found"); }
        for (MatchStats stat : stats) {
            MatchDto matchDto = matchService.getMatchById(stat.getMatchId());
            matchDtos.add(matchDto);
        }
        return matchDtos;
    }


}
