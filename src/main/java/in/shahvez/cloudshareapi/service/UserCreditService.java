package in.shahvez.cloudshareapi.service;

import in.shahvez.cloudshareapi.document.CreditDetails;
import in.shahvez.cloudshareapi.repository.CreditDetailsRepository;
import in.shahvez.cloudshareapi.repository.Profilerepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditService {
    private final CreditDetailsRepository creditDetailsRepository;
    private final Profilerepository profilerepository;
    private final ProfileService profileService;

    public CreditDetails creditInitialDetails (String clerkId){
       CreditDetails creditDetails = CreditDetails.builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("BASIC")
                .build();

        return creditDetailsRepository.save(creditDetails);
    }
    public CreditDetails getCrediteDetails(String clerkId){
       return creditDetailsRepository.findByClerkId(clerkId)
                .orElseGet(() -> creditInitialDetails(clerkId));

    }
    public CreditDetails getCrediteDetails(){
        String clerkId = profileService.getCurrentProfile().getClerkId();
       return  getCrediteDetails(clerkId);
    }

    public Boolean hasEnoughCredits(int requiredCredits){
       CreditDetails creditDetails = getCrediteDetails();
        return creditDetails.getCredits() >= requiredCredits;

    }

    public CreditDetails consumeCredits(){
       CreditDetails creditDetails = getCrediteDetails();
       if(creditDetails.getCredits() <= 0){
           return null;
       }else{
           creditDetails.setCredits(creditDetails.getCredits()-1);
         return   creditDetailsRepository.save(creditDetails);
       }
    }
    public CreditDetails addCredits(String clerkId, Integer creditsToAdd, String plan){
      CreditDetails creditDetails =  creditDetailsRepository.findByClerkId(clerkId)
                .orElseGet(() -> creditInitialDetails(clerkId));

      creditDetails.setCredits(creditDetails.getCredits()+ creditsToAdd);
      creditDetails.setPlan(plan);
      return creditDetailsRepository.save(creditDetails);
    }
}
