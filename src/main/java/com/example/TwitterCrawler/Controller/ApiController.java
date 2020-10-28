package com.example.TwitterCrawler.Controller;


import com.example.TwitterCrawler.Entities.User;
import com.example.TwitterCrawler.Service.TwitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

	@Autowired
	private TwitterService service;

	@GetMapping("/v1/user/{username}")
	public ResponseEntity<User> DiscoverProfileByUsername(@PathVariable("username") String username) {
		return ResponseEntity.ok(service.discoverUserByUsername(username));
	}
}
