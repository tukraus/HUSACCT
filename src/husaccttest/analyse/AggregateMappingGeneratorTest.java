package husaccttest.analyse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import husacct.analyse.task.reconstruct.bruteForce.AggregateMappingGenerator;

public class AggregateMappingGeneratorTest {

	@Test
	public void nextTest() {
		List<ArrayList<List<String>>> mappingList = new ArrayList<ArrayList<List<String>>>();
		String[] names = new String[] { "A", "B", "C", "D", "E", "F" };
		int numberOfGroups = 3;
		AggregateMappingGenerator mappingCalculator = new AggregateMappingGenerator(names, numberOfGroups, false, 10);
		List<List<String>> singleMapping;
		int count = 0;
		singleMapping = mappingCalculator.next(-2);
		assertNotNull(singleMapping);
		count++;
		while (true) {
			// if (count == 42)
			// singleMapping = mappingCalculator.next();
			// else
			singleMapping = mappingCalculator.next(-1);
			if (singleMapping == null) {
				// System.out.println("NO MORE MAPPINGS LEFT");
				// System.out.println("Count: " + count);
				break;
			} else {
				assertTrue("MAPPING LIST CONTAINS DUPLICATES", !mappingList.contains(singleMapping));
				mappingList.add(deepClone(singleMapping));
				count++;
			}
		}
		assertEquals("INCORRECT NUMBER OF MAPPINGS", 540, count);
		// System.out.println(mappingCalculator.getMappingMap(42));
		assertNotNull(mappingCalculator.getMappingMap(42));
	}

	@Test
	public void nextIncludingRemainderTest() {
		String[] names = new String[] { "package1", "package2", "package3", "package4", "package5", "package6","p7","p8" };
		int numberOfGroups = 3;
		AggregateMappingGenerator mappingCalculatorRemainder = new AggregateMappingGenerator(names, numberOfGroups, true, 10);
		List<List<String>> singleMapping;
		int count = 0;
		assertNotNull(mappingCalculatorRemainder.next(-2));
		count++;
		while (true) {
			if (count%2==0) {
			singleMapping = mappingCalculatorRemainder.next(-1);}
			else 
				singleMapping = mappingCalculatorRemainder.next(count);
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
		assertEquals("INCORRECT NUMBER OF MAPPINGS", 40824, count);
		assertNotNull(mappingCalculatorRemainder.getMappingMap(42));
	}

	private ArrayList<List<String>> deepClone(List<List<String>> listOfLists) {
		ArrayList<List<String>> cloneList = new ArrayList<List<String>>(listOfLists.size());
		ArrayList<String> clone;
		for (List<String> nestedList : listOfLists) {
			clone = new ArrayList<String>(nestedList.size());
			for (String element : nestedList)
				clone.add(element);
			cloneList.add(clone);
		}
		return cloneList;
	}
}
