package globals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import gantt.basemodel.common.InventoryCalculationHelper;
import gantt.basemodel.common.Operation;
import gantt.basemodel.common.ProductionOrder;
import gantt.basemodel.common.RawMaterial;
import gantt.basemodel.common.RawMaterialAccess;
import gantt.plugin.BaseModelAPI;
import gantt.utils.unit.IsoUnitOfMeasure;
import multiuser.domain.production.interfaces.ERawMaterialTransactionValue;
import multiuser.domain.production.interfaces.ERawMaterialValue;
import multiuser.util.UpdateHelper;

public class MaterialHelper {
	/**
	 * @param opr
	 * @param materialName
	 */
	public static RawMaterialAccess importMaterialToOperaiton(BaseModelAPI bAPI, Operation opr, String materialName) {
		try {

			if (StringUtils.isNotBlank(materialName)) {
				RawMaterial material = createMaterial(bAPI, materialName);

				if (material != null) {
					String id = bAPI.getUniqueId(UpdateHelper.RESERVE_MATERIAL_TRANSACTION_ID);

					ERawMaterialTransactionValue matTransVal = createMaterialTransactionValue(id, material.getId(),
							RawMaterialAccess.TRANSACTION_TYPE_CONSUME, null, RawMaterialAccess.ACCESS_METHOD_FRONT,
							RawMaterialAccess.ACCESS_QUANTITY_STRATEGY_PR_PIECE, 0);
					return bAPI.createRawMaterialAccess(matTransVal, material, opr);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static RawMaterialAccess importMaterialToOperaiton(BaseModelAPI bAPI, Operation opr, RawMaterial material,
			double quantity, int transactionType, int quantityStrategy) {

		if (material != null && opr != null) {
			int accessMethod = 0;
			if (transactionType == RawMaterialAccess.TRANSACTION_TYPE_CONSUME) {
				accessMethod = RawMaterialAccess.ACCESS_METHOD_FRONT_INCL_SETUP;
			} else if (transactionType == RawMaterialAccess.TRANSACTION_TYPE_PRODUCE) {
				accessMethod = RawMaterialAccess.ACCESS_METHOD_END;
			}

			String id = bAPI.getUniqueId(UpdateHelper.RESERVE_MATERIAL_TRANSACTION_ID);
			
			ERawMaterialTransactionValue matTransVal = createMaterialTransactionValue(id, material.getId(),
					transactionType, null, accessMethod, quantityStrategy, quantity);

			return bAPI.createRawMaterialAccess(matTransVal, material, opr);
		}

		return null;

	}

//	/**
//	 * 
//	 * @param rm
//	 * @param operation
//	 * @param transType
//	 * @param unitOfMeasure    the ISO unit of measure cod - i.e. LBR=pound, GLL=US
//	 *                         Gallon, MTQ=Cubic metre, MWH=megawatt hour,
//	 *                         MTK=Square meter, MTR=meter
//	 * @param quantityStrategy
//	 * @param staticQuantity
//	 * @return
//	 */
//	public static ERawMaterialTransactionValue createMaterialTransactionValue(RawMaterial rm, Operation operation,
//			int transType, IsoUnitOfMeasure unitOfMeasure, int accessMethod, int quantityStrategy,
//			double staticQuantity) {
//		ERawMaterialTransactionValue value = createMaterialTransactionValue(rm, transType, unitOfMeasure, accessMethod,
//				quantityStrategy, staticQuantity);
//
//		String accessId = operation.getId() + rm.getId();
//		value.setId(accessId);
//		return value;
//	}

	/**
	 * 
	 * @param rm
	 * @param transType
	 * @param unitOfMeasure    the ISO unit of measure cod - i.e. LBR=pound, GLL=US
	 *                         Gallon, MTQ=Cubic metre, MWH=megawatt hour,
	 *                         MTK=Square meter, MTR=meter
	 * @param quantityStrategy
	 * @param staticQuantity
	 * @return
	 */
	public static ERawMaterialTransactionValue createMaterialTransactionValue(String id, String materialId,
			int transType, IsoUnitOfMeasure unitOfMeasure, int accessMethod, int quantityStrategy,
			double staticQuantity) {

		ERawMaterialTransactionValue value = new ERawMaterialTransactionValue();
		value.setId(id);
		value.setRawMaterialId(materialId);
		value.setTransactionType(transType);
		if (unitOfMeasure != null) {
			value.setUnitFactor(unitOfMeasure.getUnitFactor());
		}
		value.setQuantityStrategy(quantityStrategy);

		value.setAccessMethod(accessMethod);

		if (accessMethod == RawMaterialAccess.ACCESS_QUANTITY_STRATEGY_STATIC) {
			value.setStaticQuantity(staticQuantity);
		}

		return value;
	}

	public static void createMaterialCounting(BaseModelAPI bAPI, RawMaterial rm, int quantity) {

		ERawMaterialTransactionValue value = new ERawMaterialTransactionValue();
		value.setRawMaterialId(rm.getId());
		value.setTransactionType(RawMaterialAccess.TRANSACTION_TYPE_COUNTING);
		value.setQuantity(quantity);
		value.setTransactionDate(new Timestamp(new DateTime().getMillis()));
		value.setId(rm.getId());
		bAPI.createOrUpdateRawMaterialCounting(value);
	}

	/**
	 * Creates a raw material
	 * 
	 * @param bAPI
	 * @param materialName
	 * @return
	 */
	public static RawMaterial createMaterial(BaseModelAPI bAPI, String materialName) {

		RawMaterial mat = bAPI.getRawMaterialById(materialName);
		if (mat == null) {

			ERawMaterialValue val = new ERawMaterialValue();
			val.setId(materialName);
			val.setName(materialName);
			val.setNumber(materialName);
			val.setSignificantInPlanning(true);
			val.setUnit(gantt.utils.unit.UnitManager.UNIT_CLASS_AMOUNT);

			mat = bAPI.createRawMaterial(val);

		}
		return mat;
	}

	/**
	 * Creates a raw material
	 * 
	 * @param bAPI
	 * @param materialName
	 * @return
	 */
	public static RawMaterial createMaterial(BaseModelAPI bAPI, String materialName, boolean useInPlanning) {

		RawMaterial mat = createMaterial(bAPI, materialName);
		if (mat != null) {

			mat.setSignificantInPlanning(useInPlanning);
		}
		return mat;
	}

	public static void createMaterialQuantityChange(BaseModelAPI bAPI, RawMaterial rm, int quantity, DateTime date) {

		ERawMaterialTransactionValue value = new ERawMaterialTransactionValue();
		value.setId(rm.getId() + DateHelper.dateTimeToString(date, "YYYY-MM-dd"));

		int transactionType = 0;
		if (quantity >= 0) {
			transactionType = RawMaterialAccess.TRANSACTION_TYPE_PURCHASE;
		} else {
			transactionType = RawMaterialAccess.TRANSACTION_TYPE_SHIPMENT;
		}

		value.setTransactionType(transactionType);
		value.setTransactionDate(new Timestamp(date.getMillis()));
		value.setTransactionEndTime(date);
		value.setQuantity(quantity);
		bAPI.createOrUpdateRawMaterialCounting(value);
	}
	
	public static void deleteRawMaterialAccessFromOpr(BaseModelAPI bAPI, Operation opr) {
		List<RawMaterialAccess> accessList = opr.getRawMaterialAccessList(false);
		deleteRawMaterialAccess(bAPI, accessList);
		
	}
	
	public static void deleteRawMaterialAccess(BaseModelAPI bAPI, List<RawMaterialAccess> accessList) {
		List<String> accessListStr = new ArrayList<String>();
		for (RawMaterialAccess rawMaterialAccess : accessList) {
			accessListStr.add(rawMaterialAccess.getId());
		}
		for (String id : accessListStr) {
			bAPI.deleteRawMaterialAccess(bAPI.getRawMaterialAccessById(id));
		}
		
	}

	public static Set<RawMaterial> getConsumingMaterialsForOrders(List<ProductionOrder> orderList) {
		Set<RawMaterial> rawMaterials = new HashSet<RawMaterial>();
		for (ProductionOrder order : orderList) {
			rawMaterials.addAll(getConsumingMaterialsForOrder(order));
		}
		return rawMaterials;
	}

	public static Set<RawMaterial> getConsumingMaterialsForOrder(ProductionOrder order) {
		Set<RawMaterial> rawMaterials = new HashSet<RawMaterial>();
		Operation firstOperation = order.getFirstOperation();
		if (firstOperation != null) {
			rawMaterials.addAll(getConsumingMaterialsForOperation(firstOperation));
		}
		return rawMaterials;
	}

	public static Set<RawMaterial> getConsumingMaterialsForOperation(Operation operation) {
		Set<RawMaterial> rawMaterials = new HashSet<RawMaterial>();
		List<RawMaterialAccess> rawMaterialAccessList = operation.getRawMaterialAccessList(false);
		for (RawMaterialAccess rawMaterialAcess : rawMaterialAccessList) {
			if (rawMaterialAcess.isConsuming()) {
				rawMaterials.add(rawMaterialAcess.getRawMaterial());
			}
		}
		return rawMaterials;
	}
	
	public static boolean hasOrderConsumingStartUpMaterial(ProductionOrder order) {

		for (Operation opr : order.getOperationList()) {
			if(hasOprConsumingStartUpMaterial(opr)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasOprConsumingStartUpMaterial(Operation opr) {
		List<RawMaterialAccess> rawMaterialAccessList = opr.getRawMaterialAccessList(false);
		for (RawMaterialAccess rawMaterialAccess : rawMaterialAccessList) {
			if (rawMaterialAccess.isConsuming()) {
				return true;
			}
		}
		return false;
	}

	public static boolean isOperationMaterialRdy(BaseModelAPI bAPI, Operation operation) {
		List<RawMaterialAccess> rawMaterialAccessList = operation.getRawMaterialAccessList(false);
		int materialsWithStock = 0;
		for (RawMaterialAccess rawMaterialAccess : rawMaterialAccessList) {
			RawMaterial rawMaterial = rawMaterialAccess.getRawMaterial();
			double expectedInventory = rawMaterial.getExpectedInventory(operation.getActualStart().plusMinutes(1),
					bAPI.getDefaultWarehouse(), new InventoryCalculationHelper());
			if (expectedInventory >= 0) {
				materialsWithStock++;
			}
		}
		int size = rawMaterialAccessList.size();
		return materialsWithStock == size;
	}

	public static boolean checkAndsetOrderRdy(BaseModelAPI bAPI, Operation operation) {
		ProductionOrder order = operation.getProductionOrder();
		System.out.println("Checking if " + order.getName() + " is ready to start");
		System.out.println(" ");
		if (MaterialHelper.isOperationMaterialRdy(bAPI, operation)) {
			order.setPreConditionsOk(true, true, true, true);
			System.out.println("Order " + order.getName() + " is ready to start");
			return true;
		} else {
			order.setCustomBoolean1(true);
			return false;

		}

	}
}
