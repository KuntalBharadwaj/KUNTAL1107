package com.cg.cred_metric;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableScheduling
public class CredMetricApplication {

	public static void main(String[] args) {
		SpringApplication.run(CredMetricApplication.class, args);
		log.info("Cred Metric Application Started");
	}

}
