import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static java.lang.System.exit;


public class Main {

    // Super "secret" Twilio authenticators
    public static final String ACCOUNT_SID = "AC1d1b4e23ae72003dd66fab468b156aa8";
    public static final String AUTH_TOKEN= "fb1a3b070f97245e61e44109bc85be92";

    // Phone numbers to use
    public PhoneNumber luke = new PhoneNumber("+19403906397");
    public PhoneNumber aidan = new PhoneNumber("+19407835766");
    public PhoneNumber sending =  new PhoneNumber("+18888404061");

    // r/PhotoMarket url, sorting by new, JSON version
    static String url = "https://www.reddit.com/r/photomarket/new/.json";

    Document doc = Jsoup.connect(url).ignoreContentType(true).get();

    public List<String> allTitles = new ArrayList<>();

    public String newestPost = null;

    public Main() throws IOException {
        // Login the Twilio account
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//
//        String testDoc = doc.toString();
//        String[] split = testDoc.split("\"");
//
//        // Create the list of URLS containing x100v at the time the program starts
//        // This ensures that when a new Title comes
//        // through, we know we haven't seen it before, so it is a new post
//        for(int i = 0; i < split.length; i++)
//        {
//            // Check for the keyword
//            if(split[i].contains("x100v") || split[i].contains("X100V"))
//            {
//                if(split[i-2].contains("title")){
//                    allTitles.add(split[i]);
//                }
//            }
//        }
    }

    /**
     * Find the most recent post mentioning 'x100v'
     * @return
     */
    public String findNewestPost()
    {
        String testDoc = doc.toString();
        String[] split = testDoc.split("\"");

        for(int i = 0; i < split.length; i++)
        {
            // Check for the keyword
            if(split[i].contains("x100v") || split[i].contains("X100V"))
            {
                if(split[i-2].contains("title")){
                    return split[i];
                }
            }
        }

        return null;
    }

    /**
     * Uses JSoup to pull the date from Reddit. Parses for the keyword, gathers URL, etc.
     *    Called repeatedly to check for new URLS
     * @return  true if we detect a new URL containing 'x100v' that we haven't seen before.
     */
    public boolean searchForCamera()
    {
        // Pull the website data EACH time. Fixes meatball bug
        try {
            doc = Jsoup.connect(url).ignoreContentType(true).get();
        } catch (IOException e) {
            System.out.println("Failed to connect to Reddit");
            throw new RuntimeException(e);
        }

        List<String> currentTitles = new ArrayList<>();

        String testDoc = doc.toString();
        String[] split = testDoc.split("\"");

        for(int i = 0; i < split.length; i++)
        {
            // Check for the keyword
            if(split[i].contains("x100v") || split[i].contains("X100V"))
            {
                if(split[i-2].contains("title")){
                    currentTitles.add(split[i]);
                }
            }
        }

        // If the first elements in the lists are not the same, we have encountered a new title
        if(!currentTitles.get(0).equals(allTitles.get(0))){
            allTitles = currentTitles;
            return true;
        }

        return false;
    }

    /**
     * Logs in to Twilio, sends bot initialization method, and then checks for new camera URLS
     *    every 30 seconds. Runs in a continuous while loop - bad practice but screw it.
     * @throws InterruptedException
     */
    public void runBot() throws InterruptedException {
        // Variable just used for testing purposes. Used to send periodic message that bot is still functioning
        int counter = 0;

        String init = "\n\nBot has been reactivated." +
                "\n\n" +
                "Meatballs should now be detected.";
        // sendMessage(init);

        while(true) {
            // Wait for 30 seconds
            int waitForSeconds = 30;
            TimeUnit.SECONDS.sleep(waitForSeconds);
            // testURLS();

            counter++;
            if(counter == 120){
                System.out.println("Bot still running");
                counter = 0;
            }

            if(searchForCamera()) {
                System.out.println("New Post detected");
                String message = "I have detected a new post with title: \n " +
                        "" + allTitles.get(0);
                sendMessage(message);
            }

        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Main main = new Main();
        System.out.println(main.findNewestPost());

        //main.sendMessageToLuke(main.allTitles.get(0));
        //main.runBot();
    }

    /**
     * Uses twilio API to create and send message to desired recipients
     * @param message  The message to send
     */
    public void sendMessage(String message){
        System.out.println("Sending Messages...");

        Message sendToLuke = Message.creator(luke, sending, message).create();
        System.out.println(sendToLuke.getSid());

        Message sendToAidan = Message.creator(aidan, sending, message).create();
        System.out.println(sendToAidan.getSid());
    }

    public void sendMessageToLuke(String message){
        System.out.println("Sending Messages...");

        Message sendToLuke = Message.creator(luke, sending, message).create();
        System.out.println(sendToLuke.getSid());
    }


}