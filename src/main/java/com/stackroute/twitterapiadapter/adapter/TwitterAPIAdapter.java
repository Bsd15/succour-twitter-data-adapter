package com.stackroute.twitterapiadapter.adapter;

import com.ibm.common.activitystreams.Activity;
import com.stackroute.twitterapiadapter.exceptions.EmptyQueryParamsException;
import com.stackroute.twitterapiadapter.exceptions.EmptySearchParametersException;
import com.stackroute.twitterapiadapter.service.TweetsFetchService;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.social.twitter.api.SearchParameters;
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
    private static final String consumerKey = "7DrIzeG3MkSRdCyHmF3D1paTI";
    private static final String consumerSecretKey = "6Bl3O67toi74k6Q605k8HSxHHfDVor3VzZfsTZuELlKqHHK1Gl";
    private static final String accessToken = "1164104302207438848-qPATTkkC4l21HN4m92xCeQ56W2HGoT";
    private static final String accessTokenSecret = "8rwPOCNX2RDXafS0x7FqD00rBJJyrbvhWy0N0M3I5U7qE";
    private List<SearchParameters> queryParams;
    private final Twitter twitter;
    private SchedulerFactory schedulerFactory;
    private Scheduler scheduler;
    private JobDetail tweetsFetchJob;
    private Trigger trigger;
    private PublishSubject<Activity> tweetsPublishSubject;
    private int schedulerInterval = 180;
    private int fetchTweetsCount = 100;

    public TwitterAPIAdapter() throws SchedulerException {
        twitter = new TwitterTemplate(consumerKey, consumerSecretKey, accessToken, accessTokenSecret);
        schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();
        trigger = newTrigger().withIdentity("tweetsFetchTrigger", "tweetsFetchGroup").startNow()
                .withSchedule(simpleSchedule().withIntervalInSeconds(schedulerInterval).repeatForever()).build();
        tweetsPublishSubject = PublishSubject.create();
//        log.debug(twitter.toString());
//        SearchResults results = twitter.searchOperations().search("#spring");
//        results.getTweets().forEach(tweet -> log.debug(tweet.getText()));
    }

    public List<SearchParameters> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<SearchParameters> queryParams) {
        this.queryParams = queryParams;
    }

    public PublishSubject<Activity> getTweets() {
        return tweetsPublishSubject;
    }

    /**
     * Add query to the queryParams list.
     *
     * @param queryParam String to be added to query params list.
     */
    public void addQueryParam(String queryParam) throws EmptyQueryParamsException {
        /*
         * Check if queryParams list is empty and if it's empty then
         * initialize it.
         * */
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<>();
        }
        if (!queryParam.isBlank() && !queryParam.isEmpty()) {
            this.queryParams.add(new SearchParameters(queryParam).lang("en").count(fetchTweetsCount));
        } else throw new EmptyQueryParamsException();
    }

    public void addSearchParamToQueryParams(SearchParameters searchParameters) throws EmptySearchParametersException {
        if (searchParameters == null) {
            if (queryParams == null) {
                this.queryParams = new ArrayList<>();
            }
            this.queryParams.add(searchParameters);
        } else throw new EmptySearchParametersException();
    }

    /**
     * Method to initialize the tweetsFetchJob with data required for TweetsFetchService.
     * Creates a JobDataMap, to which data is added.
     * The JobDataMap object is then used to build the tweetsFetchJob.
     */
    private void initTweetsFetchJob() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("twitter", twitter);
        jobDataMap.put("queryParams", this.queryParams);
        jobDataMap.put("tweetsPublishSubject", tweetsPublishSubject);
        tweetsFetchJob = newJob(TweetsFetchService.class)
                .withIdentity("tweetsFetchJob", "tweetsFetchJobGroup")
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
