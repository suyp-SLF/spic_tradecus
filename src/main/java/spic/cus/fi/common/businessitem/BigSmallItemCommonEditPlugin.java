package spic.cus.fi.common.businessitem;

import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.field.BasedataEdit;
import kd.fi.fr.listener.BeforeBusItemF7SelectListener;


public class BigSmallItemCommonEditPlugin extends AbstractBillPlugIn {

	private static final String bigItemKey = "spic_businessitem";
	
	
	@Override
	public void registerListener(EventObject e) {
		// TODO Auto-generated method stub
		super.registerListener(e);
		BasedataEdit businessitemF7 = this.getControl(bigItemKey);
		String entityName = getModel().getDataEntityType().getName();
		String accountOrgKey = "org";
		String adminOrgKey = "org";
		String smallItemKey = "e_expenseitem";//费用项目标识
		if(StringUtils.equals(entityName, "ap_payapply")) {
			accountOrgKey = "settleorg";
			adminOrgKey = "applyorg";
			smallItemKey = "spic_expenseitem";
		}
		List<String> small = Arrays.asList("ap_finapbill","ap_invoice");
		if(small.contains(entityName)) {
			smallItemKey = "expenseitem";
		}
		businessitemF7.addBeforeF7SelectListener(new BeforeBusItemF7SelectListener(getView(), entityName, accountOrgKey, adminOrgKey));

		BasedataEdit expenseitemF7 = this.getControl(smallItemKey);
		expenseitemF7.addBeforeF7SelectListener(new CusBeforeExpItemF7SelectListener(getView(),entityName, accountOrgKey, adminOrgKey,bigItemKey));
	}
}
