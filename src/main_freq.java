import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class main_freq {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String[] vpath = { "31461101_#1", "31461097_#0", "31461101_#1", "30674520_#1", "109129945_#5", "232654723_#23",
				"107904516_#51", "107904516_#64", "107904516_#64", "107904516_#64", "107904516_#64", "107904516_#74",
				"107904516_#82", "107904516_#84", "246160084_#5", "172809966_#1", "172809966_#2", "114308145_#0",
				"114308171_#2", "114308145_#0", "108840666_#7", "108840666_#6", "111829528_#2", "111829525_#2",
				"111829607_#4", "108891346_#8", "167609835_#9", "108891346_#8", "178731421_#18", "168657159_#1",
				"178731421_#18", "366052311_#20", "44644651_#11", "150122508_#6", "167474496_#9", "167474496_#10",
				"167473411_#6", "167473411_#0", "27294382_#9", "27087853_#11", "27087858_#1", "27087853_#11" };

		List<String> lvpath = new ArrayList<>(Arrays.asList(vpath));
//		System.out.println(lvpath);
		// Set<String> mySet = new HashSet<>(lvpath);
		for (String s : vpath) {
			int count = Collections.frequency(lvpath, s);
			for (int i = 0; i < count - 1; i++) {
				lvpath.remove(lvpath.lastIndexOf(s));
			}
			// System.out.println(s + " " +Collections.frequency(lvpath,s));

		}

		String[] nvpath = lvpath.toArray(new String[lvpath.size()]);
		
		for (String s : vpath) {
			 System.out.println(s + " " +Collections.frequency(lvpath,s));

		}

	}

}
