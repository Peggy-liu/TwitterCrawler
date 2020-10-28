package com.example.TwitterCrawler.Service;

import com.example.TwitterCrawler.Entities.Follows;
import com.example.TwitterCrawler.Entities.User;
import com.example.TwitterCrawler.Repository.FollowRepository;
import com.example.TwitterCrawler.Repository.UserRepository;
import com.example.TwitterCrawler.Scheduler.AnalyticsScheduler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpIllegalStateException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.RateLimitExceededException;
import org.springframework.social.twitter.api.CursoredList;
import org.springframework.social.twitter.api.Twitter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TwitterUserDiscoveryListener {

	private Twitter twitter;
	private AmqpTemplate template;
	private ObjectMapper objectMapper;
	private FollowRepository followRepository;
	private UserRepository userRepository;


	@Autowired
	public TwitterUserDiscoveryListener(Twitter twitter, AmqpTemplate template, FollowRepository followRepository, UserRepository userRepository) {
		this.twitter = twitter;
		this.template = template;
		this.objectMapper = new ObjectMapper();
		this.followRepository = followRepository;
		this.userRepository = userRepository;
	}


	@RabbitListener(queues = "twitter_follows")
	public void onFollowsChannel(String message) throws JsonProcessingException, InterruptedException {
		User user = objectMapper.readValue(message, User.class);
		if (user != null) {
			try {
				CursoredList<Long> friends = twitter.friendOperations().getFriendIds(user.getId());
				saveFollows(user, friends);
				//if there is next cursor, get the next page of profiles
				while (friends.hasNext()) {
					friends = twitter.friendOperations().getFriendIdsInCursor(user.getId(), friends.getNextCursor());
					saveFollows(user, friends);
				}
				//after importing friends of this user, schedule a job to import its followers
				template.convertAndSend("twitter_followers", objectMapper.writeValueAsString(user));
				log.info(String.format("%d friends are imported for user %s", user.getFollowsCount(), user.getUsername()));
			} catch (RateLimitExceededException exception) {
				AnalyticsScheduler.reachLimit = true;
				Thread.sleep(40000L);
				log.info(String.format("Rate limit exceeded while importing friends for user %s", user.getUsername()));
				throw new AmqpIllegalStateException(exception.getMessage());
			} catch (Exception exp) {
				log.error(exp.getMessage());
			}


		}
	}

	private void saveFollows(User user, CursoredList<Long> friends) {
		List<User> follows = friends.stream().map(id -> new User(id, null, Collections.singleton(user)))
				.collect(Collectors.toList());

		int batch_size = 400;
		int counter = 0;
		//max retry 3 times
		int retry = 0;

		while ((batch_size * counter) < follows.size()) {
			List<User> sublist;
			if (batch_size * counter + batch_size <= follows.size()) {
				sublist = follows.subList(batch_size * counter, batch_size * counter + batch_size);
			}
			else {
				sublist = follows.subList(batch_size * counter, follows.size());
			}
			Set<Follows> followsSet = sublist.stream().map(friend -> new Follows(user, friend)).collect(Collectors.toSet());
			//catch all exception when saving profiles to the neo4j database
			try {
				followRepository.saveFollows(followsSet);
				counter++;
			} catch (Exception e) {
				if (retry <= 3) {
					retry++;
				}
				else {
					throw e;
				}
			}

		}

	}

	@RabbitListener(queues = "twitter_followers")
	public void onFollowersChannel(String message) {
		User user = null;
		try {
			user = objectMapper.readValue(message, User.class);
			if (user != null) {
				CursoredList<Long> followers = twitter.friendOperations().getFollowerIds(user.getId());
				saveFollowers(user, followers);
				while (followers.hasNext()) {
					followers = twitter.friendOperations().getFollowerIdsInCursor(user.getId(), followers.getNextCursor());
					saveFollowers(user, followers);
				}
				//update user information
				if (user.getImported() == null || !user.getImported()) {
					user.setImported(true);
					userRepository.save(user);
				}
				//TODO: schedule user for ranking job
			}
		} catch (RateLimitExceededException exception) {
			AnalyticsScheduler.reachLimit = true;
			try {
				Thread.sleep(40000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info(String.format("Rate limit is exceeded when importing friends for user %s", user.getUsername()));
			throw new AmqpIllegalStateException(exception.getMessage());
		} catch (Exception exception) {
			log.info(exception.getMessage());
		}

	}

	private void saveFollowers(User user, CursoredList<Long> followers) {
		List<User> followerList = followers.stream().map(id -> new User(id, Collections.singleton(user), null))
				.collect(Collectors.toList());

		int batch_size = 400;
		int batch_num = 0;
		int retry = 0;

		while ((batch_size * batch_num) < followerList.size()) {
			List<User> sublist;
			if (batch_num * batch_size + batch_size <= followerList.size()) {
				sublist = followerList.subList(batch_num * batch_size, batch_num * batch_size + batch_size);
			}
			else {
				sublist = followerList.subList(batch_num * batch_size, followerList.size());
			}
			try {
				Set<Follows> follows = sublist.stream().map(follower -> new Follows(follower, user)).collect(Collectors.toSet());
				followRepository.saveFollows(follows);
				batch_num++;
			} catch (Exception e) {
				if (retry <= 3) {
					retry++;
				}
				else {
					throw e;
				}
			}
		}
	}
}
