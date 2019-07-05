package social_pulse.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class main_date {

	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String s = "2015-04-29 17:32:39";
		Date date = new Date(Long.parseLong("1430328759")*1000) ;
		try {
			System.out.println(df.format(date).toString());
			System.out.println(df.parse(s).getTime()+"\t1430328759");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
