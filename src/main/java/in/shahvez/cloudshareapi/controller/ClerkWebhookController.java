package in.shahvez.cloudshareapi.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.shahvez.cloudshareapi.dto.ProfileDTO;
import in.shahvez.cloudshareapi.service.UserCreditService;
import in.shahvez.cloudshareapi.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class ClerkWebhookController {

    private final ProfileService profileService;
    private final UserCreditService userCreditService;

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerkWebhook(@RequestHeader("svix-id") String  svixId,
                                                @RequestHeader("svix-timestamp") String svixTimestamp,
                                                @RequestHeader("svix-signature") String svixSignature,
                                                @RequestBody String payload){

        try{
            boolean isValid = verifyWebhookSignature(svixId, svixTimestamp,svixSignature,payload);
            if(!isValid){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Inavalid webhook signature");

            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            String eventype =  rootNode.path("type").asText();

            switch (eventype){
                case "user.created":
                    handleUserCreated(rootNode.path("data"));
                    break;
                case "user.updated":
                    handleUserUpdate(rootNode.path("data"));
                    break;
                case "user.deleted":
                    handleUserDeleted(rootNode.path("data"));
                    break;
            }
            return ResponseEntity.ok().build();
        }catch (Exception e){
            throw new ResponseStatusException((HttpStatus.UNAUTHORIZED), e.getMessage());

        }
    }

    private void handleUserDeleted(JsonNode data) {
        String clerkId =data.path("id").asText();
        profileService.deleteProfile(clerkId);
    }

    private void handleUserUpdate(JsonNode data) {
        String clerkId =  data.path("id").asText();
        String email ="";
        JsonNode emailAddresses = data.path("email_addresses");
        if(emailAddresses.isArray() && emailAddresses.size() >0){
            email = emailAddresses.get(0).path("email_address").asText();
        }
        String firstName=data.path("first_name").asText("");
        String lastName = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        ProfileDTO updatedProfile =  ProfileDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .photoUrl(photoUrl)
                .build();

        updatedProfile = profileService.updateProfile(updatedProfile);
        if(updatedProfile == null){
            handleUserCreated(data);
        }
    }

    private void handleUserCreated(JsonNode data) {
        String clerkId =  data.path("id").asText();
        String email = null;
        JsonNode emailAddresses = data.path("email_addresses");
        if(emailAddresses.isArray() && emailAddresses.size() >0){
            String extractedEmail = emailAddresses.get(0).path("email_address").asText();
            if (extractedEmail != null && !extractedEmail.trim().isEmpty()) {
                email = extractedEmail;
            }
        }
        String firstName=data.path("first_name").asText("");
        String lastName = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        ProfileDTO newProfile =  ProfileDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .photoUrl(photoUrl)
                .build();

        profileService.createdProfile(newProfile);
        userCreditService.creditInitialDetails(clerkId);


    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        //validate signature
        return true;
    }
}
