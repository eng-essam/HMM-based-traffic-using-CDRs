import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import utils.DataHandler;
import utils.Vertex;

public class corridor_shape {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String towers_file_path = args[0];
		double scale=1000;
		Hashtable<Integer, Vertex> twrs = DataHandler.readTowers(towers_file_path);
		double sum=0;
		List<Vertex> vs = new ArrayList<>(twrs.values());
		for (int i = 0; i < vs.size()-1; i++) {
			Vertex v0 = vs.get(i);
			Vertex v1 = vs.get(i + 1);
			double d1 = DataHandler.geodistance(v0.x, v0.y, v1.x, v1.y, "m");
			double d2 = DataHandler.euclidean(v0.x, v0.y, v1.x, v1.y);
			sum+=d1 / d2;
			System.out.println(d1 + "\t" + d2 + "\t" + d1 / d2);
		}
		System.out.println(sum/(vs.size()-1));
		System.out.println(500/(sum/(vs.size()-1)));
		
		double d1 = DataHandler.geodistance(13.1, -14.9, 13.1+(500/(sum/(vs.size()-1))), -14.9+(500/(sum/(vs.size()-1))), "m");
		System.out.println(d1 );
	}

}
