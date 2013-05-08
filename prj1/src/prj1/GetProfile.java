package prj1;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;

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
import org.jsoup.select.Elements;

public class GetProfile {

	public static boolean getProfile(String username) throws ClientProtocolException, IOException, SQLException {
		boolean result = false;
		HttpHost targetHost = new HttpHost(username+".livejournal.com", 80, "http");
		DefaultHttpClient httpclient = new DefaultHttpClient();
		BasicHttpContext localcontext = new BasicHttpContext();
		HttpGet httpget = new HttpGet("/profile");
		HttpResponse httpresp = httpclient.execute(targetHost, httpget, localcontext);
        HttpEntity entity = httpresp.getEntity();
        String html_text = new String();
        if (entity != null)
            html_text = EntityUtils.toString(entity, "UTF-8");
        Document doc = Jsoup.parse(html_text,"UTF-8");
        Elements content = doc.getElementsByClass("b-account-level");
        Map<String,GregorianCalendar> userDates = null;
        if (!content.text().isEmpty())
        	userDates=Chernovik.getDates(content.text());
		Date dateCreated = userDates.get("dateCreated").getTime();
		Date dateUpdated = userDates.get("dateUpdated").getTime();
		result = insertProfile(username, dateCreated, dateUpdated);
		return result;
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException, SQLException {
//		String[] users = "energizerr, eng_in, engraverrrr, enotokrowka, eot_71, eqq, erandl, erbeforscher, erechteyon, eredraen, ericblack, erie_rainell, eriklobakh, eroemin, erovobian, erroerrare, ertree, ervol, erzhu, es78, esash, escopar, esen_ya, eska, eskir, esmundo, estartzeva, estrecho, estrema5ura, eta_xlorka, etar_orc_volha, etotam, eugen_murashov, eugene_rom, eugene_titov, eugeneus1, eurotrach, euthanasia7, eva_kk, eva_medved, evataran, evdimir, eveline_shtrix, evg655, evg_ko, evg_pavlenko, evgenerrr, evgenivs, evgenyi51, evgol, evil_power, evlampia, evmitrofanov, evrom, evtushenko, ewgen1410, exile_sv, exkkalibur, exponline, exprabbit, ext_659462, exuser, eyelog, eyes_w1de_shut, eyma_sempai, ezhuneponyatno, ezrahovkin, f_i_x_y, fa, fabrichnova, fadek, faf2000, fagot99, falling_towers, fan-forum.blogspot.com, fandorin_k, fanforum, fannyfox, fantogiro, fanya_krasota, farouttosea, faust112, fedma, feduta, feiya_skazok, fel_in, fenix_party, fentiflu_snka, feodor_e, feofan777, feofan79, ferapont66, fes_by, fess_kiev, feuer81, fieldman747, figaro72, figaro_bender, filatenkov, files-4u.com, fili2pov, filisof, filofilm, filosofinja, finderin, finderrus, finno_marina, finnskij, fionochka, fire_foxi, first_scum, fish_hunter, fish_one, fisher_andrew, fitulina, fiyaso, fl1nt88, flackelf, flagman1980, flashofeternity, fleur_alina, flexiblegfx, flickering4, flipp0909, floralinka, florart, fly_dariya, flyer_, flyerunderfire, fmn74, fochlu, foggy_jan, fokova_helena, folkvald, foma_light, fon_harry, fonbrd, fondatrice_12, fooxers, for_fill_one, ford40, format26_kubik, formatcde, forresterall, fort_i_ko, foto6x7, foto_business, foto_free, fotoabc, fotoankh, fotoboldkp, fotofaust, fotofond2012, fotohaus, fotokrav, fotokurt, fotomasterskiye, fotorss, fotoshkola_spb, fotoslet, fotosoyuz_p5, fox_morarue, fox_rl, fr_am, frabook, fraiselle, frajsoul, fraktalist, francferdinand, franziosif, frater_tuk, frau_wolfin, freaky_franky, free_raider, freecricket, freetiger, freidd, frekensnork86, fridanutaya, froged55, froussard, frozen_eyes1976, frozys, fuck_sanchos, fuckmylj, fudzivara56, funneyrawl, futuroom, fyodor_fish, g0rky_mitich, g256, g4meb1t, g_bel, g_e_d_e_o_n, gaad79, gadsjl_7, gaduka_alenka, gaga_guga, gagarin0460, gagarin12, gal4enok_me, gal4ona87, gala3333, galahad_ru, galahovv, galan05, galataliya, galaxy_rock, galiaf, galili, galiziaforever, galyzin, ganfall, gans_gunner, gardist, garigr, garikkab, garmash14, garum_1936, garykate, gaullic, gav_1988, ge_rus001, gedas, gedda_gabler, gedygold, geers, gegemonisotorin, gelavasadze, geleopagot, gelievna, gelio_nsk, geliy, gelot, gelu_kardash, gemcag, geminianoh, geminot, gemoglobinius, genamikheev, gence197420, gendol, general_kosmosa, genius029, genkor, gennady_valya, genntal, genosse_krach, genosse_u, genrich_william, gentelev, gentlewolff, genuzzz, georg_t, georgy_1970, georgysphoto, geostrannik, gera_gerka, gerasim666666, germanarich, geronimo73, geshpanez, ggoriy, ggtops, gianes, giant_vision, gidal, gigakster, gignomai, gillederais, gilliermo, gimallai, ginddd, gingerry, giovinetta, girkoav, gistory, giterleo, gitikun, giulietta_83, gk_bang, glabuchie, glamurnaya_fifa, glas_naroda, glasha_yu, glasha_zast, glavholod, glavlinza_ru, glavmech, glaza_otkroj, glaza_vraga, glazastik90, gleb2009, gleb_nsk, glebspas, gliptika, glitzfrau, glockmaster, glubokov95, glumurka, gmaksimov, gnotprom, gocel, godfrua, godzil, goguin, gollandiya68, golodnoff, goloshin, golovach_igor, gomer_kucha, gonduras1, gonop, good_mixer, goodwine, gooodmoood, goorjev, gora85, goramo, gorbach4, gordyian, gorgeousclaire, goritruba, gorkyagent, gormlaith, gornyak2000, gorodgeroy, gorojane_tv, gorojanin_iz_b, gosha_bullit, goshstar, gotenkopf, gothicvalera, gothrom, gotva, goutsoullac, govna_telega, gr_mavrov, gr_n_tereshkov, grad_spb, graf_alter, gramobdimis, grandpajury, gray_at_theras, gray_ural, grayowl, greamreaper, greatgonchar, greba007, greeb_2003, green410, green_cow, greenchelman, greenknee, greennorth, greentroll, greenwashery, greg7greg, gregory_knyazev, gri_ol, gribochell, griffonkliff, grimster, grinnzli, ".split(", ");
//		int i = 0;
//		System.out.println(new GregorianCalendar().getTime());
//		for (String s : users)
//			if (!s.contains(" "))
//				if (getProfile(s))
//					i++;
//		System.out.println("Inserted "+i+" records from "+users.length);
//		System.out.println(new GregorianCalendar().getTime());
		getProfile("prommitey");
	}

	public static boolean insertProfile(String username, Date dateCreated, Date dateUpdated) throws SQLException {
		boolean result = false;
        Connection          conn  = null;
        PreparedStatement   pstmt = null;
        Properties connInfo = new Properties();
        connInfo.put("characterEncoding","UTF8");
        connInfo.put("user", "user");
        connInfo.put("password", "password123");
        conn = DriverManager.getConnection("jdbc:mysql://192.168.1.36/?", connInfo);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String sql = "INSERT INTO prj1.ljusers(`name`, `created`, `updated`) " +
        		"VALUES ('"+username+"','"+dateFormat.format(dateCreated)+"','"+dateFormat.format(dateUpdated)+"');";
        pstmt = conn.prepareStatement(sql);
        try {
            pstmt.execute();
            result = true;
		} catch (SQLException e) {
			System.out.println(e);
		}
        return result;
	}
	
}
