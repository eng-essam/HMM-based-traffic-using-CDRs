
public class removeInterpolation {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String vit = "--581402#1_interpolated_[0],--581402#1_interpolated_[0],108176218_interpolated_[3],108176218_interpolated_[4]";
		if(vit.contains("_interpolated_")){
			vit=vit.replaceAll("_interpolated_\\[[0-9]+\\]", "");
		}
		
		if(vit.contains("#")){
			vit=vit.replaceAll("#[0-9]+","");
		}
		
		System.out.println(vit);
	}

}
