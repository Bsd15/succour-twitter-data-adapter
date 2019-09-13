package com.stackroute.twitterapiadapter.service;

import com.ibm.common.activitystreams.Activity;
import io.reactivex.subjects.PublishSubject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.social.twitter.api.Twitter;

public class TwitterFetchService implements Job {
    private Twitter twitter;
    private PublishSubject<Activity> tweetsPublishSubject;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        /*
        * TODO Change name from TwitterFetchService to TweetsFetchService
        * TODO Complete the execute method
        * */
    }
}
