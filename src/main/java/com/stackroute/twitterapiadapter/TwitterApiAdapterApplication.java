package com.stackroute.twitterapiadapter;

import com.stackroute.twitterapiadapter.adapter.TwitterAPIAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TwitterApiAdapterApplication {

	@Autowired
	TwitterAPIAdapter twitterAPIAdapter;

	public static void main(String[] args) {
		SpringApplication.run(TwitterApiAdapterApplication.class, args);
	}

}
