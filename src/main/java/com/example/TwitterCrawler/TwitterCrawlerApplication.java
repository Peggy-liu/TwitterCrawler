package com.example.TwitterCrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TwitterCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwitterCrawlerApplication.class, args);
	}



}
