package com.example.TwitterCrawler.Repository;

import com.example.TwitterCrawler.Entities.User;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;


public interface UserRepository extends Neo4jRepository<User, Long> {


	@Query("Match (user:User) Where exists(user.pageRank) And Not exists(user.username)" +
			"With user"+
			"Order By user.pageRank Desc" +
			"Limit 1" +
			"Return user")
	public User findRankedUserToCrawl();


	@Query("Match (user:User)<-[r:FOLLOWS]-(:User)" +
			"Where Not exists(user.username) " +
			"With user, count(r) As weight" +
			"Order By weight DESC" +
			"Where weight >2"+
			"Limit 1" +
			"return user")
	public User findNextUserToCrawl();

	@Query("Match (user:User) Where exists(user.pageRank) And exists(user.username) And Not exists(user.lastPageRank)" +
			"With collect(user) As users" +
			"Foreach (u In users | Set u.lastPageRank = toFloat(u.pageRank))")
	public void setLastPageRank();

	@Query("Match (user:User) Where exists(user.pageRank) And exists(user.username) And exists(user.lastPageRank) And user.imported=true" +
			"With user" +
			"Order By user.pageRank DESC" +
			"With collect(user) As users" +
			"Unwind range(0,  (size(users)-1)) As index" +
			"With users[index] As user, index+1 As currentRank" +
			"With user, user.currentRank As previousRank, currentRank" +
			"With collect({user:user, previousRank:previousRank, currentRank:currentRank}) As results" +
			"Foreach(x In [y In results Where y.user.pageRank <> y.user.lastPageRank]" +
			"| Set x.user.currentRank = x.currentRank " +
			"Set x.user.previousRank = x.previousRank" +
			"set x.user.lastPageRank = x.user.pageRank)")
	public void updateUserCurrentRanking();


	@Query("Match (user:User) Where exists(user.username) And exists(user.pageRank) And user.imported=true" +
			"With user" +
			"Order By user.pageRank DESC" +
			"Skip {skip}" +
			"Limit {limit}" +
			"Return collect(user) As users")
	public Set<User> findRankedUsers(@Param("skip") int skip, @Param("limit") int limit);


	//public void updateDiscoveryChain();

}
