package unitTests;

import static org.junit.Assert.*;
import app4mc.example.tool.java.LoadModifySaveExample;
import java.io.File;
import java.util.List;

import org.eclipse.app4mc.amalthea.model.Amalthea;
import org.eclipse.app4mc.amalthea.model.EventChain;
import org.eclipse.app4mc.amalthea.model.Runnable;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaLoader;
import org.eclipse.emf.common.util.EList;
import org.junit.Test;

public class GetRunnablesfromEventChainTest {

	@Test
	public void test() {
		//fail("Not yet implemented");
		final File inputFile = new File("model-input/ChallengeModel_withCommImplementationTypev082.amxmi");
		 String FinaloutputExpected[] = {"Runnable_10ms_149", "Runnable_10ms_243","Runnable_10ms_272","Runnable_10ms_107","Runnable_100ms_7","Runnable_10ms_19","Runnable_2ms_8","Runnable_sporadic_700us_800us_3","Runnable_2ms_3","Runnable_50ms_36"};
         
		Amalthea model = AmaltheaLoader.loadFromFile(inputFile);
		if (model == null) {
			System.out.println("Error: No model loaded!");
			return;
		}
		int runnable_count=model.getSwModel().getRunnables().size();
		int i=0;
		// ***** Modify *****
		EList<Runnable> runnables = model.getSwModel().getRunnables();
		EList<EventChain> eventList = model.getConstraintsModel().getEventChains();
		List<Runnable> runnablewithoutduplicate = null;
		for(EventChain event : eventList)
		{
			System.out.println("Even chain :  "+event.getName());
		 runnablewithoutduplicate = LoadModifySaveExample.getRunnablesFromEventChain(event, runnables);
		for(Runnable R:runnablewithoutduplicate)
		{
			assertEquals(R.getName(),FinaloutputExpected[i]);
			i++;
			//System.out.println(R.getName());
		}
		}
		
		System.out.println("List of Runnable is as expected");
		
	}

}
