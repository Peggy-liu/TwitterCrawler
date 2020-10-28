package com.example.TwitterCrawler.Entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type="FOLLOWS")
@NoArgsConstructor
@Setter
@Getter
public class Follows {

	@Id @GeneratedValue
	private Long relationshipId;

	@StartNode
	private User userA;

	@EndNode
	private User userB;

	public Follows(User userA, User userB){
		this.userA = userA;
		this.userB = userB;
	}
}
