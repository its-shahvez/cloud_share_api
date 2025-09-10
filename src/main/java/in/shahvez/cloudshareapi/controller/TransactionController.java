package in.shahvez.cloudshareapi.controller;


import in.shahvez.cloudshareapi.document.PaymentTransaction;
import in.shahvez.cloudshareapi.document.ProfileDocument;
import in.shahvez.cloudshareapi.repository.PaymentTransactionRepository;
import in.shahvez.cloudshareapi.service.ProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")

@RequiredArgsConstructor
public class TransactionController {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getUserTransaction(){
       ProfileDocument currentProfile = profileService.getCurrentProfile();
      String clerkid =  currentProfile.getClerkId();

     List<PaymentTransaction> transactions = paymentTransactionRepository.findByClerkIdAndStatusOrderByTransactionDateDesc(clerkid, "SUCCESS");
     return ResponseEntity.ok(transactions);

    }
}
