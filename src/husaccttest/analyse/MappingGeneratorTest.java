package husaccttest.analyse;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import husacct.analyse.task.reconstruct.MappingGenerator;

public class MappingGeneratorTest {
	private static MappingGenerator mg;
	private static int testSlots;
	private static String[] testPackageList;
	
	@BeforeClass
	public static void setUp() {
		testSlots = 3;
		testPackageList = new String[] { "package1", "package2", "package3", "package4", "package5", "package6", "package7" };
		mg = new MappingGenerator(testSlots, testPackageList);
	}
	
	@Test(timeout = 20)
	public void testNumberOfPermutations() {
		assertEquals("INCORRECT NUMBER OF PERMUTATIONS. ", factorial(testPackageList.length) / factorial(testPackageList.length - testSlots),
				mg.getPermutations().length);
	}
	
	// Switch MappingGenerator.permute to public to test.
	// @SuppressWarnings("static-access")
	// @Test(timeout = 2000)
	// public void testPermute() {
	// int[] testCombination = new int[testSlots + 1];
	// for (int i = 0; i < testSlots + 1; i++) {
	// testCombination[i] = i;
	// }
	// assertEquals("INCORRECT NUMBER OF PERMUTATIONS. ", factorial(testCombination.length), mg.permute(testCombination).size());
	// }
	
	private int factorial(int n) {
		int fact = 1;
		for (int i = 1; i <= n; i++) {
			fact *= i;
		}
		return fact;
	}
}
