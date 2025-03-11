package spic.cus.fi.fr.glreimpaybill.formplugin;

import java.util.EventObject;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.CloseCallBack;
import kd.bos.form.control.Control;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.TextEdit;
import kd.bos.list.ListShowParameter;
import kd.fi.fr.utils.AccountInfo;
import kd.fi.fr.utils.ListConstructorHelper;
import kd.fi.fr.utils.ReimbursePayerAcctUtils;

public class GlreimPayBillCusEditPlugin extends AbstractBillPlugIn {

	@Override
	public void registerListener(EventObject e) {
		// TODO Auto-generated method stub
		super.registerListener(e);
		TextEdit payee = (TextEdit)this.getControl("spic_epayee");
//		payee.addClickListener(this);
		payee.addButtonClickListener(this);
		TextEdit payeeAccount = (TextEdit)this.getControl("spic_epayeeaccount");
		payeeAccount.addButtonClickListener(this);
//		payeeAccount.addClickListener(this);
	}
	
	
	@Override
	public void click(EventObject evt) {
		// TODO Auto-generated method stub
		super.click(evt);
		 Control control = (Control)evt.getSource();
		 String key = control.getKey();
		 if ("spic_epayee".equals(key)) {
		    this.selectEntryPayer();
		 } else if ("spic_epayeeaccount".equals(key)) {
		    this.selectEntryAccountByPayee();
		 }
	}
	
	 private void selectEntryPayer() {
	      String epayeeType = (String)this.getModel().getValue("spic_epayeetype");

	      if (!StringUtils.isEmpty(epayeeType)) {
	         BasedataEdit basedataEdit = null;
	         switch(epayeeType) {
	         case "bd_supplier":
	            basedataEdit = (BasedataEdit)this.getControl("spic_epayeesupllier");
	            break;
	         
	         }
	         if (basedataEdit != null) {
	            basedataEdit.click();
	         }      
	      }
	
	 }
	 
	 private void selectEntryAccountByPayee() {
	       IDataModel model = this.getModel();
	       String payeeType = (String)model.getValue("spic_epayeetype",model.getEntryCurrentRowIndex("tallydetails"));
	       DynamicObject payer = null;
	       if (StringUtils.equals(payeeType, "bd_supplier")) {
	         
	          payer = (DynamicObject)model.getValue("spic_epayeesupllier",model.getEntryCurrentRowIndex("tallydetails"));
	       } 
	       if (payer != null) {
	          this.showBankInfo(payeeType, (Long)payer.getPkValue());
	       }
	 }
	 
	 private void showBankInfo(String payeeEntityName, Long payerId) {
	       ListShowParameter lsp = null;
	       switch(payeeEntityName) {
	       case "bd_supplier":
	          lsp = ListConstructorHelper.getSupplierBankInfoShowParameter(payerId);
	          break;
	       }
	       if (lsp != null) {
	          CloseCallBack closeCallBack = new CloseCallBack(this, "entrypayeeaccount");
	          lsp.setCloseCallBack(closeCallBack);
	          this.getView().showForm(lsp);
	       }
	 }
	 
	 @Override
	public void closedCallBack(ClosedCallBackEvent evt) {
		// TODO Auto-generated method stub
		super.closedCallBack(evt);
		IDataModel model = this.getModel();
		if(StringUtils.equals("entrypayeeaccount", evt.getActionId())) {
			Object returnData =  evt.getReturnData();
			int rowIndex = model.getEntryCurrentRowIndex("tallydetails");
			if (returnData != null) {
			   ListSelectedRowCollection rowInfo = (ListSelectedRowCollection)returnData;
			   String payeeType = (String)model.getValue("payeetype",rowIndex);
			   Object entryKey;
			   if (!StringUtils.equals("bos_org", payeeType) && !StringUtils.equals("er_payeer", payeeType)) {           
			      entryKey = rowInfo.getEntryPrimaryKeyValues()[0];
			   } else {
				  entryKey = rowInfo.getPrimaryKeyValues()[0];
			   }
			   AccountInfo accountInfo = ReimbursePayerAcctUtils.getBankInfo(payeeType, (Long)entryKey);
			   if (accountInfo != null) {
			      model.setValue("spic_epayeeaccount", accountInfo.getAccount(),rowIndex);
			      model.setValue("spic_epayeebank", accountInfo.getBeBank(),rowIndex);
			   }
			}
		}
	}
	 

}
