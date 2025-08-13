package com.kabaddi.kabaddi.repository;

import com.kabaddi.kabaddi.entity.MatchStats;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchStatsRepository extends MongoRepository<MatchStats,String>{

    void deleteByMatchId(String matchId);

    List<MatchStats> findByMatchIdAndTeamNameIgnoreCase(String matchId, String teamName);

    List<MatchStats> findByMatchId(String matchId);


    MatchStats findByMatchIdAndPlayerId(String matchId, String playerId);

    List<MatchStats> findByPlayerId(String playerId);
}
