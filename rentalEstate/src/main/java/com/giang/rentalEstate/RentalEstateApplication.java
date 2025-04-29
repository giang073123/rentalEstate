package com.giang.rentalEstate;

import com.giang.rentalEstate.service.RentalRequestService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RentalEstateApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalEstateApplication.class, args);
	}
//	@Bean
//	public ApplicationRunner dataFixer(RentalRequestService rentalRequestService) {
//		return args -> {
//			rentalRequestService.fixInconsistentData();
//		};
//	}

}
