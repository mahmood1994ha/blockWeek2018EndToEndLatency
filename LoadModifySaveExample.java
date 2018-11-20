
/**
 ********************************************************************************
 * Copyright (c) 2018 Robert Bosch GmbH.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Robert Bosch GmbH - initial API and implementation
 ********************************************************************************
 */

package app4mc.example.tool.java;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map.Entry;

import org.eclipse.app4mc.amalthea.model.AbstractEventChain;
import org.eclipse.app4mc.amalthea.model.Amalthea;
import org.eclipse.app4mc.amalthea.model.AmaltheaFactory;
import org.eclipse.app4mc.amalthea.model.Event;
import org.eclipse.app4mc.amalthea.model.EventChain;
import org.eclipse.app4mc.amalthea.model.EventChainItem;
import org.eclipse.app4mc.amalthea.model.Frequency;
import org.eclipse.app4mc.amalthea.model.FrequencyUnit;
import org.eclipse.app4mc.amalthea.model.HWModel;
import org.eclipse.app4mc.amalthea.model.HwAccessElement;
import org.eclipse.app4mc.amalthea.model.HwDestination;
import org.eclipse.app4mc.amalthea.model.HwFeature;
import org.eclipse.app4mc.amalthea.model.HwFeatureCategory;
import org.eclipse.app4mc.amalthea.model.Label;
import org.eclipse.app4mc.amalthea.model.LabelAccess;
import org.eclipse.app4mc.amalthea.model.ListObject;
import org.eclipse.app4mc.amalthea.model.ProcessingUnit;
import org.eclipse.app4mc.amalthea.model.ProcessingUnitDefinition;
import org.eclipse.app4mc.amalthea.model.Runnable;
import org.eclipse.app4mc.amalthea.model.RunnableEvent;
import org.eclipse.app4mc.amalthea.model.StimuliModel;
import org.eclipse.app4mc.amalthea.model.Stimulus;
import org.eclipse.app4mc.amalthea.model.Tag;
import org.eclipse.app4mc.amalthea.model.Task;
import org.eclipse.app4mc.amalthea.model.Time;
import org.eclipse.app4mc.amalthea.model.TimeUnit;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaLoader;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaWriter;
import org.eclipse.app4mc.amalthea.model.util.HardwareUtil;
import org.eclipse.app4mc.amalthea.model.util.InstructionsUtil;
import org.eclipse.app4mc.amalthea.model.util.ModelUtil;
import org.eclipse.app4mc.amalthea.model.util.RuntimeUtil;
import org.eclipse.app4mc.amalthea.model.util.RuntimeUtil.AccessDirection;
import org.eclipse.app4mc.amalthea.model.util.RuntimeUtil.TimeType;
import org.eclipse.core.internal.resources.AliasManager.AddToCollectionDoit;
import org.eclipse.app4mc.amalthea.model.util.SoftwareUtil;
import org.eclipse.app4mc.amalthea.model.util.TimeUtil;
import org.eclipse.emf.common.util.EList;

@SuppressWarnings("unused")
public class LoadModifySaveExample {

	@SuppressWarnings("unused")
	private static final TimeType TimeType = null;

