package husaccttest.analyse;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import husacct.analyse.task.reconstruct.MappingCalculator;

public class MappingCalculatorTest {

	static List<Integer> list;
	static MappingCalculator mappingCalculator;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
		mappingCalculator = new MappingCalculator();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void findSublistsTest() {
		mappingCalculator.findAllSublists(list, 3);
		List<Integer> sublist = mappingCalculator.next();
		assertNotNull("MAPPING SUBLIST WAS NULL", sublist);
		assertEquals("INITIAL MAPPING SUBLIST NOT THE EXPECTED SIZE", 3, sublist.size());
		for (int i = 1; i < 99; i++) { // 99 = 7 choose 0 + 7 choose 1 + 7 choose 2 + 7 choose 3 + 7 choose 4.
			sublist = mappingCalculator.next();
			assertNotNull("MAPPING SUBLIST WAS NULL", sublist);
		}
		assertEquals("FINAL MAPPING SUBLIST NOT THE EXPECTED SIZE", 7, sublist.size());
	}

	@Test
	public void Test() {
		mappingCalculator.findAllSublists(list, 3);
		List<Integer> sublist = mappingCalculator.next();
		
	}
}
