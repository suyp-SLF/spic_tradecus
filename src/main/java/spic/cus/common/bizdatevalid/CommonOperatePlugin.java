package spic.cus.common.bizdatevalid;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.validate.AbstractValidator;

import java.util.Date;

/**
 * @author Wu Yanqi
 */
public class CommonOperatePlugin extends AbstractOperationServicePlugIn {

    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {
        // 记账日期业务校验，业务日期大于记账日期时，提示信息
        e.getValidators().add(new BizDateValidator());
    }

    class BizDateValidator extends AbstractValidator {
        @Override
        public void validate() {
            ExtendedDataEntity[] bills = this.getDataEntities();
            for (ExtendedDataEntity bill : bills) {
                DynamicObject dataEntity = bill.getDataEntity();
                Date bizdate = dataEntity.getDate("bizdate");
                Date spic_keepacctdate = dataEntity.getDate("spic_keepacctdate");
                if (bizdate.getTime() > spic_keepacctdate.getTime()) {
                    this.addErrorMessage(bill, "业务日期不能大于记账日期！");
                }
            }
        }
    }
}
