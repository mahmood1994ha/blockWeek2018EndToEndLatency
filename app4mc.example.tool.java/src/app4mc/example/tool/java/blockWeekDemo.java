
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.eclipse.app4mc.amalthea.model.Memory;
import org.eclipse.app4mc.amalthea.model.MemoryMapping;
import org.eclipse.app4mc.amalthea.model.Process;
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
import org.eclipse.app4mc.amalthea.model.Value;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaLoader;
import org.eclipse.app4mc.amalthea.model.io.AmaltheaWriter;
import org.eclipse.app4mc.amalthea.model.util.DeploymentUtil;
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
import org.eclipse.emf.common.util.EMap;

@SuppressWarnings("unused")
public class blockWeekDemo {

	@SuppressWarnings("unused")
	private static final TimeType TimeType = null;

	public static void main(String[] args) {
		String modelPath = args[0];
		String commMode = args[1];
		
		// example: absolute path
		// final File inputFile = new File("d:/temp/democar.amxmi");
		// final File outputFile = new File("d:/temp/democar_1.amxmi");

		// example: relative path
		final File inputFile = new File(modelPath);
		
		//final File outputFile = new File("model-output/LoadModifySave/test_1.amxmi");
		//final File analysisLog = new File("log.app4mcLog");

		// ***** Load *****

		Amalthea model = AmaltheaLoader.loadFromFile(inputFile);
		if (model == null) {
			System.out.println("Error: No model loaded!");
			return;
		}
		
		int runnable_count=model.getSwModel().getRunnables().size();
		int taskCount = model.getSwModel().getTasks().size();
		
		System.out.println("this model creates "+ runnable_count+" runnables");
		System.out.println("this model creates "+ taskCount+" tasks");
		// ***** Modify *****
		EList<Runnable> runnables = model.getSwModel().getRunnables();
		
		List<ProcessingUnit> processingUnits = HardwareUtil.getModulesFromHWModel(ProcessingUnit.class, model);
		//ProcessingUnit puGlobal = processingUnits.get(0);	
		
		for (ProcessingUnit pu : processingUnits) {
			//pu.getDefinition();
			System.out.println(pu.getName()+" frequency: " + HardwareUtil.getFrequencyOfModuleInHz(pu)/1000000 + " MHz");
		}
		
		EList<Task> tasks = model.getSwModel().getTasks();
		//List<Runnable> RunnablesFromTask= SoftwareUtil.getRunnableList(tasks.get(0), null);
		
		/*HwFeatureCategory instructionCategory = getOrCreateInstructionsCategory(model);
		Map<ProcessingUnit, Map<Runnable, Time>> mappingDataCustom = testmapping(model, runnables,instructionCategory);
		Map<ProcessingUnit, Map<HwAccessElement, Map<Runnable, Time>>> runnableAccessTimeMapping = bestCaseAccessLatencyMapping(model,runnables);
		*/
		
		EList<EventChain> eventList = model.getConstraintsModel().getEventChains();
		if (args[1] == "let") {
			System.out.println("-----------------------------------------------------");
			System.out.println("-----------------------------------------------------");
			System.out.println("-------------LET COMMUNICATION MODEL---------------------");
			for(EventChain event: eventList) {
				System.out.println("Tasks involved in the event chain: "+event.getName());
				Time endToEndLatencies = findLETChainLatency(model, event);
				System.out.println("end to end latency for "+event.getName()+" -->"+endToEndLatencies);
			}
		}else if (args[1]=="implicit") {
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("-----------------------------IMPLICIT COMMUNICATION MODEL-------------------------------");
			for(EventChain event: eventList) {
				System.out.println("Tasks involved in the event chain: "+event.getName());
				EndToEndImplicit(model, event);
				//System.out.println("end to end latency for "+event.getName()+" -->"+endToEndLatencies);
			}
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
			//cInCost(model,task);
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
		int i=0;
		/*for(Task task: tasksToSum) {
			if(task)
		}*/
		
		Time Latency = taskPeriodSum(model, tasksToSum,multiplicities);
		//EList<MemoryMapping> x = model.getMappingModel().getMemoryMapping();
		//x = DeploymentUtil.getLabelMapping(Labellabel);
		//deploymentutil.
		
		return Latency;
	}
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------	
	public static Time cInCost(Amalthea model, Task task) {
		List<Runnable> runnablesOfATask = SoftwareUtil.getRunnableList(task, null);
		Set<Label> readLabelSet = SoftwareUtil.getReadLabelSet(task, null);
		
		Set<ProcessingUnit> procunit = DeploymentUtil.getAssignedCoreForProcess(task,model);
		List<ProcessingUnit> procunitlist = new ArrayList<ProcessingUnit>();
		procunitlist.addAll(procunit);
		EList<HwAccessElement> accessElements = procunitlist.get(0).getAccessElements();
		List<HwDestination> memoryDef = new ArrayList<HwDestination>();
		for(HwAccessElement accElement: accessElements) {
			memoryDef.add(accElement.getDestination());
		}
		Time readingFromOutsideMemory = AmaltheaFactory.eINSTANCE.createTime();
		
		for (Label label : readLabelSet) {	
			Set<Memory> memoryOfLebel = DeploymentUtil.getLabelMapping(label);
			List<Memory> memoryOfLabelList = new ArrayList<Memory>();
			memoryOfLabelList.addAll(memoryOfLebel);
			
			for(Memory memory: memoryOfLabelList) {
				for(HwDestination memDef:memoryDef) {
					if (memDef.getName().equals(memory.getName())) {
						Time localReading = LabelAcessLatencyOnCore(model, label, procunitlist.get(0), memDef);
						readingFromOutsideMemory = TimeUtil.addTimes(readingFromOutsideMemory, localReading);
						
					}
				}
			}
		}
		
		//int cyclesForCopyinToLocal = readLabelSet.size();
		return readingFromOutsideMemory;
	}
	
	public static Time COutCost(Amalthea model,ProcessingUnit pu,Task task){
		///Assuming memory cost of writing is 2, this method returns the Cout cost of Task task in µs/
		Time Cout = AmaltheaFactory.eINSTANCE.createTime();
		//Frequency freq = model.getHwModel().getSystem().getQuartzes().get(0).getFrequency();
		long frequencyInHertz = HardwareUtil.getFrequencyOfModuleInHz(pu);
		Set<Label> writeLabelSet = SoftwareUtil.getWriteLabelSet(task, null);
		long timeInNanoSeconds = (long) (10e9*2*writeLabelSet.size()/frequencyInHertz);
		BigInteger coutCount = BigInteger.valueOf(timeInNanoSeconds);
		Cout.setValue(coutCount);
		Cout.setUnit(TimeUnit.NS);
/*
		Cout.setValue((BigInteger.valueOf((long)(writeLabelSet.size() * 2 * 10e12))));
		Cout.setUnit(TimeUnit.PS);
		Cout.setValue(Cout.getValue().divide(BigInteger.valueOf((long) freq.getValue())));*/ 
		//return (Time) TimeUtil.convertToTimeUnit(Cout, TimeUnit.US);
		return Cout;
	}
	
	
	//-----------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------
	public static void EndToEndImplicit(Amalthea model, EventChain event) {
		EList<Runnable> fullModelRunnables = model.getSwModel().getRunnables();
		List<Runnable> runnablesOfEventchain = getRunnablesFromEventChain(event, fullModelRunnables);
		List<Task> tasksToSum = new ArrayList<Task>();
		List<Integer> multiplicities = new ArrayList<Integer>();
		long totalLatency = 0;
		List<Task> prevTask = new ArrayList<Task>();
		for (Runnable runnable: runnablesOfEventchain) {
			List<Task> taskList = getAllTasksFromRunnable(model, runnable);
			if (taskList.equals(prevTask)) {
				continue;
			}
			prevTask = taskList;
			for (Task task:taskList) {
				List<ProcessingUnit> procunit = new ArrayList<ProcessingUnit>(DeploymentUtil.getAssignedCoreForProcess(task, model));
				Map<Task, Integer> priorityMapping = getPriorityOfTasksOnCore(model, procunit.get(0));
				/*for (Task taskLocal :priorityMapping.keySet()) {
					System.out.println(taskLocal.getName()+"-->Priority "+ priorityMapping.get(taskLocal) );
					
				}*/
				long responseTime = findResponseTime(model,priorityMapping,task, procunit.get(0));
				long cInForTask = (long) Math.ceil(cInCost(model, task).getValue().longValue()/1000000);
				long cOutForTask = (long) Math.ceil(COutCost(model,procunit.get(0),task).getValue().longValue()/1000000);
				
				totalLatency  = totalLatency + responseTime + cInForTask + cOutForTask;
				System.out.println("Task: "+ task.getName()+"response time--> "+ responseTime+" ms");
				
				System.out.println("Task "+ task.getName()+ " Execution time-->"+ getExecTimeForProcess(model, task, procunit.get(0)));
				
				System.out.println("Task: "+task.getName()+"Cin cost-->"+ cInCost(model, task));
				System.out.println("Task: "+task.getName()+"Cout cost-->"+ COutCost(model,procunit.get(0),task));
			}
		}
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Total latency for "+event.getName()+" -->"+ totalLatency);
		System.out.println("------------------------------------------------------------------------");
	}
	
	public static Map<Task,Integer> getPriorityOfTasksOnCore(Amalthea model, ProcessingUnit pu){
		Map<Task,Integer> priorityMap = new HashMap<>();
		Set<Process> tasksOnCore = DeploymentUtil.getProcessesMappedToCore(pu, model);
		List<Time> taskPeriod = new ArrayList<Time>();
		Map<Task,Time> taskToPeriod = new HashMap<>();
		
		for (Process taskOnCore: tasksOnCore) {
			Time localPeriod = getTaskPeriod(model, (Task)taskOnCore);
			taskPeriod.add(localPeriod);
			taskToPeriod.put((Task) taskOnCore, localPeriod);
		}
		
		taskToPeriod = sortByValue(taskToPeriod);
		int Priority = tasksOnCore.size();
		for (Task task: taskToPeriod.keySet()) {
			priorityMap.put(task, Priority);
			Priority--;
		}
		return priorityMap;
		
	}
	//This code is form stackoverflow https://stackoverflow.com/a/2581754
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
	

	
	public static long findResponseTime(Amalthea model,Map<Task,Integer> priorityMap,Task task, ProcessingUnit processingUnit) {
		List<Task> higherPriorityTaskList = higherPriorityTasks(priorityMap,task);
		double localSum = 0;
		long cj=0;
		long tj=0;
		long wi=0;
		long ci=(long) getExecTimeForProcess(model, task, processingUnit).getValue().longValue()/1000000;//this value is in nanoSeconds
		//System.out.println("Ci value without unit-->"+ci);
		long recurrenceSum=0;
		
		if(higherPriorityTaskList.size()==0) {
			recurrenceSum = ci;
		}else {
			for(Task highPriorityTask: higherPriorityTaskList) {
				wi=findResponseTime(model, priorityMap, highPriorityTask, processingUnit);
				Time tjWithUnit = getTaskPeriod(model, highPriorityTask);
				tjWithUnit.setUnit(TimeUnit.US);
				tj = tjWithUnit.getValue().longValue();
				//tj = getTaskPeriod(model, highPriorityTask).getValue().intValue();//this value is in Milliseconds
				cj = (long) getExecTimeForProcess(model, highPriorityTask, processingUnit).getValue().longValue()/1000000;//this value is in NanoSeconds
				recurrenceSum =  (long) Math.ceil(recurrenceSum +ci+ Math.ceil(wi/tj)*cj);
				if(recurrenceSum==wi) {
					break;
				}	
			}
		}
		return recurrenceSum;//returned in nanoSeconds
	}
	public static Time getExecTimeForProcess(Amalthea model, Task task,ProcessingUnit pu) {
		List<Runnable> Run = SoftwareUtil.getRunnableList(task, null);
		//Time totalTime = AmaltheaFactory.eINSTANCE.createTime();
		HwFeatureCategory instructionCategory = getOrCreateInstructionsCategory(model);
		List<HwFeature> instructionFeature = InstructionsUtil.getFeaturesOfCategory(pu.getDefinition(),instructionCategory);
		Time totalTime = RuntimeUtil.getExecutionTimeForProcess(task, TimeType.WCET, pu, instructionFeature, null);
		/*for(Runnable R:Run) {
			Time time = RuntimeUtil.getExecutionTimeForRunnable(R, TimeType.WCET, pu, instructionFeature, null);
			totalTime = TimeUtil.addTimes(totalTime, time);
		}*/
		return totalTime;
	}
	
	
	
	public static List<Task> higherPriorityTasks(Map<Task,Integer> priorityMap,Task task){
		int basePriority = 0;
		List<Task> higherPriority = new ArrayList<Task>();
		
		for (Task taskIterator: priorityMap.keySet()) {
			if(taskIterator.equals(task)) {
				basePriority = priorityMap.get(taskIterator);
			}
		}
		
		for (Task taskIterator: priorityMap.keySet()) {
			if(priorityMap.get(taskIterator)>basePriority) {
				higherPriority.add(taskIterator);
			}
		}
		return higherPriority;
	}

}

