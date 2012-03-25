package ru.hfgl.LJWordsAnalyzer;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private final static Logger log = Logger.getLogger(Main.class);
    private final ConcurrentHashMap<String, Integer> words = new ConcurrentHashMap<String, Integer>();

    public static void main(String[] args) throws MalformedURLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DOMConfigurator.configure("./config/log4j.xml");
        new Analyzer(new URL("http://alisomanka.livejournal.com")).go();
        //new Main().doa();
    }

    void doa() {
        Integer count = new Integer(1);
        log.info(count.toString());
        if (words.containsKey("Ввязалась")) count++;
        words.put("Ввязалась", count);
        log.info(words.get("Ввязалась"));
        if (words.containsKey("Ввязалась")) count++;
        words.put("Ввязалась", count);
        log.info(words.get("Ввязалась"));
    }
}
