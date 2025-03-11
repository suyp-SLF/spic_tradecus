package spic.cus.fi.finapbill.formplugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.AfterAddRowEventArgs;

import java.util.EventObject;

public class FinapbillCusEditPlugin extends AbstractBillPlugIn {

    @Override
    public void afterCreateNewData(EventObject e) {
        // TODO Auto-generated method stub
        super.afterCreateNewData(e);
        int rowCount = this.getModel().getEntryRowCount("detailentry");
        if (rowCount > 0) {
            //费用承担部门默认值
//			Long orgId  = RequestContext.get().getOrgId();
//			if(0L != orgId && null != orgId) {
//				this.getModel().setValue("spic_entrycostdept", orgId, 0);
//			}
            // 设置为单据头上部门的值，仿照费用项目基础资料中的赋值方式实现
            DynamicObject spic_department = (DynamicObject) this.getModel().getValue("spic_department"); // 获取到部门值
            this.getModel().setValue("spic_entrycostdept", spic_department == null ? null : spic_department.getPkValue(), 0);

//			Map<String, Object> userMap = CommonServiceHelper.getUserMap(UserServiceHelper.getCurrentUserId());
//			if (userMap != null && userMap.get("org") != null) {
//				this.getModel().setValue("spic_entrycostdept", userMap.get("org"), 0);
//			}
        }
    }

    @Override
    public void afterAddRow(AfterAddRowEventArgs e) {
        // TODO Auto-generated method stub
        super.afterAddRow(e);
        int rownum = e.getRowDataEntities()[0].getRowIndex();
//        Map<String, Object> userMap = CommonServiceHelper.getUserMap(UserServiceHelper.getCurrentUserId());
        // 费用承担部门默认值
        // 设置为单据头上部门的值
        DynamicObject spic_department = (DynamicObject) this.getModel().getValue("spic_department"); // 获取到部门值
        this.getModel().setValue("spic_entrycostdept", spic_department == null ? null : spic_department.getPkValue(), rownum);
//		if (userMap != null && userMap.get("org") != null) {
//			this.getModel().setValue("spic_entrycostdept", userMap.get("org"), rownum);
//
//		}
//		Long orgId  = RequestContext.get().getOrgId();
//		if(0L != orgId && null != orgId) {
//			this.getModel().setValue("spic_entrycostdept", orgId, rownum);
//		}
    }
}
