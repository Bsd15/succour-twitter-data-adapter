package com.stackroute.twitterapiadapter.adapter;

import com.ibm.common.activitystreams.Activity;
import com.stackroute.twitterapiadapter.exceptions.EmptyQueryParamsException;
import com.stackroute.twitterapiadapter.service.TwitterFetchService;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

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
    private JobDetail tweetsFetchJob;
    private Trigger trigger;
    private PublishSubject<Activity> tweetsPublishSubject;
    private static final int schedulerInterval = 180;

    public TwitterAPIAdapter(@Value("${twitter.consumerAPIKey}") String consumerKey, @Value("${twitter.consumerAPISecretKey}") String consumerSecretKey, @Value("${twitter.accessToken}") String accessToken, @Value("${twitter.accessTokenSecret}") String accessTokenSecret) throws SchedulerException {
        this.consumerKey = consumerKey;
        this.consumerSecretKey = consumerSecretKey;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        twitter = new TwitterTemplate(consumerKey, consumerSecretKey, accessToken, accessTokenSecret);
        schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();
        trigger = newTrigger().withIdentity("newsFetchTrigger", "newsFetchGroup").startNow()
                .withSchedule(simpleSchedule().withIntervalInSeconds(schedulerInterval).repeatForever()).build();

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

    public PublishSubject<Activity> getTweets() {
        return tweetsPublishSubject;
    }

    ;

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

    /**
     * Method to initialize the newsFetchJob with data required for NewsFetchService.
     * Creates a JobDataMap, to which data is added.
     * The JobDataMap object is then used to build the newsFetchJob.
     */
    private void initTweetsFetchJob() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("twitter", twitter);
        jobDataMap.put("tweetsPublishSubject", tweetsPublishSubject);
        tweetsFetchJob = newJob(TwitterFetchService.class)
                .withIdentity("newsFetchJob", "newsFetchJobGroup")
                .usingJobData(jobDataMap)
                .build();
    }

    /**
     * Add the tweetFetchJob to the scheduler.
     *
     * @throws SchedulerException
     */
    private void addJobToScheduler() throws SchedulerException {
        if (tweetsFetchJob != null && trigger != null) {
            scheduler.scheduleJob(tweetsFetchJob, trigger);
        }
    }

    /**
     * Start the scheduler.
     *
     * @throws SchedulerException
     */
    private void startTweetsFetchService() throws SchedulerException {
        scheduler.start();
    }

    /**
     * Stops the TweetsFetchService by shutting down the scheduler.
     *
     * @throws SchedulerException
     */
    private void stopTweetsFetchService() throws SchedulerException {
        scheduler.shutdown(true);
        log.info("Scheduler is shut down.");
    }

    public void startNewsStream() throws SchedulerException, EmptyQueryParamsException {
        if (this.queryParams != null && (!this.queryParams.isEmpty())) {
            initTweetsFetchJob();
            addJobToScheduler();
            startTweetsFetchService();
        } else {
            throw new EmptyQueryParamsException();
        }
    }

    /**
     * Used to stop the tweets stream by stopping the scheduler job for TweetsFetchService.
     *
     * @throws SchedulerException
     */
    public void stopTweetsStream() throws SchedulerException {
        stopTweetsFetchService();
    }

}
