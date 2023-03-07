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
    public static final String AUTH_TOKEN= "5db980bd16fedc399df1fd3f16a84d94";

    // Phone numbers to use
    public PhoneNumber luke = new PhoneNumber("+19403906397");
    public PhoneNumber aidan = new PhoneNumber("+19407835766");
    public PhoneNumber sending =  new PhoneNumber("+18888404061");

    // r/PhotoMarket url, sorting by new, JSON version
    static String url = "https://www.reddit.com/r/photomarket/new/.json";

    Document doc = Jsoup.connect(url).ignoreContentType(true).get();

    public List<String> seenUrls = new ArrayList<>();

    public Main() throws IOException {
        String testDoc = doc.toString();
        String[] split = testDoc.split("\\s+");
        boolean lookingForURL = false;

        // Create the list of URLS containing x100v at the time the program starts
        // This ensures that when a new URL comes through, we know we haven's seen it before and it is a new post
        for(int i = 0; i < split.length; i++)
        {
            // Check for the keyword
            if(split[i].contains("x100v") || split[i].contains("X100V"))
            {
                // If the word is detected, search for the URL pertaining to the word
                lookingForURL = true;
            }

            // If we are searching for the URL
            if(lookingForURL)
            {
                // Grab it and prepare to send the URL in message form
                if(split[i].contains("url"))
                {
                    // If we haven't seen this exact url before, add it to our database of key URLs
                    if(!seenUrls.contains(split[i + 1]))
                    {
                        seenUrls.add(split[i+1]);
                    }
                    // Whether we have seen this paticular URL before or not, we should stop looking for a url at this point
                    lookingForURL = false;
                }

            }
        }
    }

    /**
     * Uses JSoup to pull the date from Reddit. Parses for the keyword, gathers URL, etc.
     *    Called repeatedly to check for new URLS
     * @return  true if we detect a new URL containing 'x100v' that we haven't seen before.
     */
    public boolean searchForCamera()
    {
        String testDoc = doc.toString();
        String[] split = testDoc.split("\\s+");
        boolean lookingForURL = false;

        for(int i = 0; i < split.length; i++)
        {
            // Check for the keyword
            if(split[i].contains("x100v") || split[i].contains("X100V"))
            {
                // If the word is detected, search for the URL pertaining to the word appearance
                lookingForURL = true;
            }

            // If we are searching for the URL
            if(lookingForURL)
            {
                // Grab it and prepare to send the URL in message form
                if(split[i].contains("url")){
                    if(!seenUrls.contains(split[i + 1])) {
                        // JSON format will have structure "url: " "www.reddit.com..." so we need string after "url"
                        seenUrls.add(split[i+1]);
                        return true;
                    }
                    lookingForURL = false;
                }

            }
        }

        return false;
    }

    /**
     * Logs in to Twilio, sends bot initialization method, and then checks for new camera URLS
     *    every 30 seconds. Runs in a continuous while loop - bad practice but screw it.
     * @throws InterruptedException
     */
    public void runBot() throws InterruptedException {
        // Login the Twilio account
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String init = "\n\nBot has been activated." +
                "\n\n" +
                "I will be checking Reddit every 30 seconds for new posts on r/photoMarket for the term 'x100v'" +
                ". I will only message you if I detect" +
                " a new post containing that phrase. I do not have the capabilities to distinguish between" +
                " buyers and sellers, I will just send you every post that mentions x100v.";
        sendMessage(init);

        while(true) {
            // Wait for 30 seconds
            int waitForSeconds = 30;
            TimeUnit.SECONDS.sleep(waitForSeconds);
            if(searchForCamera()) {
                String message = "I have detected a new mention of x100v on r/PhotoMarket at " + seenUrls.get(seenUrls.size()-1);
                sendMessage(message);
            }

        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Main main = new Main();
        main.runBot();
    }

    /**
     * Uses twilio API to create and send message to desired recipients
     * @param message  The message to send
     */
    public void sendMessage(String message){
        Message sendToLuke = Message.creator(luke, sending, message).create();
        System.out.println(sendToLuke.getSid());

//        Message sendToAidan = Message.creator(aidan, sending, message).create();
//        System.out.println(sendToAidan.getSid());
    }

}