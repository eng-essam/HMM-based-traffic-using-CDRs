package validation.gps.comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * 
 * @author essam
 * @date Sun 31 Jul 20:06:24 EET 2016
 * 
 */
public class Map_matching_change_alignment_threshold {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double alignment_threshold = Double.parseDouble(args[0]);
		double sampling_time_threshold = Double.parseDouble(args[1]);
		String output_file_path = args[2];

		Map_matching_schulze mms = new Map_matching_schulze();
		List<String> divergences = new ArrayList<>();
		List<String> alignments = new ArrayList<>();

		double vit_divergences = 0;
		double vit_alignments = 0;

		double mms_divergences = 0;
		double mms_alignments = 0;

		double count = 0;

		for (int ii = 0; ii <= 18; ii++) {
			int index = ii;
			System.out.println("Current track segment is " + index);

			if ( index != 8 && index != 11 && index != 17) {

				double[] viterbi_residuals = mms.read_residuals(output_file_path.replace(".csv",
						"_" + index + "_viterbi_residuals_sampling_" + sampling_time_threshold + "_sec.csv"));
				// Diveregence is the average alignment distances ...
				double viterbi_div = DoubleStream.of(viterbi_residuals).sum() / viterbi_residuals.length;
				double viterbi_align_ratio = mms.get_alignment_ratio(viterbi_residuals, alignment_threshold);

				vit_divergences += viterbi_div;
				vit_alignments += viterbi_align_ratio;

				double[] mms_residuals = mms.read_residuals(output_file_path.replace(".csv",
						"_" + index + "_mms_residuals_sampling_" + sampling_time_threshold + "_sec.csv"));

				double mms_div = DoubleStream.of(mms_residuals).sum() / mms_residuals.length;
				double mms_align_ratio = mms.get_alignment_ratio(mms_residuals, alignment_threshold);

				mms_divergences += mms_div;
				mms_alignments += mms_align_ratio;

				divergences.add(viterbi_div + "," + mms_div);
				alignments.add(viterbi_align_ratio + "," + mms_align_ratio);
				// System.out.println("Finished ...");
				count++;
			}
		} // );

		divergences.add(vit_divergences / count + "," + mms_divergences / count);
		alignments.add(vit_alignments / count + "," + mms_alignments / count);

		mms.write_csv(divergences, output_file_path.replace(".csv", "_divergence_sampling_" + sampling_time_threshold
				+ "_sec_" + alignment_threshold + "_alignment_threshold.csv"));
		mms.write_csv(alignments, output_file_path.replace(".csv", "_alignment_ratio_sampling_"
				+ sampling_time_threshold + "_sec_" + alignment_threshold + "_alignment_threshold.csv"));
	}

}
