package com.example.TwitterCrawler.config;


import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;


@EnableNeo4jRepositories(basePackages = "com.example.TwitterCrawler.Repository",
							sessionFactoryRef = "getSessionFactory")
@Configuration
public class Neo4jConfiguration {

	@Value("${spring.data.neo4j.uri}")
	private String uri;

	@Value("${neo4j.username}")
	private String user;

	@Value("${neo4j.password")
	private String password;


	/*
	the default name of session factory bean is "sessionFactory" .
	you can change this name in "@EnableNeo4jRepositories" .
	 */
	@Bean
	org.neo4j.ogm.config.Configuration getConfiguration(){
		//IMPORTANT: Must set credentials as system property or else database connection fails
//		System.setProperty("username", user);
//		System.setProperty("password", password);
		System.setProperty("username", "neo4j");
		System.setProperty("password", "admin");
		return new org.neo4j.ogm.config.Configuration.Builder().uri(uri).credentials(System.getProperty("username"),System.getProperty("password")).build();
	}

	@Bean
	public SessionFactory getSessionFactory(){
		return  new SessionFactory(getConfiguration(),"com.example.TwitterCrawler.Entities");
	}

}
