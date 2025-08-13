package com.kabaddi.kabaddi.service;

import com.kabaddi.kabaddi.dto.RequestUserDto;
import com.kabaddi.kabaddi.dto.UserDto;
import com.kabaddi.kabaddi.dto.UserProfile;
import com.kabaddi.kabaddi.entity.User;
import com.kabaddi.kabaddi.exception.FileUploadException;
import com.kabaddi.kabaddi.exception.NotfoundException;
import com.kabaddi.kabaddi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final ImageUploadService imageUploadService;

    private final MatchStatsService matchStatsService;

    public UserDto createUser(RequestUserDto requestUserDto) {
        try {
            String imageUrl = null;
            if (requestUserDto.getImage() != null && !requestUserDto.getImage().isEmpty()) {
                imageUrl = imageUploadService.uploadImage(requestUserDto.getImage());
            }
            if(userRepository.existsByUsername(requestUserDto.getUsername())){
                throw new NotfoundException("Username is already in use");
            }

            User user = User.builder()
                    .name(requestUserDto.getName())
                    .username(requestUserDto.getUsername())
                    .password(requestUserDto.getPassword())
                    .url(imageUrl)
                    .build();

            userRepository.save(user);
            return convertToUserDto(user);

        } catch (IOException e) {
            throw new FileUploadException("Failed to upload image");
        }
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        users.forEach(user -> userDtos.add(convertToUserDto(user)));
        return userDtos;
    }

    public UserDto updateUser(String id, RequestUserDto requestUserDto) {
        try {
            // Find existing user by id or throw if not found
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new NotfoundException("User not found with id: " + id));

            // Check if username is changing and already used by someone else
            if (!existingUser.getUsername().equals(requestUserDto.getUsername()) &&
                    userRepository.existsByUsername(requestUserDto.getUsername())) {
                throw new NotfoundException("Username is already in use");
            }

            String imageUrl = existingUser.getUrl(); // keep old image URL if not updated
            if (requestUserDto.getImage() != null && !requestUserDto.getImage().isEmpty()) {
                imageUrl = imageUploadService.uploadImage(requestUserDto.getImage());
            }

            // Update fields
            existingUser.setName(requestUserDto.getName());
            existingUser.setUsername(requestUserDto.getUsername());
            existingUser.setPassword(requestUserDto.getPassword());
            existingUser.setUrl(imageUrl);

            userRepository.save(existingUser);

            return convertToUserDto(existingUser);

        } catch (IOException e) {
            throw new FileUploadException("Failed to upload image");
        }
    }

    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotfoundException("User not found with id: " + id));
        return convertToUserDto(user);
    }
    public UserProfile getProfile(String userId) {
        UserDto user = getUserById(userId);
        return UserProfile.builder()
                .userId(user.getId())
                .name(user.getName())
                .password(user.getPassword())
                .url(user.getUrl())
                .raidPoints(matchStatsService.getUserRaidPoints(userId))
                .defencePoints(matchStatsService.getUserDefenceoints(userId))
                .build();
    }

























    public UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .url(user.getUrl())
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
    public User convertToUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .url(userDto.getUrl())
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .build();
    }

    public void deleteById(String id) {
        if (!userRepository.existsById(id)) {
            throw new NotfoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }



}
