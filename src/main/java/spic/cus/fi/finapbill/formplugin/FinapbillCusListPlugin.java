package spic.cus.fi.finapbill.formplugin;

import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;

public class FinapbillCusListPlugin extends AbstractListPlugin {

	@Override
	public void itemClick(ItemClickEvent evt) {
		// TODO Auto-generated method stub
		super.itemClick(evt);
		if(StringUtils.equals("spic_loadfundplan", evt.getItemKey())) {
			FormShowParameter fsp = new FormShowParameter();
			fsp.setFormId("spic_loadplanformatinfo");
			fsp.getOpenStyle().setShowType(ShowType.Modal);
			this.getView().showForm(fsp);
		}
	}
}
