package user;

import java.util.Objects;
import gantt.basemodel.common.context.UpdContext;
import gantt.basemodel.common.context.UpdOrigin;
import gantt.basemodel.event.OperationEvent;
import gantt.basemodel.event.OperationListener;
import gantt.basemodel.event.OrderEvent;
import gantt.basemodel.event.OrderListener;
import gantt.plugin.BaseModelAPI;

public class CustomListener {

	private BaseModelAPI bAPI;
	UpdContext localContext;

	public CustomListener(BaseModelAPI bAPI) {
		this.bAPI = bAPI;
		localContext = UpdContext.of(UpdOrigin.UNKNOWN, "Listner");
		initOperationListner();
		initOrderListner();

	}

//	private Upd getLocalUpd() {
//		return Upd.op().withContext(localContext);
//	}
//
//	private Upd getLocalArcUpd() {
//		return Upd.arc(true, true, true).withContext(localContext);
//	}

	private void initOperationListner() {
		
		bAPI.addOperationListener(new OperationListener() {

			public void operationChanged(OperationEvent e) {
				if (e.getContext().isMultiUserOrigin()
						|| Objects.equals(e.getContext().getOriginSpecification(), "Listner")) {
					return;
				}

			}

			public void operationGroupDeleted(OperationEvent e) {
			}

			public void operationGroupChanged(OperationEvent e) {
			}

			public void operationGroupAdded(OperationEvent e) {
			}

			public void operationDeleted(OperationEvent e) {
			}

			public void operationAdded(OperationEvent e) {
			}
		});
	}


	public void initOrderListner() {

		bAPI.addOrderListener(new OrderListener() {

			@Override
			public void orderDeleted(OrderEvent e) {
			}

			@Override
			public void orderChanged(OrderEvent e) {

				
				if (e.getContext().isMultiUserOrigin()
						|| Objects.equals(e.getContext().getOriginSpecification(), "Listner")) {
					return;
				}

				
			}

			@Override
			public void orderAdded(OrderEvent e) {
			}
		});
	}
}
