package spic.cus.common.autoinfo;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.context.RequestContext;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.org.model.OrgViewTypeEnum;
import kd.fi.fr.dto.BankAccountDTO;
import kd.fi.fr.helper.GLReimBillHelper;

import java.util.EventObject;

/**
 * 单据打开时，自动带出付款方、付款账号、付款银行，参考：总账付款申请单kd.fi.fr.formplugin.GLReimPayBillEditPlugin#payerChangeAction()
 *
 * @author Wu Yanqi
 */
public class OpenWithPayeeInfoFormPlugin extends AbstractBillPlugIn {

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        IDataModel model = this.getModel();
        Long orgId = RequestContext.get().getOrgId();
        boolean isBankRole = OrgUnitServiceHelper.checkOrgFunction(orgId, OrgViewTypeEnum.IS_BANKROLL.getViewType());
        if (isBankRole) {
            model.setValue("spic_payer", orgId);
        } else {
            Long entrustOrgId = OrgUnitServiceHelper.getToOrg("10", "08", orgId, false);
            if (entrustOrgId != null) {
                model.setValue("spic_payer", entrustOrgId);
            }
        }
        BankAccountDTO bankAccount = GLReimBillHelper.queryPayerAccount(orgId);
        if (bankAccount != null) {
            model.setValue("spic_payeraccount", bankAccount.getId());
            model.setValue("spic_payerbank", bankAccount.getBank());
        }
    }
}
