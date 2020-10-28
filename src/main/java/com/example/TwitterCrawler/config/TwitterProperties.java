package com.example.TwitterCrawler.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "twitter")
@Getter
@Setter
@Component
public class TwitterProperties {
	String consumerKey;
	String consumerSecret;
	String apiToken;
	String apiSecretToken;

}
