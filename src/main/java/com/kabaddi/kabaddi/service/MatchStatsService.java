package com.kabaddi.kabaddi.service;
import com.kabaddi.kabaddi.dto.*;

import com.kabaddi.kabaddi.entity.Commentary;
import com.kabaddi.kabaddi.entity.MatchStats;
import com.kabaddi.kabaddi.entity.User;
import com.kabaddi.kabaddi.exception.NotfoundException;
import com.kabaddi.kabaddi.repository.MatchStatsRepository;
import com.kabaddi.kabaddi.repository.UserRepository;
import com.kabaddi.kabaddi.util.MatchStatus;
import com.kabaddi.kabaddi.util.PointType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchStatsService {
    private final MatchService matchService;
    private final MatchStatsRepository  matchStatsRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CommentaryService commentaryService;
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
        log.info(" Started getMatchScorecard");
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
       // Integer team1Score = 0;
       // Integer team2Score = 0;
        scoreCard.setTeam1PhotoUrl(matchDto.getTeam1PhotoUrl());
        scoreCard.setTeam2PhotoUrl(matchDto.getTeam2PhotoUrl());
        for (MatchStats stats : matchStats) {
            if(stats.getTeamName().equals(matchDto.getTeam1Name())){
                String name = userRepository.findById(stats.getPlayerId())
                        .orElseThrow(() -> new NotfoundException("User not found with id: " + stats.getPlayerId()))
                        .getName();
               // team1Score = team1Score + stats.getRaidPoints() +stats.getTacklePoints();
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
               // team2Score = team2Score + stats.getRaidPoints()+stats.getTacklePoints();;

                TeamStats teamStats = TeamStats.builder()
                        .playerId(stats.getPlayerId())
                        .playerName(name)
                        .raidPoints(stats.getRaidPoints())
                        .tacklePoints(stats.getTacklePoints())
                        .build();
                team2Stats.add(teamStats);
            }
        }
        scoreCard.setTeam1(team1Stats);
        scoreCard.setTeam2(team2Stats);
        scoreCard.setTeam1Score(matchDto.getTeam1Score());
        scoreCard.setTeam2Score(matchDto.getTeam2Score());
        log.info("Team 1{} score is {}", scoreCard.getTeam1Name(),scoreCard.getTeam1Score());
        log.info("Team 2{} score is {}", scoreCard.getTeam2Name(),scoreCard.getTeam2Score());

        log.info("scoreCards: {}", scoreCard);

        log.info("Completed getMatchScorecard");
        return scoreCard;
    }
    public ScoreCard getMatchScorecard(String matchId,String createrId) {
        log.info("Starting  LiveMatchScorecard");
        MatchDto matchDto = matchService.getMatchById(matchId);
        //if(matchDto.getStatus().equals(MatchStatus.COMPLETED)){throw new NotfoundException("Match already completed ,view ScoreCard ");}
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
        scoreCard.setTeam1Score(matchDto.getTeam1Score());
        scoreCard.setTeam2Score(matchDto.getTeam2Score());
        scoreCard.setTeam1PhotoUrl(matchDto.getTeam1PhotoUrl());
        scoreCard.setTeam2PhotoUrl(matchDto.getTeam2PhotoUrl());
        //int team1Score=0;
       // int team2Score=0;
        for (MatchStats stats : matchStats) {
            if(stats.getTeamName().equals(matchDto.getTeam1Name())){
                String name = userRepository.findById(stats.getPlayerId())
                        .orElseThrow(() -> new NotfoundException("User not found with id: " + stats.getPlayerId()))
                        .getName();
                //team1Score = team1Score + stats.getRaidPoints() + stats.getTacklePoints() ;
                TeamStats teamStats = TeamStats.builder()
                        .playerId(stats.getPlayerId())
                        .playerName(name)
                        .raidPoints(stats.getRaidPoints())
                        .tacklePoints(stats.getTacklePoints())
                        .build();
                team1Stats.add(teamStats);
            }
            else if(stats.getTeamName().equals(matchDto.getTeam2Name())) {
                String name = userRepository.findById(stats.getPlayerId())
                        .orElseThrow(() -> new NotfoundException("User not found with id: " + stats.getPlayerId()))
                        .getName();
               // team2Score = team2Score + stats.getRaidPoints() + stats.getTacklePoints();
                TeamStats teamStats = TeamStats.builder()
                        .playerId(stats.getPlayerId())
                        .playerName(name)
                        .raidPoints(stats.getRaidPoints())
                        .tacklePoints(stats.getTacklePoints())
                        .build();
                team2Stats.add(teamStats);
            }
        }
        scoreCard.setTeam1(team1Stats);
        scoreCard.setTeam2(team2Stats);
        return scoreCard;
    }


    @Transactional
    public MatchDto updateMatchstats(String createrId,String matchId, String playerId, PointType type, Integer points,String teamName) {
        log.info("Starting updateMatchstats");
        log.info("Updating Match Score for matchId{} and for Team {}",matchId,teamName);
        String liveCommentary ="";
        MatchDto updatedMatchDto = matchService.updateTeamScore(matchId,teamName,points);
        log.info("updating Live match stats");
        log.info("Received Data");
        log.info("matchId: {}, createrId: {}", matchId, createrId);
        log.info("playerId: {}, points: {}", playerId, points);
        log.info("teamName: {}", teamName);
        log.info("PointType: {}", type);
        User player = userRepository.findById(playerId).orElseThrow(() -> new NotfoundException("User not found with id: " + createrId));
        if (points > 0) {

        if(type == PointType.RAID_POINT || type == PointType.TACKLE_POINT){
            liveCommentary =teamName+" : "+ player.getName() + " scored "+ points +" "+String.join(" ",String.valueOf(type).toLowerCase().split("_")) + (points==1?"":"s");
        }
        else{
            liveCommentary = teamName+"  got " + points+" "+ type+(points==1?"":"s");
        }
        }
        else{
            liveCommentary ="Now Score is Updated Correctly";
        }
        if(type != PointType.ALL_OUT_POINT &&  type != PointType.TECHNICAL_POINT) {
            MatchStats stat = matchStatsRepository.findByMatchIdAndPlayerId(matchId, playerId);
            matchService.getMatchIfCreator(matchId, createrId);
            if (matchService.getMatchById(matchId).getRemainingDuration() <= 0) {
                throw new NotfoundException("match Duration Completed");
            }
            if (stat == null) {
                throw new NotfoundException("User not Found in match stats"); // Clarified error message
            }
            if (!(matchService.getMatchById(matchId).getStatus() == MatchStatus.LIVE)) {
                throw new NotfoundException("Match Status Not Live ");
            }
            if (!stat.getTeamName().equals(teamName)) {
                throw new NotfoundException("Player '" + playerId + "' does not belong to team '" + teamName + "'.");
            }
            if (type == PointType.RAID_POINT) {
                if (points < 0 && stat.getRaidPoints() < (-1 * points)) {
                    throw new NotfoundException("Raid points for user cant be negative");
                }
                stat.setRaidPoints(stat.getRaidPoints() + points);
            } else { // Explicitly check for TACKLE_POINT
                if (points < 0 && stat.getTacklePoints() < (-1 * points)) {
                    throw new NotfoundException("Tackle points for user cant be negative"); // Corrected message
                }
                stat.setTacklePoints(stat.getTacklePoints() + points);
            }
            matchStatsRepository.save(stat);
        }
        // After updating individual player stats, update the overall match score
        // and let MatchService handle the WebSocket publication for the matchDto.
        Commentary commentary = new Commentary();
        commentary.setMatchId(matchId);
        commentary.setCommentary(liveCommentary);
        commentary.setDateAndTime(LocalDateTime.now());;
        commentaryService.saveCommentary(commentary);
        // After updating player stats, create a new ScoreCard DTO with the latest data
        ScoreCard updatedScoreCard = getMatchScorecard(matchId); // A new method to fetch the full scorecard with player stats
        updatedScoreCard.setLiveCommentary(liveCommentary);
        // Publish the full ScoreCard DTO to the WebSocket topic
        messagingTemplate.convertAndSend("/topic/matches/" + matchId, updatedScoreCard);
        log.info("before sending commentary");
        log.info("commentary"+commentaryService.findAllCommentaryForMatch(matchId));
        messagingTemplate.convertAndSend("/topic/matches/commentary" + matchId, commentaryService.findAllCommentaryForMatch(matchId));
        log.info("after sending commentary");
        // Additionally, if you want to send individual player stats updates, you can do so here.
        // For simplicity, we'll focus on the main matchDto which contains aggregated scores.
        // If you need more granular updates (e.g., specific player score changes), we can
        // create a dedicated DTO for that and publish to a different topic or include it in the MatchDto.
        broadcastMatchUpdate(updatedMatchDto);
        //LiveCommentary(commentary);
        return updatedMatchDto;
    }
    public void broadcastMatchUpdate(MatchDto matchDto) {
        // Only broadcast matches that are not UPCOMING or COMPLETED
        if (matchDto.getStatus() != MatchStatus.UPCOMING && matchDto.getStatus() != MatchStatus.COMPLETED) {
            // We'll use a new topic for landing page updates
            messagingTemplate.convertAndSend("/topic/liveMatchesSummary", matchDto);
            log.info("Broadcasted live match summary for match {}: {}", matchDto.getId(), matchDto.getStatus());
        }
    }
//    public void LiveCommentary(String) {
//
//        messagingTemplate.convertAndSend("/topic/");
//    }
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
