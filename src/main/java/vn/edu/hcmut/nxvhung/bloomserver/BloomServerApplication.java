package vn.edu.hcmut.nxvhung.bloomserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJms
@EnableScheduling
public class BloomServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BloomServerApplication.class, args);
	}

}
