package spic.cus.scmc.purcontract;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.filter.FilterParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.list.BillList;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.botp.BFTrackerServiceHelper;

public class PurcontractCusBillPlugin extends AbstractBillPlugIn implements HyperLinkClickListener{

	@Override
	public void beforeBindData(EventObject e) {
		// TODO Auto-generated method stub
		super.beforeBindData(e);
		BillList spic_aplist = this.getControl("spic_aplist");
		Object billid = this.getModel().getDataEntity().getPkValue();
		Set<Long> ids = new HashSet<Long>();
		if(!"0".equals(billid.toString())) {
			
			Map<String, HashSet<Long>> targetBillsMap = BFTrackerServiceHelper.findTargetBills("conm_purcontract", new Long[] {(Long) billid});
			System.out.println(targetBillsMap);
			ids = targetBillsMap.get("ap_finapbill");
			
		}
		FilterParameter filterParameter = new FilterParameter();
		filterParameter.setFilter(new QFilter("id", QCP.in, ids));
		spic_aplist.setFilterParameter(filterParameter);
	}

	@Override
	public void registerListener(EventObject e) {
		// TODO Auto-generated method stub
		super.registerListener(e);
		BillList billlist = this.getControl("spic_aplist");
		billlist.addHyperClickListener(this);
	}

	@Override
	public void hyperLinkClick(HyperLinkClickEvent e) {
		String fieldName = e.getFieldName();
		int rowIndex = e.getRowIndex();
		BillList source = (BillList) e.getSource();
		Long pkValue = 0L;
		ListSelectedRowCollection selectedRows = source.getSelectedRows();
		if (selectedRows != null && selectedRows.size() > 1) {
			pkValue = (Long) selectedRows.get(rowIndex).getPrimaryKeyValue();
		} else {
			pkValue = (Long) selectedRows.get(0).getPrimaryKeyValue();
		}
		if (!StringUtils.isEmpty(fieldName) && !(pkValue == 0L)) {
			String entityId = source.getEntityId();
			showbill(entityId, pkValue);
		}

	}

	protected void showbill(String entityId, Long pkValue) {
		BillShowParameter billShowParameter = new BillShowParameter();
		billShowParameter.setFormId(entityId);
		billShowParameter.setPkId(pkValue);
		billShowParameter.getOpenStyle().setShowType(ShowType.Modal);
		this.getView().showForm(billShowParameter);
	}
	
}
