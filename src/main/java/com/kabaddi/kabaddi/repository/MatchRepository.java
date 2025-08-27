package com.kabaddi.kabaddi.repository;

import com.kabaddi.kabaddi.entity.Match;
import com.kabaddi.kabaddi.util.MatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match,String> {
    List<Match> findByStatus(MatchStatus status);
    @Query("{ 'matchName': { $regex: ?0, $options: ?1 } }")
    List<Match> findByMatchNameRegex(String s, String i);

    List<Match> findByCreatedBy(String userId);
}
