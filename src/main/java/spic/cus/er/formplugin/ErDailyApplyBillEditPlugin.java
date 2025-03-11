package spic.cus.er.formplugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListFilterParameter;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;

import java.util.EventObject;

/**
 * 需求：
 * 1. 费用申请单，单据类型为“招待费用申请单”，添加费用项目过滤，只显示业务招待费
 *
 * @author Wu Yanqi
 */
public class ErDailyApplyBillEditPlugin extends AbstractBillPlugIn implements BeforeF7SelectListener {

    private static final String AZDF = "FYXM009%"; // 业务招待费编码 ，加%是因为考虑到费用项目的个性化，个性化之后编码为FYXM009.xxx
    private static final String BT = "FYXM008%"; // 会议费编码

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit expenseitem = (BasedataEdit) this.getControl("expenseitem");
        expenseitem.addBeforeF7SelectListener(this);
    }

    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        // 费用申请单，单据类型为“招待费用申请单”，添加费用项目过滤，只显示业务招待费
        /*
        业务招待费编码：FYXM009
        单据类型：招待费用申请单（er_dailyapplybill_AZDF）
        */
        String triggerName = beforeF7SelectEvent.getProperty().getName();
        DynamicObject spic_billtypefield = (DynamicObject) this.getModel().getValue("spic_billtypefield"); // 获取单据类型
        if (spic_billtypefield != null && StringUtils.equals(triggerName, "expenseitem")) {
            String billTypeNumber = spic_billtypefield.getString("number");// 单据类型编码
            ListShowParameter showParameter = (ListShowParameter) beforeF7SelectEvent.getFormShowParameter();
            ListFilterParameter listFilterParameter = showParameter.getListFilterParameter();
            QFilter treeFilter = null;
            QFilter numberFilter = null;
            switch (billTypeNumber) {
                case "er_dailyapplybill_AZDF":
                    // 只可以选择到“业务招待费”
                    // 左树过滤
                    treeFilter = new QFilter("number", QCP.like, AZDF);
                    showParameter.setCustomParam("treeFilter", treeFilter.toSerializedString());
                    // 右表过滤
                    numberFilter = new QFilter("parent.number", QCP.like, AZDF).or(new QFilter("number", QCP.like, AZDF));
                    listFilterParameter.getQFilters().add(numberFilter);
                    break;
                case "er_dailyapplybill_BT":
                    // 只可以选择到“会议费”
                    // 左树过滤
                    treeFilter = new QFilter("number", QCP.like, BT);
                    showParameter.setCustomParam("treeFilter", treeFilter.toSerializedString());
                    // 右表过滤
                    numberFilter = new QFilter("parent.number", QCP.like, BT).or(new QFilter("number", QCP.like, BT));
                    listFilterParameter.getQFilters().add(numberFilter);
                    break;
                default: // er_dailyapplybill_Other
                    // 左树过滤
                    treeFilter = new QFilter("number", QCP.not_like, AZDF).and(new QFilter("number", QCP.not_like, BT));
                    showParameter.setCustomParam("treeFilter", treeFilter.toSerializedString());
                    // 右表过滤
                    numberFilter = new QFilter("parent.number", QCP.not_like, BT).or(new QFilter("number", QCP.not_like, BT));
                    listFilterParameter.getQFilters().add(numberFilter);
                    break;
            }
            showParameter.setCustomParam("iscontainlower", true);
        }
    }
}
