package app4mc.example.tool.java;

import java.io.File;
import java.util.List;

import org.eclipse.app4mc.amalthea.model.Amalthea;
import org.eclipse.app4mc.amalthea.model.AmaltheaFactory;
import org.eclipse.app4mc.amalthea.model.CallGraph;
import org.eclipse.app4mc.amalthea.model.GraphEntryBase;
import org.eclipse.app4mc.amalthea.model.HWModel;
import org.eclipse.app4mc.amalthea.model.HwFeature;
import org.eclipse.app4mc.amalthea.model.HwFeatureCategory;
import org.eclipse.app4mc.amalthea.model.ProcessChain;
import org.eclipse.app4mc.amalthea.model.ProcessingUnit;
import org.eclipse.app4mc.amalthea.model.Runnable;
import org.eclipse.app4mc.amalthea.model.RunnableAllocation;
import org.eclipse.app4mc.amalthea.model.Task;
import org.eclipse.app4mc.amalthea.model.Time;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaLoader;
import org.eclipse.app4mc.amalthea.model.util.HardwareUtil;
import org.eclipse.app4mc.amalthea.model.util.InstructionsUtil;
import org.eclipse.app4mc.amalthea.model.util.ModelUtil;
import org.eclipse.app4mc.amalthea.model.util.RuntimeUtil;
import org.eclipse.app4mc.amalthea.model.util.TimeUtil;
import org.eclipse.app4mc.amalthea.model.util.RuntimeUtil.TimeType;
import org.eclipse.emf.common.util.EList;

public class demo {
	public static void main(String[] args) {
		// example: relative path
		final File inputFile = new File("model-input/democar.amxmi");

		// ***** Load *****
		Amalthea model = AmaltheaLoader.loadFromFile(inputFile);

		// Get a list of all processing units and of all runnables
		List<ProcessingUnit> processingUnits = HardwareUtil.getModulesFromHWModel(ProcessingUnit.class, model);
		EList<Runnable> runnables = model.getSwModel().getRunnables();
		//EList<Task> tasks = model.getSwModel().getTasks();
/*		EList<RunnableAllocation> RunAlloc = model.getMappingModel().getRunnableAllocation();
		EList<ProcessChain> processes = model.getSwModel().getProcessChains();
		System.out.println(processes);
		for (Task task:tasks) {
			EList<GraphEntryBase> grahent = task.getCallGraph().getGraphEntries();
			//System.out.println(grahent);
			
			
		}*/
		HwFeatureCategory instructionCategory = getOrCreateInstructionsCategory(model);
		Time totalTime = AmaltheaFactory.eINSTANCE.createTime();
		for (Runnable runnable : runnables) {
			System.out.println("Label Accesses for Runnable " + runnable.getName() + ": ");
			
			for (ProcessingUnit pu : processingUnits) {
				List<HwFeature> instructionFeature = InstructionsUtil.getFeaturesOfCategory(pu.getDefinition(),
						instructionCategory);
				Time time = RuntimeUtil.getExecutionTimeForRunnable(runnable, TimeType.WCET, pu, instructionFeature,
						null);
				totalTime = TimeUtil.addTimes(totalTime, time);
				System.out.println("* ExecutionTime on " + pu.getName() + ": " + time);
				System.out.println("* Total ExecutionTime on " + pu.getName() + ": " + totalTime);
			}
		}
		
	}

	public static final String INSTRUCTIONS_CATEGORY_NAME = "Instructions";

	/**
	 * Fixed version of InstructionUtil.getOrCreateInstructionsCategory
	 * 
	 * Note to Bosch: Comparing String Objects in Java has never worked using ==
	 * ;-) See also:
	 * https://stackoverflow.com/questions/513832/how-do-i-compare-strings-in-java
	 * 
	 * @param model
	 * @return
	 */
	public static HwFeatureCategory getOrCreateInstructionsCategory(Amalthea model) {
		HWModel hwModel = ModelUtil.getOrCreateHwModel(model);
		for (HwFeatureCategory category : hwModel.getFeatureCategories()) {
			// if (category.getName() == INSTRUCTIONS_CATEGORY_NAME) return category;
			if (null != category.getName() && category.getName().equals(INSTRUCTIONS_CATEGORY_NAME))
				return category;
		}

		// create missing category
		HwFeatureCategory newCategory = AmaltheaFactory.eINSTANCE.createHwFeatureCategory();
		newCategory.setName(INSTRUCTIONS_CATEGORY_NAME);
		hwModel.getFeatureCategories().add(newCategory);

		return newCategory;
	}
}
