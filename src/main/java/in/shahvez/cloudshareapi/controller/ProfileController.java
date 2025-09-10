package in.shahvez.cloudshareapi.controller;

import in.shahvez.cloudshareapi.dto.ProfileDTO;
import in.shahvez.cloudshareapi.service.ProfileService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDTO profileDTO){
        HttpStatus status = profileService.existsByClerkId(profileDTO.getClerkId()) ?
                HttpStatus.OK:HttpStatus.CREATED;

        ProfileDTO savedProfile =  profileService.createdProfile(profileDTO);
        return ResponseEntity.status(status).body(savedProfile);
    }
}