package husaccttest.analyse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import husacct.analyse.task.reconstruct.bruteForce.AggregateMappingGenerator;

public class AggregateMappingGeneratorTest {

	@Test
	public void nextTest() {
		String[] names = new String[] { "package1", "package2", "package3", "package4", "package5", "package6" };
		int numberOfGroups = 3;
		AggregateMappingGenerator mappingCalculator = new AggregateMappingGenerator(names, numberOfGroups, false);
		List<List<String>> singleMapping;
		int count = 0;
		assertNotNull(mappingCalculator.next());
		count++;
		while (true) {
			singleMapping = mappingCalculator.next();
			if (singleMapping == null) {
				// System.out.println("NO MORE MAPPINGS LEFT");
				// System.out.println("Count: " + count);
				break;
			} else {
				// System.out.println(singleMapping);
				count++;
				// System.out.println("Count: " + count);
			}
		}
		assertEquals("INCORRECT NUMBER OF MAPPINGS", 540, count);
	}

	@Test
	public void nextIncludingRemainderTest() {
		String[] names = new String[] { "package1", "package2", "package3", "package4", "package5", "package6" };
		int numberOfGroups = 3;
		AggregateMappingGenerator mappingCalculatorRemainder = new AggregateMappingGenerator(names, numberOfGroups, true);
		List<List<String>> singleMapping;
		int count = 0;
		assertNotNull(mappingCalculatorRemainder.next());
		count++;
		while (true) {
			singleMapping = mappingCalculatorRemainder.next();
			if (singleMapping == null) {
				// System.out.println("NO MORE MAPPINGS LEFT");
				// System.out.println("Count: " + count);
				break;
			} else {
				// System.out.println(singleMapping);
				count++;
				// System.out.println("Count: " + count);
			}
		}
		assertEquals("INCORRECT NUMBER OF MAPPINGS", 1560, count);
	}
}
