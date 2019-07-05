
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import utils.DataHandler;

public class Rename_quran {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dir = args[0];
		String sura_names = args[1];

		List<String[]> sura = DataHandler.read_csv(sura_names, DataHandler.COMMA_SEP);
		Hashtable<Integer, String> sura_order = new Hashtable<>();

		sura.stream().forEach(so -> {
			int i = Integer.parseInt(so[0]);
			sura_order.put(i, so[1]);
		});

		// read all directories and subdirectories
		List<File> dirs = new ArrayList<File>();
		File[] subdirs = new File(dir).listFiles(File::isDirectory);
		dirs.addAll(Arrays.asList(subdirs));
		for (File d : subdirs) {
			File[] subsubdirs = d.listFiles(File::isDirectory);
			dirs.addAll(Arrays.asList(subsubdirs));
		}

		dirs.stream().forEach(d -> {
			List<String> files = DataHandler.listFilesForFolder(d);
			files.stream().forEach(s -> {

				String path = s.substring(0, s.lastIndexOf("/") + 1);
				String ext = s.substring(s.lastIndexOf("."));
				if (s.substring(s.lastIndexOf("/") + 1, s.lastIndexOf(".")).matches("-?\\d+(\\.\\d+)?")) {
					int fname = Integer.parseInt(s.substring(s.lastIndexOf("/") + 1, s.lastIndexOf(".")));

					System.out.println(path + "\t" + sura_order.get(fname) + "\t" + ext);
					String n_path = path + String.format("%03d", fname) + "-" + sura_order.get(fname) + ext;

					File file = new File(s);
					if (file.exists() && !file.isDirectory()) {
						File file2 = new File(n_path);

						boolean success = file.renameTo(file2);
					}
				}
			});
		});

		// List<String> files = DataHandler.listFilesForFolder(new File(dir));

	}

}