	public static void main(String[] args) {

		// example: absolute path
		// final File inputFile = new File("d:/temp/democar.amxmi");
		// final File outputFile = new File("d:/temp/democar_1.amxmi");

		// example: relative path
		final File inputFile = new File("model-input/ChallengeModel_withCommImplementationTypev082.amxmi");
		//final File outputFile = new File("model-output/LoadModifySave/test_1.amxmi");
		//final File analysisLog = new File("log.app4mcLog");

		// ***** Load *****

		Amalthea model = AmaltheaLoader.loadFromFile(inputFile);
		if (model == null) {
			System.out.println("Error: No model loaded!");
			return;
		}
		int runnable_count=model.getSwModel().getRunnables().size();
		System.out.println("this model creates "+ runnable_count+" runnables");
		int i=0;
		// ***** Modify *****
		EList<Runnable> runnables = model.getSwModel().getRunnables();
		
		List<ProcessingUnit> processingUnits = HardwareUtil.getModulesFromHWModel(ProcessingUnit.class, model);
		//ProcessingUnit puGlobal = processingUnits.get(0);	
		
		for (ProcessingUnit pu : processingUnits) {
			//pu.getDefinition();
			System.out.println(HardwareUtil.getFrequencyOfModuleInHz(pu)/1000000 + " MHz");
		}
		for (Runnable runnable : runnables) {
			i++;
			System.out.println("Runnable No. "+i+" : " + runnable.getName());
			Set<Label> readlabels = SoftwareUtil.getReadLabelSet(runnable,null);
			Set<Label> writelabels = SoftwareUtil.getWriteLabelSet(runnable,null);
			
			int x=readlabels.size();
			int y=writelabels.size();
			System.out.println("		this runnable has " + x + " read labels: ");
			System.out.println("		this runnable has " + y + " write labels: ");
			System.out.println("			Read labels are: ");
			for (Label label: readlabels) {
				System.out.println("				*" + label.getName()+" ,"+label.getSize()+"s");
			}
			System.out.println("			Write labels are: ");
			for (Label label1: writelabels) {
				System.out.println("				*" + label1.getName()+" ,"+label1.getSize()+"s");
			}
			
		}
		EList<Task> tasks = model.getSwModel().getTasks();
		//List<Runnable> RunnablesFromTask= SoftwareUtil.getRunnableList(tasks.get(0), null);
		
		HwFeatureCategory instructionCategory = getOrCreateInstructionsCategory(model);
		Map<ProcessingUnit, Map<Runnable, Time>> mappingDataCustom = testmapping(model, runnables,instructionCategory);
		Map<ProcessingUnit, Map<HwAccessElement, Map<Runnable, Time>>> runnableAccessTimeMapping = bestCaseAccessLatencyMapping(model,runnables);
		for (ProcessingUnit pu: processingUnits) {
			Map<Runnable, Time> localMap = mappingDataCustom.get(pu);
			EList<HwAccessElement> accessElements = pu.getAccessElements();
			for (HwAccessElement  accessElement : accessElements) {
				HwDestination a = accessElement.getDestination();
				System.out.println("Total times of runnables on core" + pu.getName()+ " and memory "+ a.getName());
				for (Runnable runnable: runnables) {
					Set<Label> readlabels = SoftwareUtil.getReadLabelSet(runnable,null);
					Set<Label> writelabels = SoftwareUtil.getWriteLabelSet(runnable,null);
					Time localRunTime = localMap.get(runnable);
					Time localAccessTime = runnableAccessTimeMapping.get(pu).get(accessElement).get(runnable);
					System.out.println(runnable.getName() + "// execution time -->" + localRunTime + "// access time -->" + localAccessTime);
				}
			}
			System.out.println("Got here (for debgging purposes)");
		}
		System.out.println("-----------------------------------------------------");
		System.out.println("-----------------------------------------------------");
		System.out.println("-----------------------------------------------------");
		EList<EventChain> eventList = model.getConstraintsModel().getEventChains();
		for (EventChain event : eventList) {
			List<Runnable> runnablesFromEventChain = getRunnablesFromEventChain(event,runnables);
			Map<ProcessingUnit, Map<Runnable, Time>> mappingDataRunnableSubSet = runnableMappingForEventchain(mappingDataCustom,event);
			Map<ProcessingUnit, Map<HwAccessElement, Map<Runnable, Time>>> accessTimesForEventChain = bestCaseAccessLatencyMapping(model,runnablesFromEventChain);
			System.out.println(event.getName() +" runnable subset: ");
			for(Runnable runnable:runnablesFromEventChain) {
				System.out.println("	*"+runnable.getName());
			}
			Time localExecutionTimeForEvent = AmaltheaFactory.eINSTANCE.createTime();
			for (ProcessingUnit procUnit: processingUnits) {
				for (Runnable runnableSubSet: runnablesFromEventChain) {
					Time executionTimeForEventChainRunnable = mappingDataRunnableSubSet.get(procUnit).get(runnableSubSet);
					localExecutionTimeForEvent = TimeUtil.addTimes(localExecutionTimeForEvent, executionTimeForEventChainRunnable);
					
				}
				//localExecutionTimeForEvent.setUnit(TimeUnit.S);
				
				System.out.println("	Total Execution time of "+event.getName()+" on " + procUnit.getName()+"--> "+localExecutionTimeForEvent);
				EList<HwAccessElement> accessElements = procUnit.getAccessElements();
				
				for (HwAccessElement  accessElement : accessElements) {
					Time localAccessTime = AmaltheaFactory.eINSTANCE.createTime();
					HwDestination a = accessElement.getDestination();
					for (Runnable runnableSubSet: runnablesFromEventChain) {
						Time accessTimesForRunnableInEventChain = accessTimesForEventChain.get(procUnit).get(accessElement).get(runnableSubSet);
						localAccessTime = TimeUtil.addTimes(localAccessTime, accessTimesForRunnableInEventChain);	
					}
					System.out.println("		Access time of "+event.getName()+" on Memory "+a.getName()+" on " + procUnit.getName()+": " +localAccessTime);
				}
			
			}
		}
		for(EventChain event: eventList) {
			System.out.println("Tasks involved in the event chain: "+event.getName());
			Time endToEndLatencies = findLETChainLatency(model, event);
			System.out.println("end to end latency for "+event.getName()+" -->"+endToEndLatencies);
		}
		
		
		System.out.println("done");
	}
	
