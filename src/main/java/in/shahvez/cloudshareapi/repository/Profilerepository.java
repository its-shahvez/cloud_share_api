package in.shahvez.cloudshareapi.repository;

import in.shahvez.cloudshareapi.document.ProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface Profilerepository extends MongoRepository<ProfileDocument, String> {

    Optional<ProfileDocument>  findByEmail(String email);
    ProfileDocument findByClerkId(String clerkId);
    boolean  existsByClerkId(String clerkId);


}
