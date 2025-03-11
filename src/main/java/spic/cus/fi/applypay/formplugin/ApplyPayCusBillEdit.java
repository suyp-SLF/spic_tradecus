package spic.cus.fi.applypay.formplugin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.EventObject;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.events.AfterAddRowEventArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import spic.cus.fi.common.utils.CusFiUtils;

public class ApplyPayCusBillEdit extends AbstractBillPlugIn {


	@Override
	public void afterBindData(EventObject e) {
		// TODO Auto-generated method stub
		super.afterBindData(e);
		if("0".equals(this.getModel().getDataEntity().getPkValue().toString())) {
			this.setDefaultCurrencyInfo();
		}
	}
	
	@Override
	public void afterCreateNewData(EventObject e) {
		// TODO Auto-generated method stub
		super.afterCreateNewData(e);
		int rowCount = this.getModel().getEntryRowCount("entry");
		if(rowCount>0) {
			//费用承担部门默认值
			Long orgId  = RequestContext.get().getOrgId();
			if(0L != orgId && null != orgId) {
				this.getModel().setValue("spic_entrycostdept", orgId, 0);
			}
		}
	}
	
	@Override
	public void afterAddRow(AfterAddRowEventArgs e) {
		// TODO Auto-generated method stub
		super.afterAddRow(e);
		int rownum = e.getRowDataEntities()[0].getRowIndex();
		Long orgId  = RequestContext.get().getOrgId();
		//费用承担部门默认值
		if(0L != orgId && null != orgId) {
			this.getModel().setValue("spic_entrycostdept", orgId, rownum);
		}
	}
	
	private void setDefaultCurrencyInfo() {
		DynamicObject settleOrg = (DynamicObject) this.getModel().getValue("settleorg");
		if(null != settleOrg) {
			QFilter[] filters = { new QFilter("org", "=", settleOrg.getPkValue()) };
			DynamicObject ap_init = BusinessDataServiceHelper.loadSingleFromCache("ap_init", filters);
			if(ap_init != null && ap_init.getDynamicObject("standardcurrency") != null) {
				if(null == this.getModel().getValue("paycurrency")) {
					
					this.getModel().setValue("paycurrency", ap_init.getDynamicObject("standardcurrency"));
				}
				if(null == this.getModel().getValue("settlecurrency")) {
					
					this.getModel().setValue("settlecurrency", ap_init.getDynamicObject("standardcurrency"));
				}
    			
			}
			if(ap_init != null && this.getModel().getValue("exratetable")==null) {
				this.getModel().setValue("exratetable", ap_init.getDynamicObject("exratetable"));
			}
			if(null == this.getModel().getValue("exchangerate") || 
					BigDecimal.ZERO.compareTo((BigDecimal) this.getModel().getValue("exchangerate")) == 0) {
				
				Date exratedate = (Date) this.getModel().getValue("exratedate");
				DynamicObject destCurrency = (DynamicObject) this.getModel().getValue("settlecurrency");
				DynamicObject exratetable =(DynamicObject) this.getModel().getValue("exratetable");
				DynamicObject srcCurrency = (DynamicObject) this.getModel().getValue("paycurrency");
				if(destCurrency != null && exratetable != null && srcCurrency != null && exratedate != null) {
					if(StringUtils.equals(destCurrency.getString("number"), srcCurrency.getString("number"))) {
						this.getModel().setValue("exchangerate", 1);
					}else {
						
						this.getModel().setValue("exchangerate", CusFiUtils.getExchangeRate(exratetable.getLong("id"), srcCurrency.getLong("id"), 
								destCurrency.getLong("id"), exratedate));
					}
					
				}
			}
			
		}
	}
}
