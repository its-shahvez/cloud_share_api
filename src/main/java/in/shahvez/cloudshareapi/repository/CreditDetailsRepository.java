package in.shahvez.cloudshareapi.repository;

import in.shahvez.cloudshareapi.document.CreditDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CreditDetailsRepository  extends MongoRepository<CreditDetails,String> {
    Optional<CreditDetails> findByClerkId(String clerkId);
}
