package globals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;

import gantt.basemodel.common.OperationSequence;
import gantt.basemodel.common.OperationState;
import gantt.basemodel.common.OperationStateException;
import gantt.basemodel.common.OrderState;
import gantt.basemodel.common.OrderStateException;
import gantt.basemodel.common.ProdRef;
import gantt.basemodel.common.Product;
import gantt.basemodel.common.ProductionOrder;
import gantt.basemodel.common.Resource;
import gantt.basemodel.common.Upd;
import gantt.basemodel.common.ConstraintArc;
import gantt.basemodel.common.ConstraintArc.ArcInstanceType;
import gantt.basemodel.common.Operation;
import gantt.basemodel.common.Operation.Anchor;
import gantt.plugin.BaseModelAPI.ProductionOrderHelper;
import gantt.plugin.BaseModelAPI;
import multiuser.domain.production.interfaces.EConstraintArcValue;
import multiuser.domain.production.interfaces.EOperationValue;
import multiuser.domain.production.interfaces.EProductionOrderValue;
import multiuser.util.UpdateHelper;

public class BaseModelHelper {

	/**
	 * 
	 * @param bApi
	 * @param masterRouteName
	 * @param alternateRoute
	 * @param orderId
	 * @param orderName
	 * @param orderQty
	 * @param orderState
	 * @param materialDate
	 * @param deliveryDate
	 * @return
	 */
	public static final ProductionOrder createOrderFromMasterRoute(BaseModelAPI bApi, String masterRouteName,
			String alternateRoute, String orderId, String orderName, Double orderQty, OrderState orderState,
			DateTime materialDate, DateTime deliveryDate) {

		try {

			/*
			 * Create the production order data
			 * 
			 */
			EProductionOrderValue orderValue = new EProductionOrderValue();
			orderValue.setId(orderId);
			orderValue.setName(orderName);
			orderValue.setQuantity(orderQty);
			orderValue.setState(orderState.getLevel());
			orderValue.setMaterialCalendar(new Timestamp(materialDate.getMillis()));
			orderValue.setDeliveryCalendar(new Timestamp(deliveryDate.getMillis()));

			return createOrderFromMasterRoute(bApi, masterRouteName, alternateRoute, orderValue);

		} catch (Exception e) {
			System.out.println("Error trying to create order! " + e);
			// throw new RuntimeException("Error trying to create order!", e);
		}
		return null;
	}

	/**
	 * 
	 * @param bAPI
	 * @param masterRouteName
	 * @param alternateRoute
	 * @param orderValue
	 * @return
	 */
	public static final ProductionOrder createOrderFromMasterRoute(BaseModelAPI bAPI, String masterRouteName,
			String alternateRoute, EProductionOrderValue orderValue) {
		/*
		 * Find the template route to use
		 * 
		 */

		OperationSequence templateRoute = bAPI.getOperationSequenceWithNameAndAlternateRoute(masterRouteName, // primary
																												// route
				alternateRoute, // alternate route name
				true, // only template routes
				false // no template routes
		);

		if (templateRoute == null) {
			System.err.print("Master route " + masterRouteName + " does not exist");
			return null;
		}
		try {

			String orderId = orderValue.getId();

			/*
			 * Clone the template route
			 * 
			 */
			String productName;
			if (orderValue.productNameHasBeenSet()) {
				productName = orderValue.getProductName();
			} else {
				productName = masterRouteName;
			}
			OperationSequence sequenceClone = bAPI.createOperationSequenceClone(templateRoute);
			Product product = bAPI.createProduct(orderId, productName, sequenceClone.getId(), orderId);

			// link seq and order to product!
			sequenceClone.setProductId(product.getId());
			orderValue.setProductId(product.getId());

			// add sequence
			bAPI.addOperationSequence(sequenceClone);

			ProductionOrderHelper orderHelper = new ProductionOrderHelper(orderValue, product.getEProductValue());
			orderHelper.setOprSeqVal(sequenceClone.getEOperationSequenceValue());

			return bAPI.createUpdateProductionOrder(orderHelper, null);

//			bAPI.getScheduler().schedule(order, schedulingStrategy);

		} catch (Exception e) {
			System.err.println("Error trying to create order " + e);
			throw new RuntimeException("Error trying to create order!", e);
		}
	}

