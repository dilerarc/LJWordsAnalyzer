package ru.hfgl.LJWordsAnalyzer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class PostFinder implements Runnable {

    private final static Logger log = Logger.getLogger(PostFinder.class);

    private final String currentURL;
    private final LinkedBlockingQueue<String> postLinks;

    private static boolean doneFlag = false;

    public PostFinder(String url, LinkedBlockingQueue<String> postLinks) {
        this.currentURL = url;
        this.postLinks = postLinks;
    }

    public void run() {
        if (!doneFlag) {
            try {
                log.info("connecting to " + currentURL);
                Document doc = Jsoup.connect(currentURL).get();
                Elements el = doc.getElementsByClass("page-header2");
                for (Element e : el) {
                    postLinks.put(e.children().tagName("a").attr("href"));
                }

                if (doc.getElementsByClass("skiplinks").first().child(0).text().equals("Next Day")) {
                    doneFlag = true;
                }
                System.gc();
            } catch (IOException e) {
                log.error("CAN'T CONNECT TO URL " + currentURL);
            } catch (InterruptedException e) {
                log.info("finder is interrupted");
            }
        }
    }

    public static boolean isDoneFlag() {
        return doneFlag;
    }
}
