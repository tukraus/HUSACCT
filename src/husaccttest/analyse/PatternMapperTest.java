package husaccttest.analyse;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import husacct.analyse.task.reconstruct.PatternMapper;

public class PatternMapperTest {
	
	private static PatternMapper pm;
	private static int testSlots;
	private static String[] testPackageList;
	
	@BeforeClass
	public static void setUp() {
		testSlots = 3;
		testPackageList = new String[] { "main.package1", "main.package2", "main.package3", "additional.package1" };
		pm = new PatternMapper(testSlots, testPackageList);
	}
	
	@Test
	public void testConstructor() {
		assertEquals("NUMBER OF SLOTS INCORRECT. ", testSlots, PatternMapper.getSlots());
		assertEquals("NUMBER OF PACKAGES INCORRECT. ", testPackageList.length, pm.getPackageList().length);
	}
	
	@Test(timeout = 2000)
	public void testNumberOfPermutations() {
		assertEquals("INCORRECT NUMBER OF PERMUTATIONS. ", factorial(testPackageList.length) / factorial(testPackageList.length - testSlots),
				pm.getPermutations().length);
	}
	
	// Switch PatternMapper.permute to public to test.
	// @SuppressWarnings("static-access")
	// @Test(timeout = 2000)
	// public void testPermute() {
	// int[] testCombination = new int[testSlots + 1];
	// for (int i = 0; i < testSlots + 1; i++) {
	// testCombination[i] = i;
	// }
	// assertEquals("INCORRECT NUMBER OF PERMUTATIONS. ", factorial(testCombination.length), pm.permute(testCombination).size());
	// }
	
	private int factorial(int n) {
		int fact = 1;
		for (int i = 1; i <= n; i++) {
			fact *= i;
		}
		return fact;
	}
	
}
