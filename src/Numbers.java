/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author essam
 */
public class Numbers {

	private static double getGaussian(double aMean, double aVariance) {

		java.util.Random fRandom = new java.util.Random();
		return aMean + fRandom.nextGaussian() * aVariance;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		// int num = 2023;
		// System.out.println(Math.pow(2, Math.floor(Math.log(num) /
		// Math.log(2))));
		//
		// int pow2 = Integer.highestOneBit(num);
		// if (num > pow2) {
		// pow2*=2;
		// System.out.println(pow2);
		// }

		for (int idx = 1; idx <= 100; ++idx) {
			System.out.println("Generated : " + getGaussian(0, 1));
		}
	}

}
