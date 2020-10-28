package com.example.TwitterCrawler.Repository;

import com.example.TwitterCrawler.Entities.Follows;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Set;

@RepositoryRestResource(collectionResourceRel = "following", path="following")
public interface FollowRepository extends Neo4jRepository<Follows, Long> {

	@Query("Foreach x in {follows} |" +
			"Merge (a : User {id: x.userA.id}) Merge (b: User {id: x.userB.id})" +
			"Merge (a)-[:FOLLOWS]->(b)")
	public void saveFollows(@Param("follows") Set<Follows> follows);
}
