package spic.cus.common.botp;

import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.WorkflowPlugin;

import java.util.*;

/**
 * 工作流程插件，自动获取单据体中“项目"基础资料中的负责人作为参与人
 *
 * @author Wu Yanqi
 */
public class GetHeaduserOfProjectPlugin extends WorkflowPlugin {

    @Override
    public List<Long> calcUserIds(AgentExecution execution) {
        String businessKey = execution.getBusinessKey();
        String selectFields = null;
        String entityNumber = execution.getEntityNumber();
        switch (entityNumber) {
            case "er_tripreqbill": // 出差申请单
            case "er_tripreimbursebill": // 差旅报销单
                selectFields = "tripentry.std_project.spic_headuser.id";
                break;
            case "er_dailyapplybill": // 费用申请单
            case "er_dailyloanbill": // 借款单
            case "er_dailyreimbursebill": // 费用报销单
                selectFields = "expenseentryentity.std_project.spic_headuser.id";
                break;
            case "ar_busbill": // 暂估应收单
            case "ar_finarbill": // 财务应收单
                selectFields = "entry.project.spic_headuser.id";
                break;
            case "ar_invoice": // 开票单
            case "ar_revcfmbill": // 收入确认单
                selectFields = "entry.spic_project.spic_headuser.id";
                break;
            case "ap_busbill": // 暂估应付单
            case "ap_payapply": // 付款申请单
                selectFields = "entry.project.spic_headuser.id";
                break;
            case "ap_finapbill": // 财务应付单
                selectFields = "detailentry.project.spic_headuser.id";
                break;
            case "ssc_tallyapplybill": // 记账申请单
                selectFields = "tallyentryenity.spic_project.spic_headuser.id";
                break;
            default:
                break;
        }

        // 获取当前单据的所有分录的项目
        if (StringUtils.isNotBlank(entityNumber) && StringUtils.isNotBlank(selectFields)) {
            DynamicObjectCollection ap_finapbills = QueryServiceHelper.query(entityNumber, selectFields, new QFilter[]{new QFilter("id", QCP.equals, businessKey)});
            // 获取分录中的项目的负责人
            Set<Long> spic_headuserIds = new HashSet<Long>();
            String finalSelectFields = selectFields;
            ap_finapbills.forEach(entity -> {
                long headuserid = entity.getLong(finalSelectFields);
                if (headuserid != 0L) {
                    spic_headuserIds.add(headuserid);
                }
            });
            return new ArrayList<>(spic_headuserIds);
        }
        return Collections.emptyList();
    }
}
