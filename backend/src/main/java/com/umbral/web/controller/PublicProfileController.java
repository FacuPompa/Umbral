package com.umbral.web.controller;

import com.umbral.domain.dto.PublicUserProfileResponse;
import com.umbral.domain.service.PublicProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    public PublicProfileController(PublicProfileService publicProfileService) {
        this.publicProfileService = publicProfileService;
    }

    @GetMapping("/{handle}")
    public ResponseEntity<PublicUserProfileResponse> getProfile(@PathVariable String handle) {
        return ResponseEntity.ok(publicProfileService.getProfileByHandle(handle));
    }
}
