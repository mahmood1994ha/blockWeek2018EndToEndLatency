package unitTests;

import static org.junit.jupiter.api.Assertions.*;

//import org.junit.jupiter.api.Test;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.app4mc.amalthea.model.Amalthea;
import org.eclipse.app4mc.amalthea.model.EventChain;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaLoader;
import org.eclipse.emf.common.util.EList;
import app4mc.example.tool.java.LoadModifySaveExample;

public class EndToEndImplicitTest {

	@Test
	public void test() {
		
		final File inputFile = new File("model-input/ChallengeModel_withCommImplementationTypev082.amxmi");
		Amalthea model = AmaltheaLoader.loadFromFile(inputFile);
		long latency1 = 0;
		long x[]= {11L,57L,19L};
		if (model == null) {
			System.out.println("Error: No model loaded!");
			return;
		}
		
		EList<EventChain> eventList = model.getConstraintsModel().getEventChains();
		int i=0;
		for(EventChain event: eventList)
		{
			latency1 = LoadModifySaveExample.EndToEndImplicit(model, event);
			System.out.println(latency1);
			assert(x[i] == latency1);
			System.out.println("the latnecy value is correct");
			i++;
		}
	}

}

