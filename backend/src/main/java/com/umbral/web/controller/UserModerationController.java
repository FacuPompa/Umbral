package com.umbral.web.controller;

import com.umbral.domain.dto.*;
import com.umbral.domain.service.UserModerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moderation/users")
@PreAuthorize("hasRole('MODERATOR')")
public class UserModerationController {
    private final UserModerationService service;

    public UserModerationController(UserModerationService service) { this.service = service; }

    @GetMapping
    public ModeratedUserPageResponse search(@RequestParam(defaultValue = "") @Size(max = 30) String query,
                                            @RequestParam(required = false) Boolean suspended,
                                            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int page,
                                            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return service.search(query, suspended, page, size);
    }

    @PostMapping("/{userId}/suspend")
    public ModeratedUserResponse suspend(@PathVariable Long userId, @Valid @RequestBody SuspendUserRequest request) {
        return service.suspend(userId, request);
    }

    @PostMapping("/{userId}/reactivate")
    public ModeratedUserResponse reactivate(@PathVariable Long userId) {
        return service.reactivate(userId);
    }
}
