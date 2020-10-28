package com.example.TwitterCrawler.Service;


import com.example.TwitterCrawler.Entities.FollowersLimitExceedException;
import com.example.TwitterCrawler.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TwitterServiceImpl implements TwitterService {

	@Autowired
	private Twitter twitter;

	@Autowired
	private UserService userService;

	@Override
	public User discoverUserByProfileId(long id) {
		User user = Optional.of(twitter.userOperations().getUserProfile(id))
				.map(User::new)
				.get();
		try {
			userService.scheduleUserForCrawling(user);
		} catch (FollowersLimitExceedException e) {
			User nextUser = userService.findRankerUserToCrawl();
			if (nextUser == null) {
				nextUser = userService.findNextUserToCrawl();
			}
			if (nextUser != null) {
				discoverUserByProfileId(nextUser.getId());
			}
		}
		return user;
	}

	@Override
	public User discoverUserByUsername(String username) {

		User user = Optional.of(twitter.userOperations().getUserProfile(username))
				.map(User::new)
				.get();
		try {
			userService.scheduleUserForCrawling(user);
		} catch (FollowersLimitExceedException e) {
			User nextUser = userService.findRankerUserToCrawl();
			if (nextUser == null) {
				nextUser = userService.findNextUserToCrawl();
			}
			if (nextUser != null) {
				discoverUserByProfileId(nextUser.getId());
			}
		}
		return user;
	}

}
