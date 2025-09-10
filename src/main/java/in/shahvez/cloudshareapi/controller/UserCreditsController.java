package in.shahvez.cloudshareapi.controller;

import in.shahvez.cloudshareapi.document.CreditDetails;
import in.shahvez.cloudshareapi.dto.UserCreditDto;
import in.shahvez.cloudshareapi.service.UserCreditService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserCreditsController {

    private final UserCreditService userCreditService;

    @GetMapping("/credits")
    public ResponseEntity<?> getUserCredits(){
       CreditDetails creditDetails =  userCreditService.getCrediteDetails();
      UserCreditDto response =   UserCreditDto.builder()
                .credits(creditDetails.getCredits())
                .plan(creditDetails.getPlan())
                .build();

      return  ResponseEntity.ok(response);
    }
}
