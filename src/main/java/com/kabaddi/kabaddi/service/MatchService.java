package com.kabaddi.kabaddi.service;

import com.kabaddi.kabaddi.dto.CreateMatchRequest;
import com.kabaddi.kabaddi.dto.LiveScorerCard;
import com.kabaddi.kabaddi.dto.MatchDto;
import com.kabaddi.kabaddi.entity.Match;
import com.kabaddi.kabaddi.entity.MatchStats;
import com.kabaddi.kabaddi.entity.User;
import com.kabaddi.kabaddi.exception.NotfoundException;
import com.kabaddi.kabaddi.repository.MatchRepository;
import com.kabaddi.kabaddi.repository.MatchStatsRepository;
import com.kabaddi.kabaddi.repository.UserRepository;
import com.kabaddi.kabaddi.util.MatchStatus;

import com.kabaddi.kabaddi.util.PlayerResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchStatsRepository matchStatsRepository;
    private final MatchRepository matchRepository;
    private final ImageUploadService imageUploadService;
    private final UserRepository userRepository;


    public MatchDto createMatch(CreateMatchRequest request){
        log.info("recived data for creating match " + request.toString());
        if(!userRepository.existsById(request.getCreatedBy())){
            log.info("user does not exist for creating match " + request.getCreatedBy());
            throw new NotfoundException("User not found");
        }
        try {
            String team1 = null;
            if(request.getTeam1Photo() != null)team1 =imageUploadService.uploadImage(request.getTeam1Photo());
            String team2 = null;
            if(request.getTeam2Photo() != null)team2 = imageUploadService.uploadImage(request.getTeam2Photo());
            userRepository.existsById(request.getCreatedBy());
            if(request.getTeam1Name().equals(request.getTeam2Name())){
                throw new NotfoundException("Teams name should be different");
            }
            Match new_match = Match.builder()
                        .matchName(request.getMatchName())
                        .team1Name(request.getTeam1Name())
                        .team2Name(request.getTeam2Name())
                        .team1PhotoUrl(team1)
                        .team2PhotoUrl(team2)
                        .createdBy(request.getCreatedBy())
                        .status(MatchStatus.UPCOMING)
                        .team1Score(0)
                        .team2Score(0)
                        .createdAt(LocalDate.now())
                        .totalDuration(request.getTotalDuration() * 60)
                        .location(request.getLocation())
                        .build();
                matchRepository.save(new_match);

            // creating match_stats
            for (String playerId : request.getTeam1Players()) {
                MatchStats stats = new MatchStats();
                if(!userRepository.existsById(playerId)){
                    matchRepository.deleteById(new_match.getId());
                    throw new NotfoundException("Player not found");
                }
                stats.setMatchId(new_match.getId());
                stats.setPlayerId(playerId);
                stats.setTeamName(request.getTeam1Name());
                stats.setRaidPoints(0);
                stats.setTacklePoints(0);
                matchStatsRepository.save(stats);
            }
            for (String playerId : request.getTeam2Players()) {
                MatchStats stats = new MatchStats();
                if(request.getTeam1Players().contains(playerId)) {
                    matchRepository.deleteById(new_match.getId());
                    throw new NotfoundException("Player with id " + playerId + " already exists in " + request.getTeam1Name());
                }
                if(!userRepository.existsById(playerId)) throw new NotfoundException("Player not found");
                stats.setMatchId(new_match.getId());
                stats.setPlayerId(playerId);
                stats.setTeamName(request.getTeam2Name());
                stats.setRaidPoints(0);
                stats.setTacklePoints(0);
                matchStatsRepository.save(stats);
            }
            log.info("Match and Match stats created successfully");
            return convertToDto(new_match);
        }catch (Exception e) {
            throw new NotfoundException("error in creating match " + e.getMessage());
        }
    }


    public List<MatchDto> getAllMatches() {
        List<MatchDto> dtos = new ArrayList<>();
        for(Match match : matchRepository.findAll()){
            dtos.add(convertToDto(match));
        }
        return dtos;
    }
    public List<MatchDto> getAllLiveMatches() {
        List<MatchDto> dtos = new ArrayList<>();
        List<Match> matches = matchRepository.findByStatus(MatchStatus.LIVE);
        for(Match match : matches){
            dtos.add(convertToDto(match));
        }
        return dtos;
    }
    public List<MatchDto> getAllCompletedMatches() {
        List<MatchDto> dtos = new ArrayList<>();
        List<Match> matches = matchRepository.findByStatus(MatchStatus.COMPLETED);
        for(Match match : matches){
            dtos.add(convertToDto(match));
        }
        return dtos;
    }
    public MatchDto getMatchById(String matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotfoundException("Match not found"));
        return convertToDto(match);
    }

    public void deleteById(String matchId) {
        if(matchRepository.findById(matchId).isEmpty()){
            throw new NotfoundException("Match not found");
        }
        matchRepository.deleteById(matchId);
    }
    public MatchDto updateMatchById(String matchId, String userId, CreateMatchRequest request) {
        // 1. Check if match exists
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotfoundException("Match not found"));

        // 2. Authorization check: only creator can update
        if (!match.getCreatedBy().equals(userId)) {
            throw new NotfoundException("Only the creator can update the match");
        }

        try {
            // 3. Update basic fields
            match.setMatchName(request.getMatchName());
            match.setTeam1Name(request.getTeam1Name());
            match.setTeam2Name(request.getTeam2Name());
            match.setTotalDuration(request.getTotalDuration() * 60);
            match.setLocation(request.getLocation());

            // 4. Update images if provided (optional)
            if (request.getTeam1Photo() != null && !request.getTeam1Photo().isEmpty()) {
                String team1PhotoUrl = imageUploadService.uploadImage(request.getTeam1Photo());
                match.setTeam1PhotoUrl(team1PhotoUrl);
            }
            if (request.getTeam2Photo() != null && !request.getTeam2Photo().isEmpty()) {
                String team2PhotoUrl = imageUploadService.uploadImage(request.getTeam2Photo());
                match.setTeam2PhotoUrl(team2PhotoUrl);
            }

            // 5. Save updated match
            matchRepository.save(match);

            // 6. Remove old match stats for this match
            matchStatsRepository.deleteByMatchId(matchId);

            // 7. Create new match stats for team 1 player
            for (String playerId : request.getTeam1Players()) {
                MatchStats stats = new MatchStats();
                stats.setMatchId(matchId);
                stats.setPlayerId(playerId);
                stats.setTeamName(request.getTeam1Name());
                stats.setRaidPoints(0);
                stats.setTacklePoints(0);
                matchStatsRepository.save(stats);
            }

            // 8. Create new match stats for team 2 players
            for (String playerId : request.getTeam2Players()) {
                MatchStats stats = new MatchStats();
                stats.setMatchId(matchId);
                stats.setPlayerId(playerId);
                stats.setTeamName(request.getTeam2Name());
                stats.setRaidPoints(0);
                stats.setTacklePoints(0);
                matchStatsRepository.save(stats);
            }

            // 9. Return updated match wrapped in list
            return convertToDto(match);

        } catch (IOException e) {
            throw new NotfoundException("Error uploading images");
        } catch (Exception e) {
            throw new NotfoundException("Error updating match");
        }
    }
    public MatchDto startMatch(String matchId, String userId) {
        Match match = getMatchIfCreator(matchId, userId);

        if (match.getStatus() != MatchStatus.UPCOMING) {
            throw new NotfoundException("Match already started or completed");
        }

        match.setStatus(MatchStatus.LIVE);
        match.setStartTime(LocalDateTime.now());
        match.setRemainingDuration(match.getTotalDuration());
        matchRepository.save(match);
        return convertToDto(match);
    }

    public MatchDto pauseMatch(String matchId, String userId) {
        Match match = getMatchIfCreator(matchId, userId);

        if (match.getStatus() != MatchStatus.LIVE) {
            throw new NotfoundException("Match is not live");
        }

        long elapsed = java.time.Duration.between(match.getStartTime(), LocalDateTime.now()).getSeconds();
        match.setRemainingDuration(match.getRemainingDuration() - (int) elapsed);
        match.setPauseTime(LocalDateTime.now());
        match.setStatus(MatchStatus.PAUSED);
        matchRepository.save(match);
        return convertToDto(match);
    }

    public MatchDto resumeMatch(String matchId, String userId) {
        Match match = getMatchIfCreator(matchId, userId);

        if (match.getStatus() != MatchStatus.PAUSED) {
            throw new NotfoundException("Match is not paused");
        }

        match.setStartTime(LocalDateTime.now());
        match.setStatus(MatchStatus.LIVE);
        matchRepository.save(match);
        return convertToDto(match);
    }

    public MatchDto endMatch(String matchId, String userId) {
        Match match = getMatchIfCreator(matchId, userId);

        if (match.getStatus() == MatchStatus.LIVE) {
            long elapsed = java.time.Duration.between(match.getStartTime(), LocalDateTime.now()).getSeconds();
            match.setRemainingDuration(Math.max(0, match.getRemainingDuration() - (int) elapsed));
        }

        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);
        return convertToDto(match);
    }
    public List<MatchDto> searchByMatchName(String matchName) {
        if (matchName == null || matchName.trim().isEmpty()) {
            throw new NotfoundException("Match name must not be empty");
        }

        // Remove spaces from search term
        String cleanedSearch = matchName.replaceAll("\\s+", "");

        // Build regex to match ignoring spaces (use \s* to allow spaces anywhere)
        String regex = cleanedSearch.chars()
                .mapToObj(c -> Pattern.quote(String.valueOf((char) c)) + "\\s*")
                .collect(Collectors.joining());

        List<Match> matches = matchRepository.findByMatchNameRegex("^" + regex + "$", "i");

        return matches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // Utility
    public Match getMatchIfCreator(String matchId, String userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotfoundException("Match not found"));
        if (!match.getCreatedBy().equals(userId)) {
            throw new NotfoundException("Only creator can perform this action");
        }
        return match;
    }

    public MatchDto convertToDto(Match match) {
        log.info("Converting Match to DTO");
        int remaining = match.getRemainingDuration()==null ?0:match.getRemainingDuration();
        if(remaining<0){
            remaining=0;
            match.setRemainingDuration(remaining);
            match.setStatus(MatchStatus.COMPLETED);
        }
        if (match.getStatus() == MatchStatus.LIVE && match.getStartTime() != null) {
            long elapsed = java.time.Duration.between(match.getStartTime(), LocalDateTime.now()).getSeconds();
            remaining = Math.max(0, remaining - (int) elapsed);
        }


        return MatchDto.builder()
                .id(match.getId())
                .matchName(match.getMatchName())
                .team1Name(match.getTeam1Name())
                .team2Name(match.getTeam2Name())
                .team1PhotoUrl(match.getTeam1PhotoUrl())
                .team2PhotoUrl(match.getTeam2PhotoUrl())
                .status(match.getStatus())
                .createdAt(match.getCreatedAt())
                .createdBy(match.getCreatedBy())
                .totalDuration(match.getTotalDuration())
                .remainingDuration(remaining)
                .Location(match.getLocation())
                .team1Score(match.getTeam1Score())
                .team2Score(match.getTeam2Score())
                .build();
    }

    public MatchDto updateTeamScore(String matchId,String teamName,Integer score) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new NotfoundException("Match not found"));
        if(teamName.equals(match.getTeam1Name())){
            match.setTeam1Score(match.getTeam1Score()+score);
        }
        if(teamName.equals(match.getTeam2Name())){
            match.setTeam2Score(match.getTeam2Score()+score);
        }
        return convertToDto(matchRepository.save(match));
    }

    public MatchDto setMatch(String setType, String matchId, String userId) {
        if(setType.equals("start")){
            return startMatch(matchId, userId);
        }
        else if(setType.equals("pause")){
            return pauseMatch(matchId, userId);
        }
        else if(setType.equals("resume")){
            return resumeMatch(matchId, userId);
        }
        else if(setType.equals("end")){
            return endMatch(matchId, userId);
        }
        else{
            return null;
        }
    }

    public List<MatchDto> getCreatedMatchesByUserId(String userId) {
        List<Match> createdMatches = matchRepository.findByCreatedBy(userId);
        List<MatchDto> matchDtos = new ArrayList<>();
        for (Match match : createdMatches) {
            matchDtos.add(convertToDto(match));
        }
        return matchDtos;
    }

    public LiveScorerCard getLiveScorerCard(String matchId,String createrId) {
        Match matche = getMatchIfCreator(matchId, createrId);
        MatchDto match = convertToDto(matche);
        if(match.getStatus()==MatchStatus.COMPLETED){
            throw new NotfoundException("Match has already been completed");
        }

        LiveScorerCard liveScorerCard = new LiveScorerCard();
        liveScorerCard.setMatchId(match.getId());
        liveScorerCard.setMatchName(match.getMatchName());
        liveScorerCard.setTeam1Name(match.getTeam1Name());
        liveScorerCard.setTeam2Name(match.getTeam2Name());
        liveScorerCard.setTeam1PhotoUrl(match.getTeam1PhotoUrl());
        liveScorerCard.setTeam2PhotoUrl(match.getTeam2PhotoUrl());
        liveScorerCard.setTeam1Score(match.getTeam1Score());
        liveScorerCard.setTeam2Score(match.getTeam2Score());
        liveScorerCard.setMatchStatus(match.getStatus());
        liveScorerCard.setLocation(match.getLocation());
        liveScorerCard.setCreatedBy(match.getCreatedBy());
        liveScorerCard.setRemainingTime(match.getRemainingDuration());
        List<PlayerResponse> team1Players = getTeamPlayersForMatch(matchId,match.getTeam1Name());
        List<PlayerResponse> team2Players = getTeamPlayersForMatch(matchId,match.getTeam2Name());
        liveScorerCard.setTeam1Players(team1Players);
        liveScorerCard.setTeam2Players(team2Players);
        return liveScorerCard;

    }
    public List<PlayerResponse> getTeamPlayersForMatch(String matchId, String teamName) {
        List<MatchStats> matchStats = matchStatsRepository.findByMatchIdAndTeamNameIgnoreCase(matchId,teamName);
        List<PlayerResponse> playerResponseList = new ArrayList<>();
        for(MatchStats stats : matchStats){
            PlayerResponse playerResponse = new PlayerResponse();
            playerResponse.setPlayerId(stats.getPlayerId());
            User user =userRepository.findById(stats.getPlayerId()).orElseThrow(()->new NotfoundException("user not found"));
            playerResponse.setPlayerName(user.getName());
            playerResponseList.add(playerResponse);
        }
        return playerResponseList;
    }
}
