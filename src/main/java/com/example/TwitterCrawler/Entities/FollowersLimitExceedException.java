package com.example.TwitterCrawler.Entities;

public class FollowersLimitExceedException extends Exception {
	public FollowersLimitExceedException(String message) {
		super(message);
	}
}
