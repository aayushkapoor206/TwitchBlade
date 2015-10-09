package net.chi6rag.twitchblade_test;

import net.chi6rag.twitchblade.*;
import test_helpers.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class UserActivityTest {
    DbConnection connection = new DbConnection();
    User currentUser;
    UserActivity userActivity;

    // Objects of helper classes
    UserTestHelper userTestHelper = new UserTestHelper(connection);
    TweetTestHelper tweetTestHelper = new TweetTestHelper(connection);
    IOTestHelper ioTestHelper = new IOTestHelper();
    AssertionTestHelper assertionTestHelper = new AssertionTestHelper();

    @Before
    public void beforeEach(){
        currentUser = userTestHelper.getSavedUserObject("foo_example",
                "123456789", connection);
        userActivity = new UserActivity(currentUser);
    }

    @After
    public void afterEach(){
        tweetTestHelper.deleteAllTweets();
        userTestHelper.deleteAllUsers();
    }

    @Test
    public void testGetsActivityOptions(){
        String activityOptions =
            "\nWelcome foo_example" +
            "\n\n"                  +
            "1. Tweet\n"            +
            "2. Your Timeline\n"    +
            "3. Logout\n"           +
            "Choose: ";
        assertEquals(userActivity.getActivityOptions(),
                activityOptions);
    }

    @Test
    public void testAsksForTweet(){
        String question = "What's in your mind?";
        assertEquals(userActivity.askForTweet(), question);
    }

    @Test
    public void testTweetWithValidBodyReturnsTweet(){
        String validTweetBody = "hello";
        Tweet tweet = userActivity.tweet(validTweetBody);
        Assert.assertEquals(tweet.getClass().getSimpleName(), "Tweet");
        Assert.assertEquals(tweet.getId().getClass()
                .getSimpleName(), "Integer");
        Assert.assertEquals(tweet.getBody(), "hello");
        Assert.assertEquals(tweet.getUserId(), currentUser.getId());
    }

    @Test
    public void testTweetWithInvalidBodyReturnsNull(){
        String invalidTweetBody = tweetTestHelper.getInvalidTweetBody();
        Tweet tweet = userActivity.tweet(invalidTweetBody);
        Assert.assertEquals(tweet, null);
    }

    @Test
    public void testTweetWithInvalidBodyPrintsErrorOnStdOut(){
        ByteArrayOutputStream consoleOutput = ioTestHelper.mockStdOut();
        String invalidTweetbody = tweetTestHelper.getInvalidTweetBody();
        userActivity.tweet(invalidTweetbody);
        CharSequence errorMessage = "Tweet cannot be saved";
        assertionTestHelper.assertContains(consoleOutput.toString(),
                (String) errorMessage);
        ioTestHelper.setStdOutToDefault();
    }

    @Test
    public void testPrintsUserTimelineOnStdOutForUserWithTweets(){
        ByteArrayOutputStream consoleOutput = ioTestHelper.mockStdOut();
        Tweet firstValidTweet = tweetTestHelper.getSavedTweetObject("testing one",
                this.currentUser.getId(), this.connection);
        Tweet secondValidTweet = tweetTestHelper.getSavedTweetObject("testing two",
                this.currentUser.getId(), this.connection);
        userActivity.printTimeline();
        assertionTestHelper.assertContains(consoleOutput.toString(),
                firstValidTweet.getBody());
        assertionTestHelper.assertContains(consoleOutput.toString(),
                secondValidTweet.getBody());
        ioTestHelper.setStdOutToDefault();
    }

    @Test
    public void testLogoutSetsDatabaseConnectionToNull(){
        userActivity.logout();
        try {
            Object connection = getPrivateField(userActivity, "connection");
            assertEquals(connection, null);
        } catch (NoSuchFieldException e) {
            System.out.println("UserActivityTest: " +
                    "Private Field Connection not Found");
        } catch (IllegalAccessException e) {
            System.out.println("UserActivityTest: " +
                    "Private Field not Accessible");
        }
    }

    @Test
    public void testLogoutSetsCurrentUserToNull(){
        userActivity.logout();
        try {
            Object currentUser = getPrivateField(userActivity, "currentUser");
            assertEquals(currentUser, null);
        } catch (NoSuchFieldException e) {
            System.out.println("UserActivityTest: " +
                    "Private Field CurrentUser not Found");
        } catch (IllegalAccessException e) {
            System.out.println("UserActivityTest: " +
                    "Private Field not Accessible");
        }
    }

    private Object getPrivateField(Object privateFieldContainer,
       String privateFieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = privateFieldContainer.getClass().getDeclaredField(privateFieldName);
        field.setAccessible(true);
        return field.get(privateFieldContainer);
    }

}