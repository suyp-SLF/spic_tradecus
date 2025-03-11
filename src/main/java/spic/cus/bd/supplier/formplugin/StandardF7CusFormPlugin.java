package spic.cus.bd.supplier.formplugin;

import java.util.EventObject;

import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.CloseCallBack;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Control;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.form.plugin.list.StandardF7FormPlugin;
import kd.bos.list.BillList;
import kd.bos.list.IListView;
import kd.bos.list.ListShowParameter;

public class StandardF7CusFormPlugin extends AbstractFormPlugin {


	private static final String SHOW_BIZPARTNER = "show_bizpartner";

	@Override
	public void registerListener(EventObject e) {
		// TODO Auto-generated method stub
		super.registerListener(e);
		this.addClickListeners("spic_bizpartner");
		
	}
	
	@Override
	public void click(EventObject evt) {
		// TODO Auto-generated method stub
		super.click(evt);
		Control source = (Control)evt.getSource();
		String key = source.getKey();
		if(StringUtils.equals(key, "spic_bizpartner")) {
			ListShowParameter lsp = new ListShowParameter();
			lsp.setBillFormId("bd_bizpartner");
			lsp.setLookUp(true);
			lsp.setMultiSelect(true);
			lsp.setShowTitle(false);
			lsp.setHasRight(true);
			
			lsp.setFormId("spic_bos_listf7_inh");
			lsp.getOpenStyle().setShowType(ShowType.Modal);
			lsp.setF7Style(2);
			StyleCss css = new StyleCss();
			css.setWidth("1000px");
			css.setHeight("600px");
			lsp.getOpenStyle().setInlineStyleCss(css);
			
			lsp.setCloseCallBack(new CloseCallBack(this,SHOW_BIZPARTNER));
			ListShowParameter param = (ListShowParameter) getView().getFormShowParameter();
			lsp.setUseOrgId(param.getUseOrgId());
			getView().showForm(lsp);
		}
	}
	
	@Override
	public void closedCallBack(ClosedCallBackEvent evt) {
		// TODO Auto-generated method stub
		super.closedCallBack(evt);
		String actionId = evt.getActionId();
		if(StringUtils.equals(actionId, SHOW_BIZPARTNER));
		IListView listView = (IListView) this.getView();
		listView.refresh();
		BillList billList = this.getControl("billlistap");
		billList.clearSelection();
		billList.refresh();
	}
}
