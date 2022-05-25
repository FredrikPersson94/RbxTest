package scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import gantt.basemodel.common.BaseModelBase;
import gantt.basemodel.common.Operation;
import gantt.basemodel.common.ProductionOrder;
import gantt.basemodel.scheduler.DefaultBottleneckScheduler;
import gantt.basemodel.scheduler.Scheduler;
import gantt.basemodel.scheduler.SchedulingStrategy;
import gantt.basemodel.scheduler.SchedulingStrategy.DependencyStrategy;
import gantt.plugin.BaseModelAPI;

public class ScheduleHelper {

	public static void schedule(BaseModelAPI bAPI, Scheduler scheduler, SchedulingStrategy schedStrat,
			List<? extends BaseModelBase> objList) {
		try {

			if (objList != null && !objList.isEmpty()) {
				System.out.println("-----------------SCHEDULE THESE " + objList + " -----------------------");
				scheduler.schedule(objList, schedStrat);
			}
		} catch (Exception e) {
			System.out.println("Could not schedule " + e);
		}
	}

	public static SchedulingStrategy setStandardParametersForStrat(SchedulingStrategy schedStrat) {
		schedStrat.setScheduleStartedCompletedOperation(false);
		schedStrat.setAllowMoveResourceInGroup(true);
		schedStrat.setEnsureAfterNow(true);
		schedStrat.setDependencyStrategy(DependencyStrategy.SCHEDULE_DIRECT_DEPENDENCIES);
		return schedStrat;

	}

	public static SchedulingStrategy getStandardForwardFromNow() {
		return setStandardParametersForStrat(BaseModelAPI.FORWARD_FROM_NOW_SMART.createCopy());
	}

	public static SchedulingStrategy getStandardBackwardFromDelivery() {
		return setStandardParametersForStrat(BaseModelAPI.BACKWARD_FROM_DELIVERY_CALENDAR_SMART.createCopy());
	}

	public static SchedulingStrategy getStandardForwardFromDate(DateTime startDate) {
		SchedulingStrategy strategy = setStandardParametersForStrat(
				BaseModelAPI.FORWARD_FROM_FIXED_CALENDAR_SMART.createCopy());
		strategy.setSchedulingDateTime(startDate);
		return strategy;
	}

	public static SchedulingStrategy getStandardForwardFromMaterialDate() {
		SchedulingStrategy strategy = setStandardParametersForStrat(
				BaseModelAPI.FORWARD_FROM_MATERIAL_CALENDAR_SMART.createCopy());
		return strategy;
	}

	public static SchedulingStrategy getForwardBottleNeckStrategy(BaseModelAPI bAPI) {
		DefaultBottleneckScheduler scheduler = (DefaultBottleneckScheduler) bAPI.getDefaultBottleneckScheduler();

		SchedulingStrategy strategy = setStandardParametersForStrat(scheduler.getDefaultForwardStrategy().createCopy());
		return strategy;
	}

	public static SchedulingStrategy getBackwardBottleNeckStrategy(BaseModelAPI bAPI) {
		DefaultBottleneckScheduler scheduler = (DefaultBottleneckScheduler) bAPI.getDefaultBottleneckScheduler();

		SchedulingStrategy strategy = setStandardParametersForStrat(
				scheduler.getDefaultBackwardStrategy().createCopy());
		return strategy;
	}
	
	public static SchedulingStrategy getStandardBackwardFromDate(DateTime startDate) {
		SchedulingStrategy strategy = setStandardParametersForStrat(
				BaseModelAPI.BACKWARD_FROM_FIXED_CALENDAR_SMART.createCopy());
		strategy.setSchedulingDateTime(startDate);
		return strategy;
	}

	public static Map<Operation, Integer> setOperationsScheduleStatus(List<Operation> operationList,
			int scheduleState) {
		Map<Operation, Integer> map = new HashMap<Operation, Integer>();
		for (Operation opr : operationList) {
			map.put(opr, opr.getSchedulingState());
			opr.setSchedulingState(scheduleState);
//			opr.setUnburdenResource(true);
		}
		return map;
	}

	public static Map<Operation, Integer> setOrderScheduleStatus(List<ProductionOrder> orderList, int scheduleState) {
		Map<Operation, Integer> map = new HashMap<Operation, Integer>();

		for (ProductionOrder order : orderList) {

			map.putAll(setOperationsScheduleStatus(order.getOperationList(), scheduleState));
		}

		return map;
	}

	/**
	 * Sets the operations scheduleState to the previous level
	 */
	public static void resetSchedulingStatus(Map<Operation, Integer> map) {
		for (Map.Entry<Operation, Integer> entry : map.entrySet()) {
			Operation operation = entry.getKey();
//			int schedulingState = entry.getValue();
			// operation.setSchedulingState(schedulingState);
			operation.setSchedulingState(Operation.IS_SCHEDULED);
//			operation.setUnburdenResource(false);
		}
	}
	

	public static void setPrevOprsToNow(Operation opr) {
		Operation prevOpr = opr.getFirstPreviousOperation();
		if (prevOpr != null && !prevOpr.getName().equals(opr.getName())) {
			prevOpr.setStart(opr.getStart());
			setPrevOprsToNow(prevOpr);
		}
	}

	public static void lockOperations(List<Operation> operationList, boolean setlock) {
		for (Operation opr : operationList) {
			opr.setLocked(setlock);
		}
	}

	

}
