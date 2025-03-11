package spic.cus.scmc.purcontract;

import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickArgs;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.list.BillList;
import kd.bos.list.events.BeforeShowBillFormEvent;
import kd.bos.list.plugin.AbstractListPlugin;

/**
 * 合同台账列表插件(未启用)
 * @author songruiping
 *
 */
public class ContractDetailListPlugin extends AbstractListPlugin {

	@Override
	public void billListHyperLinkClick(HyperLinkClickArgs args) {
		super.billListHyperLinkClick(args);
		HyperLinkClickEvent hyperLinkClickEvent = args.getHyperLinkClickEvent();

		BillList source = (BillList) hyperLinkClickEvent.getSource();
		ListSelectedRow currentSelectedRowInfo = source.getCurrentSelectedRowInfo();
		Long primaryKeyValue = (Long) currentSelectedRowInfo.getPrimaryKeyValue();
		String billFormId = source.getBillFormId();
		FormShowParameter formShowParameter = new FormShowParameter();
		// 打开采购合同台账
		if ("spic_purcontractdetail_pc".equals(billFormId)) {
			formShowParameter.setFormId("spic_purcontractdetail");
		//打开销售合同台账
		}else if("spic_salcontractdetail_pc".equals(billFormId)){
			formShowParameter.setFormId("spic_salcontractdetail");
		}
		
		formShowParameter.setCustomParam("PKValue", primaryKeyValue);
		formShowParameter.getOpenStyle().setShowType(ShowType.NewTabPage);
		this.getView().showForm(formShowParameter);
	}

	@Override
	public void beforeShowBill(BeforeShowBillFormEvent e) {
		// 取消打开编辑界面
		e.setCancel(true);
	}

}
