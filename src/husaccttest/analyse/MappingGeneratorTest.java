package husaccttest.analyse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import husacct.analyse.task.reconstruct.bruteForce.MappingGenerator;

public class MappingGeneratorTest {

	private static int testSlots;
	private static String[] testPackageList;

	@BeforeClass
	public static void setUp() {
		testSlots = 3;
		testPackageList = new String[] { "package1", "package2", "package3", "package4", "package5", "package6", "package7" };
	}

	@Test
	public void testNumberOfPermutations() {
		MappingGenerator mg = new MappingGenerator(testPackageList, testSlots);
		List<String> singleMapping = new ArrayList<String>();
		int countN = 0;
		assertNotNull(mg.next());
		countN++;
		ArrayList<List<String>> mappingList = new ArrayList<List<String>>();
		while (true) {
			singleMapping = mg.next();
			if (singleMapping == null) {
//				System.out.println("NO MORE MAPPINGS LEFT");
//				System.out.println(countN);
				break;
			} else {
				assertTrue("MAPPING LIST CONTAINS DUPLICATES", !mappingList.contains(singleMapping));
				mappingList.add(singleMapping);
//				System.out.println(singleMapping);
				countN++;
			}
		}
		assertEquals("INCORRECT NUMBER OF PERMUTATIONS: ", factorial(testPackageList.length) / factorial(testPackageList.length - testSlots), countN);
	}

	private int factorial(int n) {
		int fact = 1;
		for (int i = 1; i <= n; i++)
			fact *= i;
		return fact;
	}

}
