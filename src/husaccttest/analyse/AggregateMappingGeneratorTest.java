package husaccttest.analyse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import husacct.analyse.task.reconstruct.bruteForce.AggregateMappingGenerator;

public class AggregateMappingGeneratorTest {
	static AggregateMappingGenerator mappingCalculator;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] names = new String[] { "package1", "package2", "package3", "package4", "package5", "package6"};
		int numberOfGroups = 3;
		mappingCalculator = new AggregateMappingGenerator(names, numberOfGroups, false);
	}

	@Test
	public void nextTest() {
		List<List<String>> singleMapping;
		int count = 0;
		assertNotNull(mappingCalculator.next());
		count++;
		while (true) {
			singleMapping = mappingCalculator.next();
			if (singleMapping == null) {
				 System.out.println("NO MORE MAPPINGS LEFT");
				 System.out.println("Count: " + count);
				break;
			} else {
				 System.out.println(singleMapping);
				count++;
				 System.out.println("Count: " + count);
			}
		}
		assertEquals("INCORRECT NUMBER OF MAPPINGS", 540, count);
	}
}
