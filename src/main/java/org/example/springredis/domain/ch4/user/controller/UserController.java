package org.example.springredis.domain.ch4.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.user.entity.User;
import org.example.springredis.domain.ch4.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ch4/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> create(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.save(request.username()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    record CreateUserRequest(String username) {}
}
