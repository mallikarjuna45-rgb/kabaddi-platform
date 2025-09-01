package com.kabaddi.kabaddi.service;
import com.kabaddi.kabaddi.dto.*;

import com.kabaddi.kabaddi.entity.MatchStats;
import com.kabaddi.kabaddi.entity.User;
import com.kabaddi.kabaddi.exception.NotfoundException;
import com.kabaddi.kabaddi.repository.MatchStatsRepository;
import com.kabaddi.kabaddi.repository.UserRepository;
import com.kabaddi.kabaddi.util.MatchStatus;
import com.kabaddi.kabaddi.util.PointType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchStatsService {
    private final MatchService matchService;
    private final MatchStatsRepository  matchStatsRepository;
    private final UserRepository userRepository;

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
        scoreCard.setCreatorName(matchDto.getCreatorName());
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
    public ScoreCard getMatchScorecard(String matchId,String createrId) {
        log.info("getLiveMatchScorecard");
        log.info("matchId: {}, createrId: {}", matchId, createrId);
        MatchDto matchDto = matchService.getMatchById(matchId);
        //if(matchDto.getStatus().equals(MatchStatus.COMPLETED)){throw new NotfoundException("Match already completed ,view ScoreCard ");}
        log.info("no errors were found while searching for{}",matchId);
        if(!matchDto.getCreatedBy().equals(createrId)){
            throw new NotfoundException("Only the Match Creater Can doo the Live-Updating Scorecard");
        }
        List<MatchStats> matchStats = matchStatsRepository.findByMatchId(matchId);
        log.info("recorede matchStats: {}", matchStats);
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
        if(matchService.getMatchById(matchId).getRemainingDuration()<=0){
            throw new NotfoundException("match Duration Completed");
        }
        if(stat == null){
            throw new NotfoundException("User not Found");
        }
        if(!(matchService.getMatchById(matchId).getStatus() == MatchStatus.LIVE)){
            throw new NotfoundException("Match Status Not Live ");
        }
        if (type == PointType.RAID_POINT) {
            if(points <0 && stat.getRaidPoints() < -1*points){
                throw new NotfoundException("Raid points for user cant be negative");
            }
            stat.setRaidPoints(stat.getRaidPoints() + points);
        } else {
            if(points <0 && stat.getTacklePoints()< -1*points){
                throw new NotfoundException("Raid points for user cant be negative");
            }
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
        log.info("raid points received player id " + playerId);
        List<MatchStats> playerPoints = matchStatsRepository.findByPlayerId(playerId);
        int totalScore = 0;
        for (MatchStats stats : playerPoints) {
            if (stats.getRaidPoints() != null) {
                totalScore += stats.getRaidPoints();
            }
        }
        log.info("total score: {}", totalScore);
        return totalScore;
    }
    public Integer getUserDefenceoints(String  playerId) {
        log.info("defence points received player id " + playerId);
        List<MatchStats> playerPoints = matchStatsRepository.findByPlayerId(playerId);
        int totalScore = 0;
        for (MatchStats stats : playerPoints) {
            if (stats.getTacklePoints() != null) {
                totalScore += stats.getTacklePoints();
            }
        }
        log.info("total score: {}", totalScore);
        return totalScore;
    }

    public Integer getTotalMatchesPlayedByUser(String userId) {
        log.info("total matches played by user id {}", userId);
        return matchStatsRepository.findByPlayerId(userId).size();
    }

    public LocalDate  getDebutMatch(String userId) {
        MatchStats debutMatch = matchStatsRepository.findFirstByPlayerIdOrderByMatchIdAsc(userId);
//        if(debutMatch == null){ throw new NotfoundException("To see stats you have to pal"); }
        log.info("debutMatch: {}", debutMatch);
        if (debutMatch == null) {
            return null;
        }
        log.info("debutMatch: {}", debutMatch.getMatchId());
        MatchDto matchDto = matchService.getMatchById(debutMatch.getMatchId());
    return matchDto.getCreatedAt();
    }

    public List<UserMatch> getUserTopMatches(String userId) {
        List<MatchStats> matchStats = matchStatsRepository.findTop5ByPlayerIdOrderByMatchIdDesc(userId);
        List<UserMatch> userMatchs = new ArrayList<>();
        for (MatchStats stats : matchStats) {
            UserMatch userMatch = new UserMatch();
            userMatch.setMatchId(stats.getMatchId());
            log.info("finding match Details of : {}", stats.getMatchId());
            if(!matchService.existsById(stats.getMatchId())) continue;
            MatchDto matchDto = matchService.getMatchById(stats.getMatchId());
            if(! (matchDto.getStatus() == MatchStatus.COMPLETED)) continue;
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
        log.info("stats: {}", stats);
        List<MatchDto> matchDtos = new ArrayList<>();
        if(stats==null){ throw new NotfoundException("No Matches found"); }
        for (MatchStats stat : stats) {
            log.info("stats: {}", stat.getMatchId());
            if(matchService.existsById(stat.getMatchId())) {

                log.info("Match Exists: {}", stat.getMatchId());
                MatchDto matchDto = matchService.getMatchById(stat.getMatchId());
                log.info("matchDto: {}", matchDto);
                matchDtos.add(matchDto);
            }
        }
        log.info("matchDtos: {}", matchDtos);
        return matchDtos;
    }
    public void deleteMatchStatsByMatchId(String matchId) {
        matchStatsRepository.deleteByMatchId(matchId);
    }

}
