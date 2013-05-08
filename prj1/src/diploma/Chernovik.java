package prj1;
import java.net.URI;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

public class Chernovik {

	public static Map<String,GregorianCalendar> getDates(String input){
		
		String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		StringBuilder created = new StringBuilder(input.substring(input.indexOf("Created on ")+"Created on".length(), input.indexOf("(#")));
		while (created.charAt(0) == ' ') created.deleteCharAt(0);
		while (created.charAt(created.length()-1) == ' ') created.deleteCharAt(created.length()-1); 

		StringBuilder StringDay = new StringBuilder("");
		int i = 0;
		while (created.charAt(i) != ' ')
			StringDay.append(created.charAt(i++));
		created.delete(0, StringDay.length()+1);
		int day = 1;
		try {
			day=Integer.valueOf(StringDay.toString());
		} catch (Exception e) {
			System.err.println(e);
			day=1;
		}
		
		
		int month = 1;
		for (int j=0; j<months.length; j++)
			if (created.toString().contains(months[j])) month = j;
		created.delete(0, months[month].length()+1);
		
		int year = 1999;
		try {
			year = Integer.valueOf(created.toString());
		} catch (Exception e) {
			System.err.println(e);
			year=1;
		}
		
		
		GregorianCalendar dateCreated = new GregorianCalendar(year, month, day);
		
		StringBuilder updated = new StringBuilder(input.substring(input.indexOf("Last updated on")+"Last updated on".length(), input.length()));
		while (updated.charAt(0) == ' ') updated.deleteCharAt(0);
		i = 0;
		StringDay = new StringBuilder("");
		while (updated.charAt(i) != ' ')
			StringDay.append(updated.charAt(i++));
		
		updated.delete(0, StringDay.length()+1);
		try {
			day=Integer.valueOf(StringDay.toString());
		} catch (Exception e) {
			System.err.println(e);
			day=1;
		}
		

		for (int j=0; j<months.length; j++)
			if (updated.toString().contains(months[j])) month = j;
		updated.delete(0, months[month].length()+1);
		try {
			year = Integer.valueOf(updated.substring(0, 4));
		} catch (Exception e) {
			System.err.println(e);
			year=1999;			
		}
		
		GregorianCalendar dateUpdated = new GregorianCalendar(year, month, day);

		Map<String, GregorianCalendar> result = new TreeMap<String, GregorianCalendar>();
		result.put("dateCreated", dateCreated);
		result.put("dateUpdated", dateUpdated);
		
		return result;
	}
	
    public static void main(String[] args) throws Exception {
    	
         System.out.println(new GregorianCalendar().getTime());

         HttpClient httpclnt = new DefaultHttpClient();
         URIBuilder builder = new URIBuilder();
         List<String> hrefs = new ArrayList<String>();
         String username = "lytdybr";

         HttpGet httpget = new HttpGet("http://"+username+".livejournal.com/profile");
         HttpResponse httpresp = httpclnt.execute(httpget);       
         HttpEntity entity = httpresp.getEntity();         
         String html_text = new String();
         if (entity != null)
             html_text = EntityUtils.toString(entity, "UTF-8");
         
         Document doc = Jsoup.parse(html_text,"UTF-8");
         Elements content = doc.getElementsByClass("b-account-level");
         Map<String,GregorianCalendar> userDates;
         if (!content.text().isEmpty()) {
        	 userDates=getDates(content.text());
         
         int year = userDates.get("dateUpdated").get(GregorianCalendar.YEAR);
         int month = userDates.get("dateUpdated").get(GregorianCalendar.MONTH)+1;
         
         
         while (year >= userDates.get("dateCreated").get(GregorianCalendar.YEAR)) {

        	 builder.setScheme("http").setHost("www.livejournal.com").setPath("/view/")
                 .setParameter("type", "month")
                 .setParameter("user", username)
                 .setParameter("y", String.valueOf(year))
                 .setParameter("m", String.valueOf(month))
                 .setParameter("format", "light");
                 URI uri = builder.build();
                 httpget = new HttpGet(uri);
//                System.out.println(httpget.getURI());
                 httpresp = httpclnt.execute(httpget);
                 entity = httpresp.getEntity();
                 html_text = new String();
                 if (entity != null)
                     html_text = EntityUtils.toString(entity, "UTF-8");
                 httpget.abort();
                 doc = Jsoup.parse(html_text);
                 Elements links = doc.select("a[href]");
                 for (Element link : links) {
                     if ((link.toString().contains(username)) & (link.toString().contains("html")))
                         hrefs.add(link.attr("href"));
                 }
                 month--;
                 if (month == 0){
                     year--;
                     month=12;
                 }
         }
         System.out.println(new GregorianCalendar().getTime());
         Connection          conn  = null;
         PreparedStatement   pstmt = null;
         Properties connInfo = new Properties();
         connInfo.put("characterEncoding","UTF8");
         connInfo.put("user", "user");
         connInfo.put("password", "password123");
         conn  = 
         DriverManager.getConnection("jdbc:mysql://10.2.54.29/?", connInfo);
         for (String str : hrefs) {
             pstmt = conn.prepareStatement("INSERT INTO prj1.test(`href`)  VALUES ('"+str+"');");
             pstmt.execute();
         }
         System.out.println(new GregorianCalendar().getTime());
     }
    }
}
