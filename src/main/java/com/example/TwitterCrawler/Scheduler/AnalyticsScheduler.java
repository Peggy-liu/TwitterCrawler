package com.example.TwitterCrawler.Scheduler;

import com.example.TwitterCrawler.Entities.User;
import com.example.TwitterCrawler.Service.TwitterService;
import com.example.TwitterCrawler.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Slf4j
public class AnalyticsScheduler {

	public static boolean reachLimit = false;
	@Value("${spring.data.neo4j.uri}")
	private String uri;
	@Autowired
	private UserService userService;
	@Autowired
	private TwitterService twitterService;
	private RestTemplate restTemplate = new RestTemplate();

	//schedule a page rank job every 5 mins
	@Scheduled(fixedRate = 300000)
	public void schedulePageRankJob() {
		// Schedule a PageRank job for the Twitter follower graph in Neo4j using HTTP REST API
		String relativePath = "%s/service/mazerunner/analysis/pagerank/FOLLOWS";
		String analysisEndpoint = String.format(relativePath, uri);

		restTemplate.getForEntity(analysisEndpoint, null);
		log.info(String.format("PageRank scheduled on follows graph at %s", new Date()));
	}

	//schedule a user discovery every one min
	@Scheduled(fixedRate = 60000)
	public void scheduleUserDiscovery() {
		if (!reachLimit) {
			User user = userService.findRankerUserToCrawl();
			if (user == null) {
				user = userService.findNextUserToCrawl();
			}
			if (user != null) {
				//schedule crawling for this user
				twitterService.discoverUserByProfileId(user.getId());
			}
		}
		else {
			//reschedule again next time
			reachLimit = false;
		}
		userService.updateRanking();

	}


}
