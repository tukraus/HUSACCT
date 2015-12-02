package husaccttest.analyse;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import husacct.ServiceProvider;
import husacct.analyse.task.reconstruct.MVCPattern_FreeRemainder;
import husacct.analyse.task.reconstruct.Pattern;
import husacct.define.IDefineService;

public class architecturalPatternTest {
	static Pattern currentPattern = null;
	static IDefineService defineService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		currentPattern = new MVCPattern_FreeRemainder();
		defineService = ServiceProvider.getInstance().getDefineService();

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
	public void testPatternInsertion1() {
		currentPattern.insertPattern();
	}

	@Test
	public void testNumberOfRules() {
		assertEquals("INCORRECT NUMBER OF RULES", 8, defineService.getDefinedRules().length);
	}

}
