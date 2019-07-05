/**
 * 
 */
package social_pulse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.DataHandler;

/**
 * @author essam sed 's/\"//g' file.txt
 */
public class main_split_social_data {

	/**
	 * @param args
	 */
	private static final Pattern TAG_REGEX = Pattern.compile(
			"\\{geometry:\\{type:Point,coordinates:\\[(\\d*\\.?\\d*),(\\d*\\.?\\d*)\\]\\},timestamp:(\\d+),user:[0-9a-zA-Z]+\\}");

	private static final Pattern TAG_REGEX_1 = Pattern
			.compile("\\[(\\d*\\.?\\d*),(\\d*\\.?\\d*)\\]\\},timestamp:(\\d+),user:[0-9a-zA-Z]+");

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// DataHandler.read_csv(path, seperator)
		String path = args[0];
		String out_path = args[1];
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		List<String[]> segments = new ArrayList<>();
		Hashtable<String, Hashtable<Long, String[]>> usrs = new Hashtable<>();
		BufferedReader reader;
		try {
			Reader isreader = new InputStreamReader(new FileInputStream(path), "utf-8");
			reader = new BufferedReader(isreader);

			String line;
			while ((line = reader.readLine()) != null) {
				// line.replaceAll("[", "\"").replaceAll("]", "\"");

				// if (line.isEmpty()) {
				// continue;
				// }
				// segments.add(line.trim().split(seperator));
				final Matcher matcher = TAG_REGEX_1.matcher(line);
				while (matcher.find()) {

					String[] s = matcher.group(0).replaceAll("\\[", "").replaceAll("\\]", "")
							.replaceAll("timestamp:", "").replaceAll("user:", "").replaceAll("\\}", "")
							.replaceAll("\\}", "").split(",");

					String rec = s[3] + s[2] + s[0] + s[1];
					long unix_epoch_date = Long.parseLong(s[2]);
					Date ts = new Date(unix_epoch_date * 1000);
					String d = df.format(ts).toString();
					if (usrs.containsKey(s[3])) {
						Hashtable<Long, String[]> t = usrs.get(s[3]);
						t.put(unix_epoch_date, new String[] { s[3], d, s[1], s[0] });
						usrs.replace(s[3], t);
					} else {
						Hashtable<Long, String[]> t = new Hashtable<>();

						t.put(unix_epoch_date, new String[] { s[3], d, s[1], s[0] });
						usrs.put(s[3], t);
					}
					// segments.add(new String[] { s[3], s[2], s[0], s[1] });
					// System.out.println(rec);
				}

			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(main_split_social_data.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(main_split_social_data.class.getName()).log(Level.SEVERE, null, ex);
		}

		System.out.println("number of users in study:\t" + usrs.size());
		usrs.entrySet().stream().forEach((entry) -> {
			String usr = entry.getKey();
			Hashtable<Long, String[]> t = entry.getValue();
			// average one check-in per week = 8 per 2 months
			List<Long> v = new ArrayList<Long>(t.keySet());
			Collections.sort(v);

			for (Long d : v) {
				String[] chckin = t.get(d);
				segments.add(chckin);
			}
			// if (t.size() > 8) {
			// System.out.println(usr + "\t" + t.size());
			// segments.addAll(t);
			// }
		});
		DataHandler.write_csv(segments, out_path);
	}

}
