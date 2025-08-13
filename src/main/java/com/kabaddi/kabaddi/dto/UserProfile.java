package com.kabaddi.kabaddi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfile {
    private String userId;
    private String name;
    private String username;
    private String password;
    private Integer raidPoints;
    private Integer defencePoints;
    private String url;
}
