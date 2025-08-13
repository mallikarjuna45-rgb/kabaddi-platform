package com.kabaddi.kabaddi.controller;

import com.kabaddi.kabaddi.config.MatchWebSocketBroadcaster;
import com.kabaddi.kabaddi.dto.RequestUserDto;
import com.kabaddi.kabaddi.dto.SummaryCard;
import com.kabaddi.kabaddi.dto.UserDto;
import com.kabaddi.kabaddi.dto.UserProfile;
import com.kabaddi.kabaddi.service.UserService;
import com.kabaddi.kabaddi.util.MatchStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final MatchWebSocketBroadcaster webSocketBroadcaster;

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@Valid @ModelAttribute RequestUserDto requestUserDto){
        UserDto savedUserDto = userService.createUser(requestUserDto);
        return ResponseEntity.ok(savedUserDto);
    }
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String id, @Valid @ModelAttribute RequestUserDto requestUserDto){
        return ResponseEntity.ok(userService.updateUser(id,requestUserDto));
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteById(id); // assuming this throws exception if not found
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfile> getUserStats(@PathVariable String userId){
        return ResponseEntity.ok(userService.getProfile(userId));
    }
    @GetMapping("/test/ws/send")
    public ResponseEntity<String> testSendWebSocket() {
        SummaryCard liveMatch = new SummaryCard(
                "M001",
                "Championship Final",
                2,
                1,
                "https://example.com/teamA.png",
                "https://example.com/teamB.png",
                "Team A",
                "Team B",
                15, // remaining minutes
                "City Stadium",
                MatchStatus.LIVE
        );// create dummy summary
        webSocketBroadcaster.sendScoreUpdate("6899bb91bccd8b42c7c4977f", liveMatch);
        return ResponseEntity.ok("Test message sent");
    }

}
