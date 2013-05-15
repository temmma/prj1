package diploma;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LJUser {
	private String username;
	private Date createdDate;
	private Date updatedDate;
    private Connection conn;
    private PreparedStatement DBStatement;
    private Properties connInfo;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	
	public LJUser(String username){
		this.username = username;
		dataBaseConnection();
		checkProfile();
	}
	
	private boolean dataBaseConnection(){
		boolean result = false;
		connInfo = new Properties();
        connInfo.put("characterEncoding","UTF8");
        connInfo.put("user", "user");
        connInfo.put("password", "password123");
        try {
        	conn = DriverManager.getConnection("jdbc:mysql://192.168.1.38/?", connInfo);
        	result = true;
		} catch (SQLException e) {
			System.out.println("Unable to connect to database!\n"+e);
		}        
        return result;
	}
	
	private boolean dataBaseRequest(String request, String[] prepared){
		boolean result = false;
        try {
            DBStatement = conn.prepareStatement(request);
            if (prepared != null)
            	for (int i=0; i<prepared.length; i++)
            		DBStatement.setString(i+1, prepared[i]);
            DBStatement.execute();
            result = true;
		} catch (SQLException e) {
			System.out.println("Unable to make request to database!\n"+request);			
			System.out.println(e);
		}
		return result;
	}

	public Document obtainHtmlText(String get){
		Document doc = null;
		HttpHost targetHost = new HttpHost(username+".livejournal.com", 80, "http");
		DefaultHttpClient httpclient = new DefaultHttpClient();
		BasicHttpContext localcontext = new BasicHttpContext();
		HttpGet httpget = new HttpGet(get);
		String htmlText = "";
		HttpResponse httpresp;
		try {
			httpresp = httpclient.execute(targetHost, httpget, localcontext);
	        HttpEntity entity = httpresp.getEntity();
	        if (entity != null)
	            htmlText = EntityUtils.toString(entity, "UTF-8");
	        doc = Jsoup.parse(htmlText,"UTF-8");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}

	private Date decodeDate(String which, String toParse){
		Date result = null;
		String buff = "";
		if (which == "Created")
			buff = toParse.substring(toParse.indexOf("Created on ")+"Created on ".length(), toParse.indexOf("(#")-1);
		else
			buff = toParse.substring(toParse.indexOf("updated on ")+"updated on ".length(), toParse.length());
		SimpleDateFormat sdf = new SimpleDateFormat("d MMMMMMMMM yyyy", Locale.US);
		try {
			result = sdf.parse(buff);
		} catch (Exception e) {
			System.out.println("Unable to parse date from "+buff);
			System.err.println(e);
		}
		return result;
	}

	public boolean checkProfile(){
		boolean result = false;
		String sql = "SELECT * FROM prj1.ljusers " +
				"WHERE `name` = '"+username+"';";
        result = dataBaseRequest(sql, null);
        Date oldUpdate = null;        
        try {
        	ResultSet rst = DBStatement.getResultSet();
		        while (rst.next()){
		        oldUpdate = rst.getDate(2);	
		        }			 
		} catch (SQLException e) {
			System.out.println("Unable to get ResultSet from query "+sql);
			e.printStackTrace();
		}        
		Document doc = obtainHtmlText("/profile");
        Elements content = doc.getElementsByClass("b-account-level");
		createdDate = decodeDate("Created", content.text());
		updatedDate = decodeDate("Updated", content.text());
		
		if (oldUpdate == null){								//������ ���
	        sql = "INSERT INTO prj1.ljusers(`name`, `created`, `updated`) " +
	        		"VALUES ('"+username+"','"+dateFormat.format(createdDate)+"','"+dateFormat.format(updatedDate)+"');";
	        obtainEntries(createdDate, updatedDate);
	        result = dataBaseRequest(sql, null);
		} else if (!oldUpdate.equals(updatedDate)){			//������ ����, �� ��������
			sql = "UPDATE prj1.ljusers SET `updated`='"+dateFormat.format(updatedDate)+"' " +
					"WHERE `name` = '"+username+"';";
			obtainEntries(oldUpdate, updatedDate);
			result = dataBaseRequest(sql, null);
		} else {											//������ ���� � �� �������� 
			obtainEntries(updatedDate, updatedDate);
			result = true;
		}
		return result;
	}
	
	public void obtainEntries(Date startDate, Date endDate){
		GregorianCalendar clndr = new GregorianCalendar();
		clndr.setTime(endDate);
		int aYear = clndr.get(GregorianCalendar.YEAR);
		int aMonth = clndr.get(GregorianCalendar.MONTH)+1;
		clndr.setTime(startDate);
		while (aYear >= clndr.get(GregorianCalendar.YEAR)){
			obtainMonthEntries(aYear, aMonth);
			if (aYear == clndr.get(GregorianCalendar.YEAR) & aMonth == clndr.get(GregorianCalendar.MONTH)) break;
			aMonth -= 1;
			if (aMonth == 0) {
				aMonth = 12;
				aYear -= 1;
			}
		}
		System.out.println("Username "+username+" records was inserted at "+new Date());
	}
	
	private void obtainMonthEntries(int year, int month){
		String strYear = String.valueOf(year);
		String strMonth = String.valueOf(month);
		if (strMonth.length()<2) strMonth = "0"+strMonth;
		Document doc = obtainHtmlText("/"+strYear+"/"+strMonth+"/?format=light");	
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
        boolean noComments = false;
        
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
        		if (i++%2 == 0) { 										//������ ������ ����� 
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
        			try {
						dateTime = new SimpleDateFormat("yyyy.MM.dd'T'K:mm a").
								parse(strYear+"."+strMonth+"."+strDay+"T"+time.toString());
					} catch (ParseException e) {
						System.err.println("SimpleDateFormat ParseException Unable to parse \""+strYear+"."+strMonth+"."+strDay+"T"+time.toString()+"\"");
						dateTime = new GregorianCalendar(1999, 0, 1, 0, 0, 0).getTime();
					}
        		}
        		else{													//������ ������ - ���������, ������, ���������� ���������
        			post_id = tt.html();
        			title = "";
        			commentsCount = 0;
        			String[] buffer = tt.text().split(" ");
        			if (buffer.length == 1) {							//��������� ���� �����, ��������� ����
        				noComments = true;
        				commentsCount = 0;
        				title = tt.select("a[href]").text();
        			} else 	if (buffer.length > 1) { 					//���� ������ 1 ����� 
            			if (buffer[buffer.length-1].equals("reply")) { 	//����� ���� ������� 
            				commentsCount = 1;
            				title = tt.select("a[href]").text();
            			} else if (buffer[buffer.length-1].equals("replies")) {
            				try {
            					commentsCount = Integer.parseInt(buffer[buffer.length-2]); //������ � ����� ����� - ����������
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
        	        String sql = "INSERT INTO prj1.entries(`username`, `post_id`, `time`, `title`, `comments`) " +
        	        		"VALUES ('"+username+"','"+post_id+"','"+new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(dateTime)+
        	        		"', ? ,'"+commentsCount+"');";
        	        String[] prepared = {title};
        	        dataBaseRequest(sql, prepared);
        	        obtainPost(post_id);
        	        if (!noComments){
        	        	try {
            	        	new Comments(username, post_id);
						} catch (Exception e) {
							System.out.println("problems with comments");
						}        	        	
        	        }
        		}
        	}
        }
		
	}
	
	public void obtainPost(String post_id){
		Document doc = obtainHtmlText("/"+post_id+".html?format=light");
		String text = doc.getElementsByClass("b-singlepost-body").text();
		System.out.println(new Date() + " - "+ text);
		String[] prepared = {text};
        String sql = "INSERT INTO prj1.posts(`username`, `text`, `post_id`) " +
        		"VALUES ('"+username+"',?,'"+post_id+"');";
        dataBaseRequest(sql, prepared); 
        
	}
	
	public static void main(String[] args) {
		System.out.println(new Date());
		String[] users = "mi3ch ".split(" ");
		for (String s : users) 
			new LJUser(s);
		System.out.println(new Date());
		
	}

}