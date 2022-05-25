package scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import gantt.basemodel.common.Operation;
import gantt.basemodel.common.ProductionOrder;
import gantt.basemodel.common.Resource;
import gantt.plugin.BaseModelAPI;

public class SelectComponents {

	public static List<Resource> getResourcesWithID(BaseModelAPI bAPI, String[] resIDs) {
		List<Resource> resList = new ArrayList<Resource>();
		for (String id : resIDs) {
			Resource res = bAPI.getResourceWithId(id);
			if (id != null) {
				resList.add(res);
			}
		}

		return resList;
	}

	public static List<Operation> findOperationsInOrders(List<ProductionOrder> orderList, String[] resourceList,
			String[] oprNames, int[] oprStatus, int[] orderStatus, DateTime startDate, DateTime endDate) {
		List<Operation> operationList = new ArrayList<Operation>();

		for (ProductionOrder order : orderList) {
			List<Operation> findOperationsInList = findOperationsInList(order.getOperationList(), resourceList,
					oprNames, oprStatus, orderStatus, startDate, endDate);
			operationList.addAll(findOperationsInList);
		}

		return operationList;
	}

	/**
	 * 
	 * @param operationList
	 * @param resourceList
	 * @param oprNames
	 * @param oprStatus
	 * @param orderStatus
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static List<Operation> findOperationsInList(List<Operation> operationList, String[] resourceList,
			String[] oprNames, int[] oprStatus, int[] orderStatus, DateTime startDate, DateTime endDate) {
		List<Operation> filteredOperationList = findOperationsInList(operationList, resourceList, oprNames, oprStatus,
				orderStatus, startDate, endDate, new DefaultParam(), null);
		return filteredOperationList;
	}

	/**
	 * 
	 * @param operationList
	 * @param resourceList
	 * @param oprNames
	 * @param oprStatus
	 * @param orderStatus
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static List<Operation> findOperationsInList(List<Operation> operationList, String[] resourceList,
			String[] oprNames, int[] oprStatus, int[] orderStatus, DateTime startDate, DateTime endDate,
			CustomSearchParameter param, Comparator<Operation> comp) {
		List<Operation> filteredOperationList = operationList.stream()
				.filter(opr -> opr.getProductionOrder() != null && opr.getResource() != null
						&& opr.getType() == Operation.OPERATION_CLONED && opr.getStart().isAfter(startDate)
						&& opr.getStart().isBefore(endDate) && isStringInArray(opr.getName(), oprNames)
						&& isIntInArray(opr.getStatus().getLevel(), oprStatus)
						&& isIntInArray(opr.getProductionOrder().getStatusLevel(), orderStatus)
						&& isStringInArray(opr.getResource().getId(), resourceList) && (param == null || param.getKey(opr)))
				.collect(Collectors.toList());
		if (comp != null) {
			Collections.sort(filteredOperationList, comp);
		}
		return filteredOperationList;
	}
	
	/**
	 * @param operationList
	 * @param resourceList
	 * @param startDate
	 * @return
	 */
	public static List<Operation> findFixOperations(List<Operation> operationList, String[] resourceList,
			 DateTime startDate) {
		List<Operation> filteredOperationList = operationList.stream()
				.filter(opr -> opr.getProductionOrder() != null && opr.getResource() != null 
				&& opr.getType() == Operation.OPERATION_CLONED 
				&& ((opr.getStart().isBefore(startDate) && !opr.getEnd().isBefore(new DateTime())
						&& opr.getStatusLevel() < 70 && opr.getStatusLevel() > 15)
				|| (opr.getStart().isAfter(startDate) 
						&& !opr.getProductionOrder().getName().matches("[0-9]+") 
						&& opr.getStart().isBefore(startDate.plusDays(20))) )
				&& isStringInArray(opr.getResource().getId(), resourceList) )
				.collect(Collectors.toList());
		return filteredOperationList;
	}

	public static List<ProductionOrder> findOrders(BaseModelAPI bAPI, int[] orderStatus, DateTime startDate,
			DateTime endDate) {
		List<ProductionOrder> productionOrderList = bAPI.getProductionOrderCollection().stream()
				.filter(order -> order.getEarliestStart() != null &&
						order.getEarliestStart().isAfter(startDate)
						&& order.getEarliestStart().isBefore(endDate)
						&& isIntInArray(order.getStatusLevel(), orderStatus))
				.collect(Collectors.toList());
		return productionOrderList;
	}

	public static List<ProductionOrder> getOrdersFromOprList(List<Operation> oprList) {

		Set<ProductionOrder> orders = new HashSet<ProductionOrder>();
		List<ProductionOrder> orderList = new ArrayList<ProductionOrder>();
		for (Operation opr : oprList) {
			orders.add(opr.getProductionOrder());
		}
		orderList.addAll(orders);
		return orderList;
	}

