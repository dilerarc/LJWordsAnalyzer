package ru.hfgl.LJWordsAnalyzer;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Analyzer {

    private final static Logger log = Logger.getLogger(Analyzer.class);

    private final String ljURL;
    private final LinkedBlockingQueue<String> postLinks = new LinkedBlockingQueue<String>();
    private final ConcurrentHashMap<String, Integer> words = new ConcurrentHashMap<String, Integer>();

    public Analyzer(String ljURL) {
        this.ljURL = ljURL;
    }

    public void go() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        ExecutorService postFinderExecutor = Executors.newFixedThreadPool(5);
        ExecutorService postParserExecutor = Executors.newFixedThreadPool(1);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
        df.applyPattern("/yyyy/MM/dd/");
        //calendar.set(Calendar.DAY_OF_MONTH, 30);
        //calendar.set(Calendar.MONTH, 8);
        //calendar.set(Calendar.YEAR, 2006);

        for (int i = 0; i < 1; i++) {
            postParserExecutor.submit(new PostParser(postLinks, words));
        }
        while (!PostFinder.isDoneFlag()) {
            String nextURL = ljURL + df.format(calendar.getTime());
            postFinderExecutor.submit(new PostFinder(nextURL, postLinks));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        postFinderExecutor.shutdown();
        while (!postFinderExecutor.isTerminated()) {
        }
        log.info("POST FINDER IS TERMINATED");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        postParserExecutor.shutdownNow();
        while (!postParserExecutor.isTerminated()) {
        }

        log.info("Parsing is over");
        log.info("Saving...");

        TreeMap<String, Integer> resultSortedMap = new TreeMap<String, Integer>(new ValueComparator(words));
        resultSortedMap.putAll(words);

        saveResult(resultSortedMap);
        log.info("Parsing is complete");
    }

    private void saveResult(Map<String, Integer> resultSortedMap) {
        StringBuilder xml = new StringBuilder();
        xml
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(System.lineSeparator())
                .append("<")
                .append("words")
                .append(">")
                .append(System.lineSeparator());
        for (String key : resultSortedMap.keySet()) {
            xml
                    .append("  <")
                    .append(key)
                    .append(" ")
                    .append("count=")
                    .append("\"")
                    .append(resultSortedMap.get(key))
                    .append("\"")
                    .append("/>")
                    .append(System.lineSeparator());
        }
        xml.append("</").append("words").append(">");

        try {
            String fileName = ljURL.replace("http://", "") + ".xml";
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            bw.write(xml.toString());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            log.error("CANNOT SAVE FILE");
        }
    }
}

class ValueComparator implements Comparator {

  Map base;
  public ValueComparator(Map base) {
      this.base = base;
  }

  public int compare(Object a, Object b) {

    if((Integer)base.get(a) < (Integer)base.get(b)) {
      return 1;
    } else if((Integer)base.get(a) == (Integer)base.get(b)) {
      return 0;
    } else {
      return -1;
    }
  }
}
