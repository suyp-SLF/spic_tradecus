package spic.cus.fi.common.businessitem;

import kd.bos.form.IFormView;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;

public class CusBeforeExpItemF7SelectListener implements BeforeF7SelectListener {

	 	private static final String expenseItemKey = "expenseitem";
	     public static final String DAILY_REIMBURSE_SSC = "er_dailyreimbursebil_ssc";
	     public static final String DAILY_REIMBURSE_SSC_ = "er_dailyreimbursebill_ssc";
	     private IFormView view;
	     private String billType;
	     private String accountancyOrg;
	     private String administrativeOrg;
	     private String businessItem;
	  
	     public CusBeforeExpItemF7SelectListener(IFormView view, String billType, String accountancyOrg, String administrativeOrg, String businessItem) {
	        this.view = view;
	        this.billType = billType;
	        this.accountancyOrg = accountancyOrg;
	        this.administrativeOrg = administrativeOrg;
	        this.businessItem = businessItem;
	     }
	  
	  
	  
	     public void beforeF7Select(BeforeF7SelectEvent arg0) {
	    	Object businessItemO = this.view.getModel().getValue(this.businessItem);
	    	if(null == businessItemO) {
	    		arg0.setCancel(true);
	    		view.showErrorNotification("请先选择业务类型");
	    	}
	        Object accountancyOrgO = this.view.getModel().getValue(this.accountancyOrg);
	        Object administrativeOrgO = this.view.getModel().getValue(this.administrativeOrg);
	        if (accountancyOrgO != null && administrativeOrgO != null ) {
	           Long accountancyOrgId = Long.parseLong(((DynamicObject)accountancyOrgO).getPkValue().toString());
	           Long administrativeOrgId = Long.parseLong(((DynamicObject)administrativeOrgO).getPkValue().toString());
	           
	        	   
	           Long businessItemId = businessItemO != null?Long.parseLong(((DynamicObject)businessItemO).getPkValue().toString()):null;

	           QFilter treeFilter = this.getExpItemTreeFilter(administrativeOrgId, businessItemId);
	           ListShowParameter cardF7Param = (ListShowParameter)arg0.getFormShowParameter();
	           cardF7Param.getListFilterParameter().getQFilters().add(treeFilter);
	           cardF7Param.getListFilterParameter().getQFilters().add(QFilter.of("isleaf=?", new Object[]{Boolean.TRUE}));
	           cardF7Param.setCustomParam("treeFilter", treeFilter.toSerializedString());
	           cardF7Param.setCustomParam("useOrgId", accountancyOrgId);
	           cardF7Param.setCaption(ResManager.loadKDString("业务项目", "BeforeExpItemF7SelectListener_0", "fi-fr-formplugin", new Object[0]));
	        }
	     }
	  
	  
	  
	  
	  
	  
	  
	  
	     public QFilter getExpItemTreeFilter(Long administrativeOrgId, Long busItemId) {
	        QFilter treeFilter = new QFilter("id", "in", this.getRelatedExpItemsByBillType());
	  
//	        treeFilter.and(new QFilter("id", "in", this.getRelatedExpItemsByDept(administrativeOrgId)));
	        if(null != busItemId) {
	        	
	        	treeFilter.and(new QFilter("id", "in", this.getRelatedExpItemsByBusItem(busItemId)));
	        }
	  
	        treeFilter.and(new QFilter("enable", "=", Boolean.TRUE));
	        return treeFilter;
	     }
	  
	  
	  
	  
	  
	  
	     public Set<Long> getRelatedExpItemsByBillType() {
	        Set<Long> relatedExpItemPks = new HashSet();
	  
	        if (this.billType != null && this.billType.indexOf("ssc") != -1) {
	           if ("er_dailyreimbursebil_ssc".equals(this.billType)) { 
	        	   this.billType = "er_dailyreimbursebill_ssc";         
	           } else {
	              this.billType = this.billType.replace("_ssc", "");
	           }     
	           
	        }
	        DynamicObject[] billTypes = BusinessDataServiceHelper.load("er_billtype", "id,number", new QFilter[]{new QFilter("number", "=", this.billType)});
	  
	        List<Long> billTypeId = new ArrayList(10);      

	        for(int i = 0; i < billTypes.length; ++i) {        
	        	DynamicObject billType = billTypes[i];
	           billTypeId.add(Long.parseLong(billType.getPkValue() + ""));
	        }
	        if (billTypeId.size() > 0) {
	           QFilter typeFilter = new QFilter("billtype", "=", billTypeId.get(0));
	           DynamicObject[] relatedExpItems = BusinessDataServiceHelper.load("er_expenseitembill", "expenseitem", new QFilter[]{typeFilter});
	           if (relatedExpItems != null && relatedExpItems.length > 0) {
	              relatedExpItemPks = (Set)Stream.of(relatedExpItems).map((v) -> {
	                 return Long.parseLong(v.getDynamicObject("expenseitem").getPkValue().toString());            
	              }).collect(Collectors.toSet());         
	           }     
	        }
	        System.out.println(relatedExpItemPks.size());
	        return (Set)relatedExpItemPks;
	     }
	  
	  
	  
	     //有点问题，和管控策略冲突
	     private List<Long> getRelatedExpItemsByDept(Long orgId) {
	        List<Long> relatedExpItemPks = new ArrayList(10);
	        List<Long> allOrgs = OrgUnitServiceHelper.getAdminOrgRelation(Collections.singletonList(orgId), true);
	        allOrgs.add(orgId);
	        DynamicObject[] reldepts = BusinessDataServiceHelper.load("bd_expitemreldept", "expenseitem, relorg", new QFilter[]{new QFilter("relorg", "in", allOrgs)}); 

	        for(int i = 0; i < reldepts.length; ++i) {         
	           DynamicObject item = reldepts[i];
	           DynamicObject expItem = (DynamicObject)item.get("expenseitem");
	           relatedExpItemPks.add(Long.parseLong(expItem.getPkValue() + ""));
	        }

	        return relatedExpItemPks;
	     }
	  
	  
	  
	  
	     private List<Long> getRelatedExpItemsByBusItem(Long busItemId) {
	        List<Long> relatedExpItemPks = new ArrayList(10);
	        DynamicObject[] reldepts = BusinessDataServiceHelper.load("bd_busitemrelexpitem_", "expenseitem", new QFilter[]{new QFilter("businessitem", "=", busItemId)});
	
	        for(int i = 0; i < reldepts.length; ++i) {        
	           DynamicObject item = reldepts[i];
	           DynamicObject expItem = (DynamicObject)item.get("expenseitem");
	           relatedExpItemPks.add(Long.parseLong(expItem.getPkValue() + ""));
	        }
	        return relatedExpItemPks;
	     }

}