	/**
	 * Creates a constraint arc between two operations
	 * 
	 * @param srcOprId
	 * @param dstOprId
	 * @return
	 * @return
	 */
	public static ConstraintArc createConstraintArc(BaseModelAPI bAPI, String srcOprId, String dstOprId, boolean hardLink) {
		EConstraintArcValue arcVal = new EConstraintArcValue();
		try {
//			System.out.println("CREATE ARC between " + srcOprId + " and " + dstOprId);
//			arcVal.setId(bAPI.getUniqueId(UpdateHelper.CONSTRAINT_ARC));
			arcVal.setSourceId(srcOprId);
			arcVal.setDestinationId(dstOprId);

			arcVal.setSourceParentId(srcOprId);
			arcVal.setInstanceType(ArcInstanceType.ROUTE.getPersistenceId());
			
			if (hardLink) {
				arcVal.setHardlink(true);
				arcVal.setSyncResource(true);
			}
			
			return bAPI.createOrUpdateConstraintArc(arcVal, Upd.arc());
		} catch (Exception e) {
			System.err.println("Could not create arc " + e);
		}
		return null;
	}

	/**
	 * Creates a prodRef (Route network arc) between two operations
	 * 
	 * @param bAPI
	 * @param srcOpr
	 * @param dstOpr
	 * @return
	 */
	public static ConstraintArc createProdRef(BaseModelAPI bAPI, Operation srcOpr, Operation dstOpr) {
		try {
			ProductionOrder srcPo = srcOpr.getProductionOrder();
			ProductionOrder dstPo = dstOpr.getProductionOrder();
			System.out.println("Creating a suborder link bewteen: (" + srcPo + ", " + srcOpr.getName() + ") and ("
					+ dstPo + ", " + dstOpr.getName() + ")");
			if (srcOpr != null && dstOpr != null && srcPo != null && dstPo != null) {

				String srcOprId = srcOpr.getId();
				String dstOprId = dstOpr.getId();
				EConstraintArcValue arcVal = new EConstraintArcValue();

//				arcVal.setId(bAPI.getUniqueId(UpdateHelper.CONSTRAINT_ARC));

//				arcVal.setId(srcOprId + "-" + dstOprId);
				arcVal.setInstanceType(ArcInstanceType.PROD_REF.getPersistenceId());

				arcVal.setSourceId(srcOprId);
				arcVal.setSourceParentId(srcPo.getId());

				arcVal.setDestinationId(dstOprId);
				arcVal.setDestinationParentId(dstPo.getId());

				arcVal.setSourceAnchor(Anchor.WORKLOAD_END.getPersistenceId());

				return bAPI.createOrUpdateConstraintArc(arcVal);
			}
		} catch (Exception e) {
			System.out.println("error in createSubOrder: " + e);
		}
		return null;
	}

	public static Operation findOperationInOrder(ProductionOrder order, String oprName) {
		for (Operation opr : order.getOperationList()) {
			if (oprName.equals(opr.getName())) {
				return opr;
			}
		}
		return null;

	}

	public static void removeOutgoingProdRefsFromOperations(BaseModelAPI bAPI, List<Operation> oprList) {

		Set<String> arcIds = new HashSet<String>();
		for (Operation opr : oprList) {
			arcIds.addAll(getOutgoingProdRefIdsFromOperation(bAPI, opr));
		}
		deleteArcs(bAPI, arcIds);
		bAPI.endAllChange();
	}

	public static void removeIncommingProdRefsFromOperations(BaseModelAPI bAPI, List<Operation> oprList) {

		Set<String> arcIds = new HashSet<String>();
		for (Operation opr : oprList) {
			arcIds.addAll(getIncommingProdRefsIdsFromOperation(bAPI, opr));
		}
		deleteArcs(bAPI, arcIds);
		bAPI.endAllChange();
	}

	public static void removeAllProdRefsFromOperations(BaseModelAPI bAPI, List<Operation> oprList) {

		Set<String> arcIds = new HashSet<String>();
		for (Operation opr : oprList) {
			arcIds.addAll(getAllProdRefIdsFromOperation(bAPI, opr));
		}
		deleteArcs(bAPI, arcIds);
		bAPI.endAllChange();
	}

