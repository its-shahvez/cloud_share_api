package in.shahvez.cloudshareapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
@SpringBootApplication
@EnableMongoAuditing
public class CloudshareapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudshareapiApplication.class, args);
	}

}
