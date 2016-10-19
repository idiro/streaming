package com.idiro.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

import twitter4j.FilterQuery;
import twitter4j.Logger;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class SimpleTwitterStream {
	
	private static Logger logger = Logger.getLogger(SimpleTwitterStream.class);
	
	public static final String propOAuthConsumerKey = "twitter.consumer.key";
	public static final String propOAuthConsumerSecret = "twitter.consumer.secret";
	public static final String propOAuthAccessToken = "twitter.access.token";
	public static final String propOAuthAccessTokenSecret = "twitter.access.token.secret";
	public static final String propServerName = "server.name";
	public static final String propPort = "server.port";
	public static final String propKeywords = "keywords";
	public static final String propPrint = "print";
	public static final String propJSON = "json";
	
	
	private static String oAuthConsumerKey;
	private static String oAuthConsumerSecret;
	private static String oAuthAccessToken;
	private static String oAuthAccessTokenSecret;
	private static String serverName;
	private static String port;
	private static String keywords;
	private static boolean print;
	private static boolean json;
	
	protected static Socket pingSocket;
	protected static PrintWriter out;
	
	private static void displayPropRequired(){
		logger.info("This programme connect to twitter and retrieve the following data (^A delimited):");
		logger.info("date, username, location, content");
		logger.info("The programme requires a property file");
		logger.info("Property required: ");
		logger.info(propOAuthConsumerKey+": see twitter API");
		logger.info(propOAuthConsumerSecret);
		logger.info(propOAuthAccessToken);
		logger.info(propOAuthAccessTokenSecret);
		logger.info(propServerName+": where to redirect the data");
		logger.info(propPort);
		logger.info(propKeywords+": , delimited list of key words");
		logger.info("Optional property: ");
		logger.info(propPrint+": if 'true', print instead of sending to port");
	}
	
	private static String loadprop(Properties props, String key){
		String ans = props.getProperty(key);
		if(ans == null){
			displayPropRequired();
			System.exit(1);
		}
		return ans;
	}
	
	private static void readPropFile(File propF) throws FileNotFoundException, IOException{
		Properties prop = new Properties();
		prop.load(new FileInputStream(propF));
		oAuthConsumerKey = loadprop(prop,propOAuthConsumerKey);
		oAuthConsumerSecret = loadprop(prop,propOAuthConsumerSecret);
		oAuthAccessToken = loadprop(prop,propOAuthAccessToken);
		oAuthAccessTokenSecret = loadprop(prop,propOAuthAccessTokenSecret);
		serverName = loadprop(prop,propServerName);
		port = loadprop(prop,propPort);
		keywords = prop.getProperty(propKeywords,"");
		print = "true".equalsIgnoreCase(prop.getProperty(propPrint,"false"));
		json = "true".equalsIgnoreCase(prop.getProperty(propJSON,"false"));
	}
	
    public static void main(String[] args) throws Exception {
    	if(args.length != 1){
    		displayPropRequired();
    		System.exit(1);
    	}
    	File f = new File(args[0]);
    	if(!f.exists()){
    		displayPropRequired();
    		System.exit(1);
    	}
    	logger.info("Read properties...");
    	readPropFile(f);
    	logger.info("Configure streaming...");
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(oAuthConsumerKey);
        cb.setOAuthConsumerSecret(oAuthConsumerSecret);
        cb.setOAuthAccessToken(oAuthAccessToken);
        cb.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
        cb.setJSONStoreEnabled(true);
        cb.setIncludeEntitiesEnabled(true);
        
        if(!print){
        	logger.info("Connect to the socket...");
        	pingSocket = new Socket(serverName, Integer.valueOf(port));
        	out = new PrintWriter(pingSocket.getOutputStream(), true);
        }
        
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        StatusListener listener = new StatusListener() {

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			public void onStatus(Status status) {
                String toWrite = null;
                if(json){
                	toWrite = new String(TwitterObjectFactory.getRawJSON(status).getBytes());
                }else{
                    User user = status.getUser();
                	StringBuilder app = new StringBuilder();
                	app.append(status.getCreatedAt());
                	app.append("\001").append(user.getScreenName());
                	app.append("\001").append(user.getLocation());
                	app.append("\001").append(status.getText());
                	app.append("\n");
                	toWrite = app.toString();
                }
                
                if(print){
                	logger.info(toWrite);
                }else{
                	out.println(toWrite);
                }
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			public void onScrubGeo(long userId, long upToStatusId) {
			}

			public void onStallWarning(StallWarning warning) {
			}


        };
        FilterQuery fq = new FilterQuery();

        twitterStream.addListener(listener);
        try{
        	if(keywords.isEmpty()){
        		logger.info("Start sampling...");
        		twitterStream.sample();
        	}else{
        		logger.info("Start filtering...");
        		String keywordsArr[] = keywords.split(",");
        		fq.track(keywordsArr);
        		twitterStream.filter(fq);
        	}
        }catch(Exception e){
        	logger.info("Shutdown...");
        	twitterStream.shutdown();
        }

    }
}