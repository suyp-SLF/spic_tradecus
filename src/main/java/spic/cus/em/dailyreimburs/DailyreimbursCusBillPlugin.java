package spic.cus.em.dailyreimburs;

import java.util.EventObject;

import kd.bos.bill.AbstractBillPlugIn;

public class DailyreimbursCusBillPlugin extends AbstractBillPlugIn {
	
	
	@Override
	public void afterBindData(EventObject e) {
		// TODO Auto-generated method stub
		super.afterBindData(e);
		setExpenseEntryView();
	}

	private void setExpenseEntryView() {
		// TODO Auto-generated method stub
		int rowCount = this.getModel().getEntryRowCount("expenseentryentity");
		
	}
}
