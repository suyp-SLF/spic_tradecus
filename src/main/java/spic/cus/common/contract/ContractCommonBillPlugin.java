package spic.cus.common.contract;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.operate.FormOperate;
import kd.bos.mvc.bill.BillModel;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;

import java.util.EventObject;

/**
 * 1. 重复关联合同，付款计划行重复问题；
 * 2. 合同号根据组织过滤；
 * 3. 查看合同按钮弹框；
 */
public class ContractCommonBillPlugin extends AbstractBillPlugIn implements BeforeF7SelectListener {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit spic_purcontractno = (BasedataEdit) this.getControl("spic_purcontractno"); // 合同F7添加过滤，通用于采购合同、销售合同
        spic_purcontractno.addBeforeF7SelectListener(this);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String key = evt.getItemKey();

        if (StringUtils.equals(key, "spic_viewpurcontract") || StringUtils.equals(key, "spic_viewsalcontract")) {
            String contractEntity = StringUtils.equals(key, "spic_viewpurcontract") ? "conm_purcontract" : "conm_salcontract";
            String contractField = StringUtils.equals(key, "spic_viewpurcontract") ? "spic_purcontractno" : "spic_salcontractno";
            DynamicObject contract = (DynamicObject) this.getModel().getValue(contractField);
            if (null != contract) {
                BillShowParameter fsp = new BillShowParameter();
                fsp.setFormId(contractEntity);
                fsp.getOpenStyle().setShowType(ShowType.Modal);
                fsp.setPkId(contract.getPkValue());
                this.getView().showForm(fsp);
            } else {
                this.getView().showErrorNotification("请先选择合同");
            }
        }
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate source = (FormOperate) args.getSource();
        String operateKey = source.getOperateKey();
        // 关联合同为上拉，付款计划行重复问题解决；标准产品只支持下推的方式
        if (StringUtils.equals(operateKey, "draw_purcontract")) {
//			this.getModel().deleteEntryData("detailentry");
            this.getModel().deleteEntryData("planentity");
        }
        System.out.println("ContractCommonBillPlugin.beforeDoOperation");
    }


    @Override
    public void beforeF7Select(BeforeF7SelectEvent beforeF7SelectEvent) {
        String name = beforeF7SelectEvent.getProperty().getName();
        if (StringUtils.equals(name, "spic_purcontractno")) {
            // 根据结算组织，往来单位过滤
            String entityName = this.getModel().getDataEntityType().getName();
            BillModel model = (BillModel) this.getModel();
            QFilter qFilter = null;
            DynamicObject asstact = null;
            DynamicObject org = null;
            switch (entityName) {
                case "ap_finapbill": // 财务应付单->应付结算单
                case "ar_finarbill": // 财务应收单
                    asstact = (DynamicObject) model.getValue("asstact");
                    break;
                case "ar_invoice": // 开票单
                case "fa_card_real": // 实物卡片
                case "fa_purchasebill": // 采购转固单
                    break;
                case "ap_payapply": // 付款申请单
                    org = (DynamicObject) model.getValue("settleorg");
                    break;
                case "fr_glreim_recbill": // 总账收款单
                case "fr_glreim_paybill": // 总账付款单
                    org = (DynamicObject) model.getValue("accountingorg");
                    break;
                default:
                    break;
            }
            if (asstact != null) {
                qFilter = new QFilter("org.number", QCP.equals, asstact.getString("number"));
            } else if (org == null) {
                org = (DynamicObject) model.getValue("org");
            }
            if (org != null) {
                qFilter = new QFilter("org.number", QCP.equals, org.getString("number"));
            }
            if (qFilter != null) {
                beforeF7SelectEvent.getCustomQFilters().add(qFilter);
            }
        }
    }
}
