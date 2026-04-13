package org.example.springredis.domain.ch4.user.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.user.entity.User;
import org.example.springredis.domain.ch4.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User save(String username) {
        return userRepository.save(User.builder().username(username).build());
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
