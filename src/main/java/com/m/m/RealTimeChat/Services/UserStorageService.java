package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import com.m.m.RealTimeChat.Models.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("userStorage")
public class UserStorageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
private final OnlineUserService onlineUserService;

    public UserStorageService(UserRepository userRepository, PasswordEncoder passwordEncoder, OnlineUserService onlineUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.onlineUserService = onlineUserService;
    }

    @Transactional
    public void saveUserToStorage(User user) {
        if (user != null) {
            if(user.getProfilePic() == null) {
                user.setProfilePic("ProfilePic/defaultPic.jpg");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            if (user.getUserName().equals("Admin")) {
                user.setRoles("ADMIN");
            } else {
                user.setRoles("user");
            }
            userRepository.save(user);
        }
    }

    public List<String> getOtherNicknamesList(Authentication auth) {
        return userRepository.findAll().stream().filter(user -> !user.getUserName().equals(auth.getName())).map(User::getNickname).toList();

    }

    public List<String> getAllNicknames() {
        return userRepository.findAllNicknames();
    }

    public boolean removeUserFromStorage(Authentication auth) {
        User userToDelete = userRepository.findUserByUserName(auth.getName()).orElseThrow(() -> new UsernameNotFoundException("User doesn't exist"));
        onlineUserService.removeOnlineUser(userRepository.findUserByUserName(auth.getName()).get().getNickname());
        userRepository.delete(userToDelete);
        return userRepository.findUserByUserName(auth.getName()).isEmpty();
    }

    public void banUser(String user, LocalDateTime banExp) {

        User banndedUser = getUserByNickname(user);
        banndedUser.setNonBanned(false);
        banndedUser.setBanExpiration(banExp);
        userRepository.save(banndedUser);

    }

    public void unBanUser(String user) {
        User allowedUser = getUserByNickname(user);
        allowedUser.setNonBanned(true);
        allowedUser.setBanExpiration(null);
        userRepository.save(allowedUser);
    }

    public List<User> getBannedUsers() {
        return userRepository.findAllBannedUsers();
    }

    public List<String> getBannedNicknames() {
        return userRepository.findAllBannedUsers().stream().map(User::getNickname).toList();
    }


    public User getUser(String name) {
        return userRepository.findUserByUserName(name).orElseThrow(() -> new UsernameNotFoundException("Username doesn't exist"));
    }

    public User getUserByNickname(String nickname) {
        return userRepository.findAll().stream().filter(user -> user.getNickname().equals(nickname)).toList().get(0);
    }


    public void updateUserInfo(Authentication auth,String profilePicPath, String newPassword, String newNickname) {
        User user = getUser(auth.getName());
        if (!profilePicPath.isEmpty()) {
            user.setProfilePic(profilePicPath);
        }
        if (!(auth instanceof OAuth2AuthenticationToken) && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        if (!newNickname.isEmpty()) {
            user.setNickname(newNickname);
        }
        userRepository.save(user);

    }

    public boolean confirmActualPassword(Authentication auth, String password) {
        return auth instanceof OAuth2AuthenticationToken ||  passwordEncoder.matches(password, getUser(auth.getName()).getPassword());
    }

    public boolean isNewPassNotDifferent(String newPassword, String actualPassword) {
        return newPassword.equals(actualPassword);
    }

    public boolean validateRequestUser(String requestUser, String sender, String sendTo) {
        String nickname = getUser(requestUser).getNickname();
        return nickname.equals(sender) || nickname.equals(sendTo);
    }

    public Optional<User> findUser(String email) {
        return userRepository.findUserByMail(email);
    }
}


