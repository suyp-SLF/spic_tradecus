package spic.cus.common.adddept;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.ObjectUtils;
import kd.bos.entity.datamodel.IBillModel;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListFilterParameter;
import kd.bos.list.ListShowParameter;
import kd.bos.org.api.IOrgService;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.ServiceFactory;
import kd.bos.servicehelper.org.OrgViewType;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.fi.bcm.fel.common.StringUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应付结算单界面添加部门（行政组织）
 *
 * @author Wu Yanqi
 */
public class CommonAddDepartmentPlugin extends AbstractBillPlugIn implements BeforeF7SelectListener {
    /*
    1. 查询行政组织下的当前人员的部门信息
     */

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit department = (BasedataEdit) this.getControl("spic_department");
        department.addBeforeF7SelectListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        IBillModel model = (IBillModel) this.getModel();
        DynamicObject department = (DynamicObject) model.getValue("spic_department");
        if (ObjectUtils.isEmpty(department)) {
            long currentUserId = UserServiceHelper.getCurrentUserId(); // 获得当前用户的id
            String entityName = model.getDataEntityType().getName(); // 获取当前的单据标识，7张单据
            DynamicObject org = null;
            switch (entityName) {
                case "ap_payapply":
                    org = (DynamicObject) this.getModel().getValue("settleorg");
                    break;
                case "ar_busbill":
                case "ar_invoice":
                case "ar_finarbill":
                case "ap_invoice":
                case "ap_busbill":
                case "ap_finapbill":
                default:
                    org = (DynamicObject) this.getModel().getValue("org");
                    break;
            }
            if (org != null) {
                Long orgId = org.getLong("id");
                List<Long> allDepartmentByUserId = UserServiceHelper.getAllDepartmentByUserId(currentUserId);
                IOrgService service = ServiceFactory.getService(IOrgService.class);
                List<Long> orgIds = allDepartmentByUserId.stream().filter(id -> service.isParentOrg(OrgViewType.Admin, orgId, id)).collect(Collectors.toList());
                if (orgIds.size() > 0) {
                    this.getModel().setValue("spic_department", orgIds.get(0));
                }
            }
        }
    }


    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        String triggerName = beforeF7SelectEvent.getProperty().getName();
        // 部门F7窗口过滤
        if (StringUtils.equals(triggerName, "spic_department")) {
            ListShowParameter showParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            List<Long> allDepartmentByUserId = UserServiceHelper.getAllDepartmentByUserId(UserServiceHelper.getCurrentUserId());
            IDataModel model = this.getModel();
            String entityName = model.getDataEntityType().getName();
            DynamicObject org = null;
            switch (entityName) {
                case "ap_payapply":
                    org = (DynamicObject) this.getModel().getValue("settleorg");
                    break;
                case "ar_busbill":
                case "ar_invoice":
                case "ar_finarbill":
                case "ap_invoice":
                case "ap_busbill":
                case "ap_finapbill":
                default:
                    org = (DynamicObject) this.getModel().getValue("org");
                    break;
            }
            if (org != null) {
//                Long rootId = org.getLong("id");
//                showParameter.setCustomParam("rootId", rootId);
                IOrgService service = ServiceFactory.getService(IOrgService.class);
                List<Long> orgIds = new ArrayList<>();
                allDepartmentByUserId.forEach(id -> {
//                    if (service.isParentOrg(OrgViewType.Admin, rootId, id)) {
                    orgIds.add(id);
//                    }
                });
                showParameter.setCustomParam("range", new HashSet<>(orgIds));
                showParameter.setCustomParam("isIncludeAllSub", true);
                // 右表过滤，展示当前用户是/否兼职的部门
                ListFilterParameter listFilterParameter = showParameter.getListFilterParameter();
                QFilter departIds = new QFilter("id", QCP.in, allDepartmentByUserId);
                listFilterParameter.getQFilters().add(departIds);
            } else {
                this.getView().showTipNotification("请先选择结算组织！");
                beforeF7SelectEvent.setCancel(true);
            }
        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        String key = e.getProperty().getName();
        ChangeData[] changeData = e.getChangeSet();
        Object newValue = changeData[0].getNewValue();
        Object oldValue = changeData[0].getOldValue();
        String orgId = StringUtils.equals(this.getModel().getDataEntityType().getName(), "ap_payapply") ? "settleorg" : "org";
        // 结算组织改变时，清空部门信息
        if (newValue != oldValue && (StringUtils.equals(key, orgId))) {
            this.getModel().setValue("spic_department", null);
        }
    }
}
