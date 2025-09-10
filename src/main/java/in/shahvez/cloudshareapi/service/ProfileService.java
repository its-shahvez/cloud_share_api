package in.shahvez.cloudshareapi.service;


import in.shahvez.cloudshareapi.document.ProfileDocument;
import in.shahvez.cloudshareapi.dto.ProfileDTO;
import in.shahvez.cloudshareapi.repository.Profilerepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor

public class ProfileService {


    private  final Profilerepository profilerepository;

    public ProfileDTO createdProfile(ProfileDTO profileDTO){

        if(profilerepository.existsByClerkId(profileDTO.getClerkId())){
            return  updateProfile(profileDTO);
        }


        ProfileDocument profile =   ProfileDocument.builder()
                .clerkId(profileDTO.getClerkId())
                .email(profileDTO.getEmail())
                .firstName(profileDTO.getFirstName())
                .lastName(profileDTO.getLastName())
                .photoUrl(profileDTO.getPhotoUrl())
                .credits(5)
                .createdAt(Instant.now())
                .build();
        try {
            System.out.println("Attempting to save profile: " + profile.toString());
            profile = profilerepository.save(profile);
            System.out.println("Profile saved successfully with ID: " + profile.getId());
        } catch (Exception e) {
            System.out.println("!!!!!!!!!! DATABASE SAVE FAILED !!!!!!!!!!");
            e.printStackTrace();
            return null;
        }



        return ProfileDTO.builder()
                .id(profile.getId())
                .clerkId(profile.getClerkId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .photoUrl(profile.getPhotoUrl())
                .credits(profile.getCredits())
                .createdAt(profile.getCreatedAt())
                .build();


    }
    public  ProfileDTO updateProfile(ProfileDTO profileDTO) {
       ProfileDocument existingprofile = profilerepository.findByClerkId(profileDTO.getClerkId());

       if(existingprofile != null){
           //update fields if provided
           if(profileDTO.getEmail() != null && !profileDTO.getEmail().isEmpty()){
               existingprofile.setEmail(profileDTO.getEmail());
           }
           if(profileDTO.getFirstName() != null && !profileDTO.getFirstName().isEmpty()){
               existingprofile.setFirstName(profileDTO.getFirstName());
           }
           if(profileDTO.getLastName() != null && !profileDTO.getLastName().isEmpty()){
               existingprofile.setLastName(profileDTO.getLastName());
           }
           if(profileDTO.getPhotoUrl() != null && !profileDTO.getPhotoUrl().isEmpty()){
               existingprofile.setPhotoUrl(profileDTO.getPhotoUrl());
           }
           profilerepository.save(existingprofile);

            return ProfileDTO.builder()
                   .id(existingprofile.getId())
                   .email(existingprofile.getEmail())
                   .clerkId(existingprofile.getClerkId())
                   .firstName(existingprofile.getFirstName())
                   .lastName(existingprofile.getLastName())
                   .credits(existingprofile.getCredits())
                   .createdAt(existingprofile.getCreatedAt())
                   .photoUrl(existingprofile.getPhotoUrl())
                   .build();

       }
        return null;
    }
     public  boolean existsByClerkId(String clerkId){
        return  profilerepository.existsByClerkId(clerkId);
     }

     public  void deleteProfile(String clerkId){
      ProfileDocument existingProfile =  profilerepository.findByClerkId(clerkId);
      if(existingProfile != null){
          profilerepository.delete(existingProfile);
      }
    }
        public ProfileDocument   getCurrentProfile(){
            if (SecurityContextHolder.getContext().getAuthentication()==null) {
                throw new UsernameNotFoundException("User not Authenticated");

            }
         String clerkId =SecurityContextHolder.getContext().getAuthentication().getName();

           return  profilerepository.findByClerkId(clerkId);
           }
}
