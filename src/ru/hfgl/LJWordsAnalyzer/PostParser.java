package ru.hfgl.LJWordsAnalyzer;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class PostParser implements Runnable {

    private final static Logger log = Logger.getLogger(PostParser.class);

    private final LinkedBlockingQueue<String> postLinks;
    private final ConcurrentHashMap<String, Integer> words;

    public PostParser(LinkedBlockingQueue<String> postLinks, ConcurrentHashMap<String, Integer> words) {
        this.postLinks = postLinks;
        this.words = words;
    }

    public void run() {
        while (true) {
            try {
                log.info("parsing start");
                String currentURL = postLinks.take();
                Document doc = Jsoup.connect(currentURL).get();
                log.info("connected to " + currentURL);
                Elements el = doc.getElementsByClass("asset-body");
                String s = el.text().replace("Метки.*", "").trim();
                String[] ss = s.split("\\s");
                log.info("found " + ss.length + " words");
                for (int i = 0; i < ss.length; i++) {
                    String word = ss[i].toLowerCase().replaceAll("[^А-Яа-яЁёA-Za-z]", "");
                    Integer count = 1;
                    if (words.containsKey(word)) count++;
                    words.put(word, count);
                }
                log.info("words from " + currentURL + " added");

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                break;
            }
        }
    }
}
