package com.stackroute.twitterapiadapter;

import com.stackroute.twitterapiadapter.adapter.TwitterAPIAdapter;
import com.stackroute.twitterapiadapter.exceptions.EmptyQueryParamsException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class TwitterApiAdapterApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwitterApiAdapterApplication.class, args);
		try {
			TwitterAPIAdapter twitterAPIAdapter = new TwitterAPIAdapter();
			twitterAPIAdapter.addQueryParam("CGI");
			twitterAPIAdapter.addQueryParam("Amazon");
			twitterAPIAdapter.getTweets().subscribe(activity -> log.debug(activity.toString()));
			twitterAPIAdapter.startNewsStream();
		} catch (SchedulerException e) {
			e.printStackTrace();
		} catch (EmptyQueryParamsException e) {
			e.printStackTrace();
		}
	}

}
