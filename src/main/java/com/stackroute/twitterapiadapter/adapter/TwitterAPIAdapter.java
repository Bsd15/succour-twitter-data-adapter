package com.stackroute.twitterapiadapter.adapter;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TwitterAPIAdapter {
    private String consumerKey;
    private String consumerSecretKey;
    private String accessToken;
    private String accessTokenSecret;
    private List<String> queryParams;
    private final Twitter twitter;
    private SchedulerFactory schedulerFactory;
    private Scheduler scheduler;
    private JobDetail newsFetchJob;
    private Trigger trigger;
    private PublishSubject<Activity> articlePublishSubject;
    private static final int schedulerInterval = 180;

    public TwitterAPIAdapter(@Value("${twitter.consumerAPIKey}") String consumerKey,@Value("${twitter.consumerAPISecretKey}") String consumerSecretKey,@Value("${twitter.accessToken}") String accessToken,@Value("${twitter.accessTokenSecret}") String accessTokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecretKey = consumerSecretKey;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        twitter = new TwitterTemplate(consumerKey, consumerSecretKey, accessToken, accessTokenSecret);
        log.debug(twitter.toString());
        SearchResults results = twitter.searchOperations().search("#spring");
        results.getTweets().forEach(tweet -> log.debug(tweet.getText()));
    }

    public List<String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<String> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Add query to the queryParams list.
     *
     * @param queryParam String to be added to query params list.
     */
    public void addQueryParam(String queryParam) {
        /*
         * Check if queryParams list is empty and if it's empty then
         * initialize it.
         * */
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<>();
        }
        this.queryParams.add(queryParam);
    }
}
