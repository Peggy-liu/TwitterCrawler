package com.example.TwitterCrawler.Service;


import com.example.TwitterCrawler.Entities.FollowersLimitExceedException;
import com.example.TwitterCrawler.Entities.User;
import com.example.TwitterCrawler.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class UserService {
	public final static int MAX_FOLLOWS = 50000;
	public final static int MAX_FOLLOWERS = 50000;
	public final static String FOLLOW_QUEUE = "twitter_follows";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AmqpTemplate amqpTemplate;

	private ObjectMapper mapper = new ObjectMapper();

	public void scheduleUserForCrawling(User user) throws FollowersLimitExceedException {
		if(userRepository.findById(user.getId()).isEmpty()){
			userRepository.save(user);

		}
			//check if the amount of follower and friends are manageable
			if(user.getFollowerCount() < MAX_FOLLOWERS && user.getFollowsCount() < MAX_FOLLOWS){

				try {
					//send a message to schedule a job for crawling
					amqpTemplate.convertAndSend(FOLLOW_QUEUE, mapper.writeValueAsString(user));
					log.info(String.format("Scheduled user %s for crawling at time %d", user.getUsername(), new Date().getTime()));
					user.setDiscoveredTime(new Date().getTime());
					// TODO: Update the discovery chain
					//userRepository.updateDiscoveryChain();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
			else{
				throw new FollowersLimitExceedException("this user has too many friends and followers.");
			}


	}

	public User findRankerUserToCrawl(){
		return userRepository.findRankedUserToCrawl();
	}

	public User findNextUserToCrawl(){
		return userRepository.findNextUserToCrawl();
	}

	public void updateRanking(){
		userRepository.updateUserCurrentRanking();
	}
}