	public static boolean isStringInArray(String str, String[] strList) {
		if ((strList == null || strList.length == 0)) {
			return true;
		}
		for (String currentStr : strList) {
			if (currentStr.equals(str)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isIntInArray(int intVal, int[] intList) {
		if (intList == null || intList.length == 0) {
			return true;
		}
		for (int currentStr : intList) {
			if (currentStr == intVal) {
				return true;
			}
		}
		return false;
	}
	

	public static class DefaultParam implements CustomSearchParameter {

		@Override
		public boolean getKey(Operation opr) {

			return true;
		}
	}

	

	public static class OprStartComparator implements Comparator<Operation> {

		@Override
		public int compare(Operation o1, Operation o2) {

			return o1.getActualStart().compareTo(o2.getActualStart());
		}

	}

	public static class StatusAndStartDateComparator implements Comparator<Operation> {

		@Override
		public int compare(Operation o1, Operation o2) {
			int statusLevel1 = o1.getStatusLevel();
			int statusLevel2 = o2.getStatusLevel();
			DateTime start1 = o1.getActualStart();
			DateTime start2 = o2.getActualStart();

			int statusComp = Integer.compare(statusLevel2, statusLevel1);

			if (statusComp == 0) {
				return start1.compareTo(start2);
			} else {
				return statusComp;
			}
		}
	}

	public static class EndDateComparator implements Comparator<Operation> {

		@Override
		public int compare(Operation o1, Operation o2) {
			int statusLevel1 = o1.getStatusLevel();
			int statusLevel2 = o2.getStatusLevel();
			DateTime start1 = o1.getActualStart();
			DateTime start2 = o2.getActualStart();

			int statusComp = Integer.compare(statusLevel2, statusLevel1);

			if (statusComp == 0) {
				return start1.compareTo(start2);
			} else {
				return statusComp;
			}
		}
	}

	public static class MaterialDateComparator implements Comparator<Operation> {

		@Override
		public int compare(Operation o1, Operation o2) {

			ProductionOrder order1 = o1.getProductionOrder();
			ProductionOrder order2 = o2.getProductionOrder();

			DateTime mat1 = order1.getMaterialDateTime();
			DateTime mat2 = order2.getMaterialDateTime();

			int matComp = mat1.compareTo(mat2);
			if (mat1.compareTo(mat2) == 0) {
				DateTime start1 = o1.getActualStart();
				DateTime start2 = o2.getActualStart();
				return start1.compareTo(start2);
			} else {
				return matComp;
			}
		}
	}

	public static class MaterialDateComparatorOrder implements Comparator<ProductionOrder> {

		@Override
		public int compare(ProductionOrder o1, ProductionOrder o2) {

			DateTime mat1 = o1.getMaterialDateTime();
			DateTime mat2 = o2.getMaterialDateTime();

			return mat1.compareTo(mat2);

		}
	}

	public static class DeliveryDateComparatorOrder implements Comparator<ProductionOrder> {

		@Override
		public int compare(ProductionOrder o1, ProductionOrder o2) {

			DateTime del1 = o1.getDeliveryDateTime();
			DateTime del2 = o2.getDeliveryDateTime();

			int delCmp = del1.compareTo(del2);

			if (delCmp == 0) {
				DateTime mat1 = o1.getMaterialDateTime();
				DateTime mat2 = o2.getMaterialDateTime();
				return mat1.compareTo(mat2);
			}
			return delCmp;

		}
	}

	public static class OrderNbrComparator implements Comparator<ProductionOrder> {

		@Override
		public int compare(ProductionOrder o1, ProductionOrder o2) {

			DateTime mat1 = o1.getMaterialDateTime();
            DateTime mat2 = o2.getMaterialDateTime();
            
            int y1 = mat1.getYear();
            int y2 = mat2.getYear();
                        
            int d1 = mat1.getDayOfYear();
            int d2 = mat2.getDayOfYear();                        
            
            if(y1 == y2) {
                if(d1 == d2) {
                    return o1.getName().compareTo(o2.getName()); 
                }
                else {
                    return d1-d2;
                }
            }
            else {
                return y1-y2;
            }
		}
	}

	public static class startDateComparator implements Comparator<Operation> {

		public int compare(Operation o1, Operation o2) {

			DateTime start1 = o1.getActualStart();
			DateTime start2 = o2.getActualStart();

			return start1.compareTo(start2);
		}
	};

	public static class EarlisetStartedOprComparator implements Comparator<Operation> {

		public int compare(Operation o1, Operation o2) {
			Integer statusCmp = ((Integer) o2.getStatusLevel()).compareTo(o1.getStatusLevel());
			if (statusCmp != 0) {
				return statusCmp;
			}
			DateTime start1 = o1.getActualStart();
			DateTime start2 = o2.getActualStart();
			return start1.compareTo(start2);
		}
	};
}
