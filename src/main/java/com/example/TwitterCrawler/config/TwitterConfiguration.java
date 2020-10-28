package com.example.TwitterCrawler.config;


import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;


@Configuration
public class TwitterConfiguration {

	@Autowired
	private TwitterProperties property;

	@Bean
	public Twitter twitter(){
		return new TwitterTemplate(property.consumerKey, property.consumerSecret, property.apiToken, property.apiSecretToken);
	}

	@Bean
	public Queue follow_queue(){
		return new Queue("twitter_follows");
	}

	@Bean
	public Queue follower_queue(){
		return new Queue("twitter_followers");
	}

}
