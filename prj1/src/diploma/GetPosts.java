package diploma;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetPosts {
	
	public static void getPosts(String username, int year, int month) throws Exception {
		String strYear = String.valueOf(year);
		String strMonth = String.valueOf(month);
		if (strMonth.length()<2) strMonth = "0"+strMonth;
			
		HttpClient httpclnt = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://"+username+".livejournal.com/"+strYear+"/"+strMonth+"/?format=light");
		System.out.println("http://"+username+".livejournal.com/"+strYear+"/"+strMonth+"/?format=light");
        HttpResponse httpresp = httpclnt.execute(httpget);
        HttpEntity entity = httpresp.getEntity();
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        Document doc = Jsoup.parse(html_text);
        Elements table = doc.select("dl");
        Elements dates = table.select("dt");
        Elements tables = table.select("table");
        Iterator<Element> it = tables.iterator();
        Element entry;
        
        List<Character> digits = new ArrayList<Character>(Arrays.asList('0','1','2','3','4','5','6','7','8','9'));
        
        String strDay;
        String title;
        Date dateTime = null;
        int commentsCount;
        String post_id;
        
        for (Element t : dates) {
        	strDay="";
        	for (int i=0; i<t.text().length(); i++)
        		if (digits.contains(t.text().charAt(i))) strDay = strDay + t.text().charAt(i);
        	if (strDay.length()<2) strDay = "0"+strDay;
        	
        	entry = it.next();
        	Elements rows = entry.select("tbody").select("tr").select("td");
        	int i=0;
        	StringBuilder time = new StringBuilder();
        	for (Element tt : rows) {
        		if (i++%2 == 0) { 										//первая запись время
        			time.setLength(0);
        			time.append(tt.text());
        			if (time.toString().contains("a")) {
        				time.deleteCharAt(time.length()-1);
        				time.append(" AM");
        			}
        			else{
        				time.deleteCharAt(time.length()-1);
        				time.append(" PM");
        			}
        			dateTime = new SimpleDateFormat("yyyy.MM.dd'T'K:mm a").
        					parse(strYear+"."+strMonth+"."+strDay+"T"+time.toString());
        		}
        		else{													//вторая запись - заголовок, ссылка, количество комментов
        			post_id = tt.html();
        			title = "";
        			commentsCount = 0;
        			String[] buffer = tt.text().split(" ");
        			if (buffer.length == 1) {							//заголовок одно слово, коментов нету
        				commentsCount = 0;
        				title = tt.select("a[href]").text();
        			} else 	if (buffer.length > 1) { 					//если больше 1 слова
            			if (buffer[buffer.length-1].equals("reply")) { 	//всего один коммент
            				commentsCount = 1;
            				title = tt.select("a[href]").text();
            			} else if (buffer[buffer.length-1].equals("replies")) {
            				try {
            					commentsCount = Integer.parseInt(buffer[buffer.length-2]); //третье с конца слово - количество
            					title = tt.select("a[href]").text();
							} catch (Exception e) {
								commentsCount = 0;
								title = tt.select("a[href]").text();
							}
            			} else {
							commentsCount = 0;
							title = tt.select("a[href]").text();
            			}
        			}
        			String tmp = "<a href=\"http://"+username+".livejournal.com/";
        			post_id = post_id.substring(tmp.length(), post_id.indexOf(".html"));
        			insertPost(username, post_id, dateTime, title, commentsCount);
        		}
        	}
        }
	}
	
	public static void insertPost(String username, String post_id, Date dateTime, String title, int commentsCount) throws SQLException{
        Connection          conn  = null;
        PreparedStatement   pstmt = null;
        Properties connInfo = new Properties();
        connInfo.put("characterEncoding","UTF8");
        connInfo.put("user", "user");
        connInfo.put("password", "password123");
        conn  = 
        DriverManager.getConnection("jdbc:mysql://192.168.1.38/?", connInfo);
        String sql = "INSERT INTO prj1.posts(`username`, `post_id`, `time`, `title`, `comments`) " +
        		"VALUES ('"+username+"','"+post_id+"','"+new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(dateTime)+
        		"','"+title+"','"+commentsCount+"');";
        pstmt = conn.prepareStatement(sql);
        System.out.println(sql);
        pstmt.execute();
	}

	public static void main(String[] args) throws Exception {
		getPosts("tema",2013,1);
	}

}
