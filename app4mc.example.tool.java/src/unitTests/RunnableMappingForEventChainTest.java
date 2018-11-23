package unitTests;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.eclipse.app4mc.amalthea.model.Amalthea;
import org.eclipse.app4mc.amalthea.model.AmaltheaFactory;
import org.eclipse.app4mc.amalthea.model.EventChain;
import org.eclipse.app4mc.amalthea.model.HwAccessElement;
import org.eclipse.app4mc.amalthea.model.HwDestination;
import org.eclipse.app4mc.amalthea.model.HwFeatureCategory;
import org.eclipse.app4mc.amalthea.model.ProcessingUnit;
import org.eclipse.app4mc.amalthea.model.Runnable;
import org.eclipse.app4mc.amalthea.model.Time;
import org.eclipse.app4mc.amalthea.model.TimeUnit;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaLoader;
import org.eclipse.app4mc.amalthea.model.util.HardwareUtil;
import org.eclipse.app4mc.amalthea.model.util.TimeUtil;
import org.eclipse.emf.common.util.EList;
import org.junit.Test;

import app4mc.example.tool.java.LoadModifySaveExample;


public class RunnableMappingForEventChainTest {

	@Test
	public void test() {
		//fail("Not yet implemented");
		final File inputFile = new File("model-input/ChallengeModel_withCommImplementationTypev082.amxmi");
        
		Amalthea model = AmaltheaLoader.loadFromFile(inputFile);
		if (model == null) {
			System.out.println("Error: No model loaded!");
			return;
		}
		String y[] = {"3259400ns","1267400ns","1027400ns"}; 
		int i=0;
		int runnable_count=model.getSwModel().getRunnables().size();
		EList<Runnable> runnables = model.getSwModel().getRunnables();
		EList<EventChain> eventList = model.getConstraintsModel().getEventChains();
		HwFeatureCategory instructionCategory =LoadModifySaveExample.getOrCreateInstructionsCategory(model);
		List<ProcessingUnit> processingUnits = HardwareUtil.getModulesFromHWModel(ProcessingUnit.class, model);
		Map<ProcessingUnit, Map<Runnable, Time>> mappingDataCustom = LoadModifySaveExample.testmapping(model, runnables,instructionCategory);
		

		
		for (EventChain event : eventList) {
			List<Runnable> runnablesFromEventChain =LoadModifySaveExample.getRunnablesFromEventChain(event,runnables);
			Map<ProcessingUnit, Map<Runnable, Time>> mappingDataRunnableSubSet = LoadModifySaveExample.runnableMappingForEventchain(mappingDataCustom,event);
			Map<ProcessingUnit, Map<HwAccessElement, Map<Runnable, Time>>> accessTimesForEventChain = LoadModifySaveExample.bestCaseAccessLatencyMapping(model,runnablesFromEventChain);
			System.out.println(event.getName() +" runnable subset: ");
			Time TotalExecutionTimeForEvent = AmaltheaFactory.eINSTANCE.createTime();
			for(Runnable runnable:runnablesFromEventChain) {
				//System.out.println("	*"+runnable.getName());
			}
			Time localExecutionTimeForEvent = AmaltheaFactory.eINSTANCE.createTime();
			for (ProcessingUnit procUnit: processingUnits) {
				for (Runnable runnableSubSet: runnablesFromEventChain) {
					Time executionTimeForEventChainRunnable = mappingDataRunnableSubSet.get(procUnit).get(runnableSubSet);
					localExecutionTimeForEvent = TimeUtil.addTimes(localExecutionTimeForEvent, executionTimeForEventChainRunnable);
					
				}
				TotalExecutionTimeForEvent= TimeUtil.addTimes(TotalExecutionTimeForEvent,localExecutionTimeForEvent);

			
			}
			//System.out.println(TotalExecutionTimeForEvent);
			//System.out.println(TimeUtil.timeToString(TotalExecutionTimeForEvent));
			assertEquals(y[i],TimeUtil.timeToString(TotalExecutionTimeForEvent));
			System.out.println("correct timing!");
			i++;
			
		}
		}
}
		
