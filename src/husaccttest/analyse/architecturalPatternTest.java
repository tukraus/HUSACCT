package husaccttest.analyse;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import husacct.ServiceProvider;
import husacct.analyse.task.reconstruct.patterns.BrokerPattern_CompleteFreedom;
import husacct.analyse.task.reconstruct.patterns.BrokerPattern_FreeRemainder;
import husacct.analyse.task.reconstruct.patterns.BrokerPattern_RequesterInterface;
import husacct.analyse.task.reconstruct.patterns.BrokerPattern_RestrictedRemainder;
import husacct.analyse.task.reconstruct.patterns.LayeredPattern_CompleteFreedom;
import husacct.analyse.task.reconstruct.patterns.LayeredPattern_FreeRemainder;
import husacct.analyse.task.reconstruct.patterns.LayeredPattern_IsolatedInternalLayers;
import husacct.analyse.task.reconstruct.patterns.LayeredPattern_RestrictedRemainder;
import husacct.analyse.task.reconstruct.patterns.MVCPattern_CompleteFreedom;
import husacct.analyse.task.reconstruct.patterns.MVCPattern_ControllerInterface;
import husacct.analyse.task.reconstruct.patterns.MVCPattern_FreeRemainder;
import husacct.analyse.task.reconstruct.patterns.MVCPattern_RestrictedRemainder;
import husacct.analyse.task.reconstruct.patterns.Pattern;
import husacct.define.IDefineService;

public class architecturalPatternTest {
	static Pattern currentPattern = null;
	static IDefineService defineService;
	static ArrayList<Pattern> list = new ArrayList<Pattern>();
	static Iterator<Pattern> it;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defineService = ServiceProvider.getInstance().getDefineService();
		addPatternTypes();
		it = list.iterator();
	}

	private static void addPatternTypes() { // Add new pattern classes here.
		try {
			list.add(LayeredPattern_CompleteFreedom.class.newInstance());
			list.add(LayeredPattern_FreeRemainder.class.newInstance());
			list.add(LayeredPattern_IsolatedInternalLayers.class.newInstance());
			list.add(LayeredPattern_RestrictedRemainder.class.newInstance());
			list.add(MVCPattern_FreeRemainder.class.newInstance());
			list.add(MVCPattern_CompleteFreedom.class.newInstance());
			list.add(MVCPattern_ControllerInterface.class.newInstance());
			list.add(MVCPattern_FreeRemainder.class.newInstance());
			list.add(MVCPattern_RestrictedRemainder.class.newInstance());
			list.add(BrokerPattern_RequesterInterface.class.newInstance());
			list.add(BrokerPattern_FreeRemainder.class.newInstance());
			list.add(BrokerPattern_CompleteFreedom.class.newInstance());
			list.add(BrokerPattern_RequesterInterface.class.newInstance());
			list.add(BrokerPattern_RestrictedRemainder.class.newInstance());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNumberOfRules() {
		while (it.hasNext()) {
			currentPattern = (Pattern) it.next();
			currentPattern.insertPattern();
			assertEquals("INCORRECT NUMBER OF RULES FOR: " + currentPattern, currentPattern.getNumberOfRules(), defineService.getDefinedRules().length);
			System.out.println(currentPattern);
			defineService.resetDefinedArchitecture();
		}

	}

}
