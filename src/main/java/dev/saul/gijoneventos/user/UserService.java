package dev.saul.gijoneventos.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

}