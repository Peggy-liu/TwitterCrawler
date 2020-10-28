package com.example.TwitterCrawler.Service;


import com.example.TwitterCrawler.Entities.User;

public interface TwitterService {

	public User discoverUserByProfileId(long id);
	public User discoverUserByUsername(String username);
}
