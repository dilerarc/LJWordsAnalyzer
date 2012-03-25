package ru.hfgl.LJWordsAnalyzer;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Analyzer {

    private final static Logger log = Logger.getLogger(Analyzer.class);

    private final URL ljURL;
    private final LinkedBlockingQueue<String> postLinks = new LinkedBlockingQueue<String>();
    //private final ConcurrentHashMap<String,Integer> words = new ConcurrentHashMap<String, Integer>();
    private final ConcurrentHashMap<String,Integer> words = new ConcurrentHashMap<String, Integer>();

    public Analyzer(URL ljURL) {
        this.ljURL = ljURL;
    }

    public void go() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        ExecutorService postFinderExecutor = Executors.newFixedThreadPool(5);
        ExecutorService postParserExecutor = Executors.newFixedThreadPool(1);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
        df.applyPattern("/yyyy/MM/dd/");
        //calendar.set(Calendar.DAY_OF_MONTH,30);
        //calendar.set(Calendar.MONTH,8);
        //calendar.set(Calendar.YEAR,2006);

        for (int i = 0; i < 1; i++) {
              postParserExecutor.submit(new PostParser(postLinks,words));
        }
        while(!PostFinder.isDoneFlag()) {
            String nextURL = ljURL + df.format(calendar.getTime());
            postFinderExecutor.submit(new PostFinder(nextURL, postLinks));
            calendar.add(Calendar.DAY_OF_YEAR,-1);
        }
        postFinderExecutor.shutdown();
        while (!postFinderExecutor.isTerminated()){}
        log.info("POST FINDER IS TERMINATED");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        postParserExecutor.shutdownNow();
        while (!postParserExecutor.isTerminated()){}
        log.info(words);
    }
}