	//-----------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------
	public static Map<ProcessingUnit, Map<Runnable, Time>> runnableMappingForEventchain(Map<ProcessingUnit, Map<Runnable, Time>> fullMapping,EventChain event){
		Set<ProcessingUnit> fullProcessingUnits = fullMapping.keySet();
		
		Map<Runnable, Time> innerMap = new HashMap<>();
		Map<ProcessingUnit, Map<Runnable, Time>> mapFromRunnable = new HashMap<>();
		for (ProcessingUnit procUnit: fullProcessingUnits) {
			Set<Runnable> fullRunnables = fullMapping.get(procUnit).keySet();
			List<Runnable> fullRunnablesList = new ArrayList<>(fullRunnables);
			List<Runnable> runnablesFromEventChain = getRunnablesFromEventChain(event,fullRunnablesList);
			for (Runnable eventRunnable:runnablesFromEventChain) {
				for (Runnable mapRunnable : fullRunnables ) {
					boolean EqualityCondition = eventRunnable.getName().equals(mapRunnable.getName());					
					if (EqualityCondition) {
						innerMap.put(mapRunnable, fullMapping.get(procUnit).get(mapRunnable));
						mapFromRunnable.put(procUnit, innerMap);
					}
				}
			}
		}
		return mapFromRunnable;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static Time LabelAcessLatencyOnCore(Amalthea model, Label label, ProcessingUnit pu,HwDestination memory ) {
		@SuppressWarnings("static-access")
		Map<ProcessingUnit, HashMap<HwDestination, Time>> accessTimes = HardwareUtil.getAccessTimes(model, TimeType.WCET, AccessDirection.READ);
		HashMap<HwDestination, Time> localMap = accessTimes.get(pu);
		Time accessTimeForLabelOnCore = localMap.get(memory);
		return (accessTimeForLabelOnCore);
	}
	//-------------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------------
	public static final String INSTRUCTIONS_CATEGORY_NAME = "Instructions";
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
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------
	public static Map<ProcessingUnit, Map<Runnable, Time>> testmapping(Amalthea model, EList<Runnable> runnables,HwFeatureCategory instructionCategory) {
		
		List<ProcessingUnit> processingUnits = HardwareUtil.getModulesFromHWModel(ProcessingUnit.class, model);
		Map<ProcessingUnit, Map<Runnable, Time>> runTimeToRunnablePerProcessingUnit = new HashMap<>();
		for(ProcessingUnit pu : processingUnits) {
			List<HwFeature> instructionFeature = InstructionsUtil.getFeaturesOfCategory(pu.getDefinition(),instructionCategory);
			Map<Runnable, Time> runTimeToRunnableMap = new HashMap<>();
			for(Runnable runnable : runnables) {
				@SuppressWarnings("static-access")
				Time Runtime = RuntimeUtil.getExecutionTimeForRunnable(runnable, TimeType.WCET, pu, instructionFeature, null);
				runTimeToRunnableMap.put(runnable, Runtime);
				runTimeToRunnablePerProcessingUnit.put(pu,runTimeToRunnableMap);
			}
		}
			return(runTimeToRunnablePerProcessingUnit);
	}
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------	
	public static List<Runnable> getRunnablesFromEventChain(EventChain event,List<Runnable> fullRunnablesList) {		
		//Set<Runnable> eventChainRunnablesList = new HashSet<Runnable>();
		List<Runnable> eventChainRunnablesList = new ArrayList<Runnable>();
		EList<EventChainItem> Seg = event.getSegments();
		for (EventChainItem Se : Seg) {
			Event Stimulus = Se.getEventChain().getStimulus();
			Runnable stimulusRunnable = ((RunnableEvent) Stimulus).getEntity();
			eventChainRunnablesList.add(stimulusRunnable);
			Event Response = Se.getEventChain().getResponse();
			Runnable responseRunnable = ((RunnableEvent) Response).getEntity();
			eventChainRunnablesList.add(responseRunnable);
		}
		//List<Runnable> eventChainRunnablesListWithoutDuplicates = new ArrayList<>(new HashSet<>(eventChainRunnablesList));
		List<Runnable> eventChainRunnablesListWithoutDuplicates = eventChainRunnablesList.stream().distinct().collect(Collectors.toList());
		return eventChainRunnablesListWithoutDuplicates;
	}
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------	
	public static Map<ProcessingUnit,Map<HwAccessElement, Map<Runnable, Time>>> bestCaseAccessLatencyMapping(Amalthea model,List<Runnable> runnableList ){
		Map<ProcessingUnit,Map<HwAccessElement, Map<Runnable, Time>>> AccessTimeFullMap = new HashMap<>();
		List<ProcessingUnit> processingUnits = HardwareUtil.getModulesFromHWModel(ProcessingUnit.class, model);
		for (ProcessingUnit pu: processingUnits) {
			Map<HwAccessElement, Map<Runnable, Time>> localAccessTimeMapToMemory = new HashMap<>();
			EList<HwAccessElement> accessElement = pu.getAccessElements();
			for (HwAccessElement accessElementCounter : accessElement) {
				HwDestination memoryDef = accessElementCounter.getDestination();
				Map<Runnable,Time> localAccessTimeMap = new HashMap<>();
				for (Runnable runnable : runnableList) {
					Time localAccessTime = AmaltheaFactory.eINSTANCE.createTime();
					Set<Label> readlabels = SoftwareUtil.getReadLabelSet(runnable,null);
					Set<Label> writelabels = SoftwareUtil.getWriteLabelSet(runnable,null);
					for (Label label: readlabels) {
						Time labelAccessLatency = LabelAcessLatencyOnCore(model, label, pu,memoryDef );
						localAccessTime = TimeUtil.addTimes(localAccessTime,labelAccessLatency);
					}
					for (Label label: writelabels) {
						Time labelAccessLatency = LabelAcessLatencyOnCore(model, label, pu,memoryDef );
						localAccessTime = TimeUtil.addTimes(localAccessTime,labelAccessLatency);
					}
					localAccessTimeMap.put(runnable, localAccessTime);
				}
				localAccessTimeMapToMemory.put(accessElementCounter, localAccessTimeMap);
			}
			AccessTimeFullMap.put(pu, localAccessTimeMapToMemory);
		}
		
		return(AccessTimeFullMap);
	}
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------	
	public static List<Task> getAllTasksFromRunnable(Amalthea model, Runnable runnable){
		List<Task> tasksAssociatedWithArunnable = new ArrayList<Task>();
		EList<Task> tasksFromModel = model.getSwModel().getTasks();
		for (Task task:tasksFromModel) {
			List<Runnable> runnablesOfTask = SoftwareUtil.getRunnableList(task, null);
			if (runnablesOfTask.contains(runnable)) {
				tasksAssociatedWithArunnable.add(task);
			}
		}
		return tasksAssociatedWithArunnable;
	}
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------	
	public static Time getTaskPeriod(Amalthea model,Task task) {
		List<Time> periodsOfProcess = RuntimeUtil.getPeriodsOfProcess(model, task, TimeType.WCET, null);
		return periodsOfProcess.get(0);
		
	}
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------	
	public static Time taskPeriodSum(Amalthea model,List<Task> tasks,List<Integer> multiplicities) {
		Time sum = AmaltheaFactory.eINSTANCE.createTime();
		int i=0;
		for(Task task: tasks) {
			Time localTime = TimeUtil.multiplyTime(getTaskPeriod(model, task), multiplicities.get(i)); 
			i++;
			sum = TimeUtil.addTimes(sum, localTime);
		}		
		return sum;
	}
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------	
	public static Time findLETChainLatency(Amalthea model, EventChain event) {
		EList<Runnable> fullModelRunnables = model.getSwModel().getRunnables();
		List<Runnable> runnablesOfEventchain = getRunnablesFromEventChain(event, fullModelRunnables);
		List<Task> tasksToSum = new ArrayList<Task>();
		List<Integer> multiplicities = new ArrayList<Integer>();
		
		for (Runnable localRunnable:runnablesOfEventchain) {
			List<Task> taskList = getAllTasksFromRunnable(model, localRunnable);
			if (taskList.size()>1) {
				//TODO handle this case
				//find the task with the shortest period and add it to the sum list 
				BigInteger timeValue = new BigInteger("0");
				Task localTask = AmaltheaFactory.eINSTANCE.createTask();
				for (Task task:taskList) {
					Time local = getTaskPeriod(model, task);
					BigInteger localTime = local.getValue();
					timeValue = timeValue.min(localTime);
					if (timeValue == localTime) {
						localTask = task;
					}					
				}
				tasksToSum.add(localTask);
			}else {
				tasksToSum.add(taskList.get(0));
			}
		}
		tasksToSum=tasksToSum.stream().distinct().collect(Collectors.toList());
		int backwardsComm=0;
		for (Task task: tasksToSum) {
			System.out.println("---------------------------------------------------------------------");
			System.out.println(task.getName());
			List<Runnable> localRunnable = SoftwareUtil.getRunnableList(task, null);
			ArrayList<Runnable> localRunnablefiltered = new ArrayList<Runnable>();
			for (Runnable runnable : localRunnable) {
				if (runnablesOfEventchain.contains(runnable)) {
					localRunnablefiltered.add(runnable);
				}
			}
			int localCounterIndex=0;
			for (int i=0;i<localRunnablefiltered.size();i++) {
				for (int j=0;j<runnablesOfEventchain.size();j++) {
					if(localRunnablefiltered.get(i).equals(runnablesOfEventchain.get(j))) {
						if(backwardsComm==0) {
							localCounterIndex=j;
							backwardsComm++;
							break;
						} else if(localCounterIndex>j) {
							localCounterIndex=j;
							backwardsComm++;
							break;
						}
					}
				}
			}
			multiplicities.add(backwardsComm);
		}
		System.out.println("number of backward comms"+backwardsComm);
		Time Latency = taskPeriodSum(model, tasksToSum,multiplicities);
		return Latency;
	}
}

