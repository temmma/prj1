package prj1;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Content {

	public static void main(String[] args) throws Exception {
        HttpClient httpclnt = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://dolboeb.livejournal.com/2449749.html?format=light");
        
        HttpResponse httpresp = httpclnt.execute(httpget);       
        HttpEntity entity = httpresp.getEntity();         
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        
        Document doc = Jsoup.parse(html_text,"UTF-8");      
        Elements content = doc.getElementsByClass("entry-title");
        String text = doc.getElementsByClass("b-singlepost-body").text();        
        System.out.println(text);
        content = doc.getElementsByClass("entry-date");
//        GregorianCalendar dateEntry=getEntryDate(content.html());
//        System.out.println(dateEntry.getTime());
        content = doc.getElementsByClass("b-singlepost-body");
        String entryContent = content.text();
        System.out.println(entryContent);
        httpget.abort();
	}

	public static GregorianCalendar getEntryDate(String input) throws Exception{
		String buff = input;
		int start = buff.indexOf("title=")+7;
		int end = start + 25;
		buff = buff.substring(start, end);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		GregorianCalendar result = new GregorianCalendar();
		result.setTime(df.parse(buff));		
		return result;
	}
	
}
