package spic.cus.bd.supplier.formplugin;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.control.Control;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.BillList;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.business.datamodel.DynamicFormModelProxy;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
import kd.fi.cas.helper.OperateServiceHelper;
import spic.cus.fi.common.utils.CusOrgUtils;

public class BizpartnerListF7FormPlugin extends AbstractFormPlugin {

	@Override
	public void registerListener(EventObject e) {
		// TODO Auto-generated method stub
		super.registerListener(e);
		this.addClickListeners("spic_createcus","spic_createsup");
	}
	
	@Override
	public void click(EventObject evt) {
		// TODO Auto-generated method stub
		super.click(evt);
		Control source = (Control)evt.getSource();
		String key = source.getKey();
		if(StringUtils.equals(key, "spic_createcus") || StringUtils.equals(key, "spic_createsup")) {
			
			String targetEntityType = StringUtils.equals(key, "spic_createcus")? "bd_customer":"bd_supplier";
			generateCusOrSup(targetEntityType);

		}
		
	}

	private void generateCusOrSup(String targetEntityType) {
		// TODO Auto-generated method stub
		BillList list = this.getControl("billlistap");
		ListSelectedRowCollection selectRows = list.getSelectedRows();
		Object[] pkValues = selectRows.getPrimaryKeyValues();
		Map<Object, DynamicObject> bizpartnersMap = BusinessDataServiceHelper.loadFromCache(pkValues, "bd_bizpartner");
		List<DynamicObject> newAsstacts = new ArrayList<DynamicObject>();
		ListShowParameter param = (ListShowParameter) getView().getFormShowParameter();
		Long useOrgId = param.getUseOrgId();
		Long createOrgId = CusOrgUtils.getCtrlOrgId(useOrgId, targetEntityType);
		DynamicObject createOrg = BusinessDataServiceHelper.loadSingle(createOrgId, "bos_org");
		if(null != createOrg) {
			String bdCtrlStrgy = BaseDataServiceHelper.getBdCtrlStrgy(targetEntityType, createOrgId.toString());
			Set<Object> bizpartnerIds = bizpartnersMap.keySet();
			DynamicObject[] targetEntities = BusinessDataServiceHelper.load(targetEntityType, "bizpartner",
					new QFilter[] {new QFilter("bizpartner.id", QCP.in, bizpartnerIds),
								   new QFilter("createorg.id", QCP.equals, createOrgId)});
			for (DynamicObject targetEntity : targetEntities) {
				bizpartnerIds.remove(targetEntity.getDynamicObject("bizpartner").getPkValue());
			}
			for (Object bizpartnerId : bizpartnerIds) {
				Map<Class<?>, Object> services = new HashMap<>();
				DynamicFormModelProxy model = new DynamicFormModelProxy(targetEntityType, UUID.randomUUID().toString(), services);
				Object one = model.createNewData();
				DynamicObject newAsstact = model.getDataEntity();
				newAsstact.set("createOrg", createOrg);
				newAsstact.set("ctrlstrategy", bdCtrlStrgy);
				setValue(newAsstact,bizpartnersMap.get(bizpartnerId));
				newAsstacts.add(newAsstact);
			}
			if(newAsstacts.size()>0) {
				
				OperationResult result = OperateServiceHelper.execOperate("save", targetEntityType, newAsstacts.toArray(new DynamicObject[newAsstacts.size()]), null);
				List<Object> successPkIds = result.getSuccessPkIds();
				if(successPkIds.size()>0) {
					
					OperationResult submitResult = OperateServiceHelper.execOperate("submit", targetEntityType, successPkIds.toArray(new DynamicObject[successPkIds.size()]), null);
					List<Object> submitsuccessPkIds = submitResult.getSuccessPkIds();
					if(submitsuccessPkIds.size()>0) {
						
						OperationResult auditResult = OperateServiceHelper.execOperate("audit", targetEntityType, submitsuccessPkIds.toArray(new DynamicObject[submitsuccessPkIds.size()]), null);
						DynamicObject[] successAsstacts = BusinessDataServiceHelper.load(targetEntityType, "name,number", new QFilter[]{new QFilter("id", QCP.in, auditResult.getSuccessPkIds())});
						StringBuilder mes = new StringBuilder();
						for (DynamicObject successAsstact : successAsstacts) {
							mes.append(successAsstact.getString("name"));
							mes.append(",");
							
						}
						if(mes.length()>0) {
							
							mes.deleteCharAt(mes.length()-1);
						}
						this.getView().showMessage(mes.toString());
					}
				}
			}
		}else {
			this.getView().showMessage("未找到有控制规则的组织");
		}
		
		
	}

	private void setValue(DynamicObject target, DynamicObject source) {
		// TODO Auto-generated method stub
		String[] propList = new String[] {"number","name","simplename","type","internal_company","country","societycreditcode","tx_register_no",
				"artificialperson","regcapital","businessterm","businessscope","establishdate","linkman","postal_code","url",
				"admindivision","orgcode","biz_register_num","idno"};
		for (String prop : propList) {
			target.set(prop, source.get(prop));
		}
		target.set("bizpartner_phone", source.get("phone"));
		target.set("bizpartner_fax", source.get("fax"));
		
	}
	
	
	

}