	public static Set<String> getOutgoingProdRefIdsFromOperation(BaseModelAPI bAPI, Operation opr) {

		Set<String> arcIds = new HashSet<String>();

		for (ProdRef arc : opr.getOutgoingProdRefs()) {
			arcIds.add(arc.getId());
		}

		return arcIds;
	}

	public static Set<String> getIncommingProdRefsIdsFromOperation(BaseModelAPI bAPI, Operation opr) {

		Set<String> arcIds = new HashSet<String>();

		for (ProdRef arc : opr.getIncomingProdRefs()) {
			arcIds.add(arc.getId());
		}

		return arcIds;
	}

	public static Set<String> getAllProdRefIdsFromOperation(BaseModelAPI bAPI, Operation opr) {

		Set<String> arcIds = new HashSet<String>();

		arcIds.addAll(getIncommingProdRefsIdsFromOperation(bAPI, opr));
		arcIds.addAll(getOutgoingProdRefIdsFromOperation(bAPI, opr));

		return arcIds;
	}

	public static void deleteArcs(BaseModelAPI bAPI, Set<String> arcIds) {
		deleteArcs(bAPI, arcIds, Upd.arc());
	}

	public static void deleteArcs(BaseModelAPI bAPI, Set<String> arcIds, Upd upd) {
		try {
			bAPI.beginAllChange();
			for (String id : arcIds) {
				ConstraintArc arc = bAPI.getConstraintWithId(id);
				bAPI.deleteConstraintArc(arc, upd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		bAPI.endAllChange();
	}



	public static void startOperation(Operation opr, Upd upd) {
		if (opr == null) {
			return;
		}
		try {
			OperationState status = opr.getStatus();
			if (!(status == OperationState.STARTED || status == OperationState.COMPLETE)) {
				ProductionOrder order = opr.getProductionOrder();
				if (order.getStatusLevel() <= 20) {
					order.setStatus(OrderState.RELEASED);
				}
				order.setPreConditionsOk(true);
				opr.setStart(new DateTime(), upd);
				opr.setStatus(OperationState.STARTED);
			}
		} catch (OperationStateException e) {
			e.printStackTrace();
		} catch (OrderStateException e) {
			e.printStackTrace();
		}
	}

	public static void completeOperation(Operation opr, Upd upd) {
		try {
			if (opr.getStatus() != OperationState.COMPLETE) {

				opr.setEnd(DateTime.now());
				opr.setStatus(OperationState.COMPLETE, true, upd);
			}
		} catch (OperationStateException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public static void completePreviousOperations(Operation opr, Upd upd) {

		try {
			DateTime nextOprStart = opr.getNextOperation().getStart();
//		DateTime actualStart = opr.getActualStart();
//				if(start.isBefore())
			if (opr.getStatus() != OperationState.COMPLETE) {
				opr.setEnd(nextOprStart, upd);
			}

			Operation prevOperation = opr.getFirstPreviousOperation();
			if (prevOperation != null && prevOperation.getStatus() != OperationState.COMPLETE) {
				completePreviousOperations(prevOperation, upd);
			}
			opr.setStatus(OperationState.COMPLETE, true, upd);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


		/**
		 * Creates a resource or resource group. If the resource already exist it
		 * returns the existing resource
		 * 
		 * @param id
		 * @param name
		 * @param isResGroup
		 * @param groupId
		 * @return
		 */
		public static Resource createUpdateResource(BaseModelAPI bAPI, String id, String name, boolean isResGroup,
				String groupId) {

			Resource res = bAPI.getResourceWithId(id);

			if (res == null) {
				if (isResGroup) {
					res = bAPI.createResourceGroup(id, name);
				} else {
					res = bAPI.createResource(id, name);
					if (groupId != null) {
						res.setResourceGroupId(groupId);
						res.setColor(res.getResourceGroup().getColor());
					}

				}
			}

			return res;
		}

		public static Operation createOperationClone(BaseModelAPI bAPI, Operation templateOpr, String routeId) {
			String oprId = bAPI.getUniqueId(UpdateHelper.RESERVE_OPERATION_ID);

			Operation opr = bAPI.createOperation(oprId, templateOpr.getName(), routeId, templateOpr.getId(),
					Operation.OPERATION_CLONED);
			return opr;

		}

		public static Operation getTemplateOperation(BaseModelAPI bAPI, String kstPlg, String oprName,
				String[] primaryResources, boolean isSubOperation) {
			try {

				String templateOprId = oprName;
				if (primaryResources.length > 0) {
					for (String string : primaryResources) {
						templateOprId += string;
					}
				} else {
					templateOprId += kstPlg;
				}

				return getTemplateOperation(bAPI, kstPlg, templateOprId, oprName, primaryResources, isSubOperation);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
		
		public static Operation getTemplateOperation(BaseModelAPI bAPI, String kstPlg, String id, String oprName,
				String[] primaryResources, boolean isSubOpr) {
			try {

				Operation templateOpr = bAPI.getOperationWithId(id);
				if (templateOpr == null) {
					EOperationValue operationValue = new EOperationValue();
					operationValue.setId(id);
					operationValue.setName(oprName);
					operationValue.setType(Operation.OPERATION_DEFINED);

					templateOpr = bAPI.createOrUpdateOperation(operationValue, true);
					BaseModelHelper.assignResourcesToOpr(bAPI, templateOpr, kstPlg, primaryResources);
				}
				System.out.println(templateOpr.getName());

				if (templateOpr.getName() == null || !templateOpr.getName().equals(oprName)) {
					templateOpr.setName(oprName);
				}

				if ((templateOpr.getPossibleResourceList() == null || templateOpr.getPossibleResourceList().isEmpty())
						|| (primaryResources != null && primaryResources.length != 0
								&& !areResourcesSame(templateOpr, primaryResources))) {
					assignResourcesToOpr(bAPI, templateOpr, kstPlg, primaryResources);
				}

				return templateOpr;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
		
		private static boolean areResourcesSame(Operation opr, String[] resList) {

			List<String> resNames = Arrays.asList(resList);
			List<String> currentResNames = new ArrayList<String>();
			List<Resource> possibleResourceList = opr.getPossibleResourceList();

			for (Resource resource : possibleResourceList) {
				currentResNames.add(resource.getId());
			}
			return CollectionUtils.isEqualCollection(currentResNames, resNames);
		}
		
		/**
		 * Assigns the resources to the operation. Creates resources if it doesn't
		 * already exist.
		 * 
		 * @param opr
		 * @param kstPlg             //The resource for the operation if its not part of
		 *                           a group. If it's part of a group kstPlg will be the
		 *                           resourcGroup while the primaryResources will be the
		 *                           actual resources.
		 * @param primaryResources
		 * @param secondaryResources
		 */
		public static void assignResourcesToOpr(BaseModelAPI bAPI, Operation opr, String kstPlg,
				String[] primaryResources) {
			try {
				List<Resource> possibleResourceList = new ArrayList<Resource>();
				// If the resource is a group or part of a group
				String selectedResId = null;
				if (primaryResources != null && primaryResources.length != 0) {
					createUpdateResource(bAPI, kstPlg, kstPlg, true, null); // Group resource

					// Adds the primary the primary resources in the group as possible resources for
					// the operation

					primaryResources = ParseHelper.parseNumbersAsStrInArray(primaryResources);

					for (int i = 0; i < primaryResources.length; i++) {
						String resName = primaryResources[i];
						if (!resName.equals("0")) {
							Resource primaryRes = createUpdateResource(bAPI, primaryResources[i], primaryResources[i],
									false, kstPlg);
							opr.addPossibleResource(primaryRes);
							possibleResourceList.add(primaryRes);
						}
					}
					selectedResId = primaryResources[0];

				} else {
					// Adds resource as a possible resources for the operation
					Resource stdRes = createUpdateResource(bAPI, kstPlg, kstPlg, true, null);
					opr.addPossibleResource(stdRes);
					possibleResourceList.add(stdRes);
					selectedResId = kstPlg;
				}

				if (selectedResId != null) {
					opr.setSelectedResourceId(selectedResId, Upd.op());
				}

			} catch (Exception e) {
				System.out.println("Could not update operaiton " + opr + " ." + e);
			}
		}

}
