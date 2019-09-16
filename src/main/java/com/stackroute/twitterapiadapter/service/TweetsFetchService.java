package com.stackroute.twitterapiadapter.service;

import com.ibm.common.activitystreams.Activity;
import io.reactivex.subjects.PublishSubject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.social.twitter.api.SearchParameters;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Twitter;

import java.util.List;

import static com.ibm.common.activitystreams.Makers.activity;
import static com.ibm.common.activitystreams.Makers.object;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Slf4j
public class TweetsFetchService implements Job {
    private Twitter twitter;
    private List<SearchParameters> queryParams;
    private PublishSubject<Activity> tweetsPublishSubject;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        queryParams.forEach(queryParam -> {
            twitter.searchOperations().search(queryParam)
                    .getTweets()
                    .forEach(tweet ->
                            tweetsPublishSubject.onNext(convertToActivity(tweet.getText())));
        });
    }

    /**
     * Convert the given article object into a Activity stream object.
     * @param tweetText to converted to activity stream object
     * @return Activity
     */
    private Activity convertToActivity(String tweetText) {
        return activity()
                .verb("post")
                .actor("News-Adapter")
                .object(object("article").content(tweetText))
                .get();
    }
}
