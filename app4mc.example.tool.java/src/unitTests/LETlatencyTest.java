package unitTests;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.List;
import java.io.File;
import java.math.BigInteger;

import org.eclipse.app4mc.amalthea.model.Amalthea;
import org.eclipse.app4mc.amalthea.model.AmaltheaFactory;
import org.eclipse.app4mc.amalthea.model.EventChain;
import org.eclipse.app4mc.amalthea.model.Time;
import org.eclipse.app4mc.amalthea.model.TimeUnit;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaLoader;
import org.eclipse.app4mc.amalthea.model.util.TimeUtil;
import org.eclipse.emf.common.util.EList;
//import org.junit.jupiter.api.Test;
import org.junit.Test;

import app4mc.example.tool.java.LoadModifySaveExample;

public class LETlatencyTest {

	@Test
	public void test() {
		//fail("Not yet implemented");
		final File inputFile = new File("model-input/ChallengeModel_withCommImplementationTypev082.amxmi");
		Amalthea model = AmaltheaLoader.loadFromFile(inputFile);
		EList<EventChain> eventList = model.getConstraintsModel().getEventChains();
		String time_1[] = {"20ms", "112ms", "52700us"};
		int i=0;
		for (EventChain effectChain : eventList) {
		Time Latency = LoadModifySaveExample.findLETChainLatency(model, effectChain);
		assertEquals(time_1[i], TimeUtil.timeToString(Latency));
		i++;
		}

}
}
