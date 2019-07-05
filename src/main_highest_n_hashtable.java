import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.TreeMap;

public class main_highest_n_hashtable {

	public static void main(String[] args) {

		Hashtable<String, Double> map = new Hashtable();
		ValueComparator bvc = new ValueComparator(map);
		TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);

		map.put("A", 19.5);
		map.put("B", 67.4);
		map.put("C", 57.4);
		map.put("D", 67.3);

		System.out.println("unsorted map: " + map);

		sorted_map.putAll(map);

		
		System.out.println("results: " + new ArrayList<>(sorted_map.keySet()).subList(0,2));
	}
}

class ValueComparator implements Comparator<String> {

	Hashtable<String, Double> base;

	public ValueComparator(Hashtable<String, Double> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return 1;
		} else {
			return -1;
		} // returning 0 would merge keys
	}

}
