package com.example.TwitterCrawler.Entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.social.twitter.api.TwitterProfile;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@NoArgsConstructor
@NodeEntity
@Setter
@Getter(AccessLevel.PUBLIC)
public class User {

	@Id
	private Long id;

	@Relationship(type = "FOLLOWS")
	private Set<User> follows = new HashSet<>();

	@Relationship(type="FOLLOWS", direction ="INCOMING")
	private Set<User> followers = new HashSet<>();

	private String name;
	private String username;
	private Date created_at;
	private String description;
	private String location;
	private String profile_image_url;
	private String url;
	private boolean verified;

	private float pageRank;
	private Float lastPageRank;

	private Integer followerCount;
	private Integer followsCount;

	private Integer previousRank;
	private Integer currentRank;

	private Boolean imported;
	private Long discoveredTime;
	private Integer discoveredRank;


	public User(TwitterProfile profile){
		this.name = profile.getName();
		this.username = profile.getScreenName();
		this.created_at = profile.getCreatedDate();
		this.description=profile.getDescription();
		this.location = profile.getLocation();
		this.profile_image_url = profile.getProfileImageUrl();
		this.url = profile.getUrl();
		this.verified = profile.isVerified();
		this.followerCount = profile.getFollowersCount();
		this.followsCount = profile.getFriendsCount();

	}

	public User(Long id, Set<User> follows, Set<User> followers){
		this.id = id;
		this.follows = follows;
		this.followers = followers;
	}

}
