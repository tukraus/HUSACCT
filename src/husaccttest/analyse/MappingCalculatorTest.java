package husaccttest.analyse;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import husacct.analyse.task.reconstruct.bruteForce.MappingCalculator;

public class MappingCalculatorTest {

	List<Integer> listSU = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
	static int numberOfGroups;
	static MappingCalculator mappingCalculator;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mappingCalculator = new MappingCalculator();
		numberOfGroups = 3;
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
	public void test() {
	}


}
