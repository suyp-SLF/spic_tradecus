package spic.cus.fi.voucher.opplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.property.EntryProp;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

public class VoucherPostSendToTaxCusOp extends AbstractOperationServicePlugIn {
	
	private static final String TAXSYS = "taxsys";
	
	@Override
	public void onPreparePropertys(PreparePropertysEventArgs e) {
		// TODO Auto-generated method stub
		super.onPreparePropertys(e);
		e.getFieldKeys().add("spic_issendtax");
		e.getFieldKeys().add("org");
		e.getFieldKeys().add("bookeddate");
		e.getFieldKeys().add("bizdate");
		e.getFieldKeys().add("localcur");
		e.getFieldKeys().add("vouchertype");
		e.getFieldKeys().add("booktype");
		e.getFieldKeys().add("bookeddate");
		e.getFieldKeys().add("entries");
		e.getFieldKeys().add("entries.edescription");
		e.getFieldKeys().add("entries.account");
		e.getFieldKeys().add("entries.entrydc");
		e.getFieldKeys().add("entries.currency");
		e.getFieldKeys().add("entries.debitori");
		e.getFieldKeys().add("entries.creditori");
		e.getFieldKeys().add("entries.localrate");
		e.getFieldKeys().add("entries.debitlocal");
		e.getFieldKeys().add("entries.creditlocal");
		e.getFieldKeys().add("entries.seq");
		
	}
	
	@Override
	public void afterExecuteOperationTransaction(AfterOperationArgs e) {
		// TODO Auto-generated method stub
		super.afterExecuteOperationTransaction(e);
		List<Object> pkIds = this.getOperationResult().getSuccessPkIds();
		DynamicObject[] datas = e.getDataEntities();
		List<DynamicObject> sendToTaxDatas = new ArrayList<DynamicObject>();//税务系统需要的凭证
		List<DynamicObject> taxVoucherMidDatas = new ArrayList<DynamicObject>();//生成的税务凭证中间数据
		for (DynamicObject data : datas) {
			if(pkIds.contains(data.getPkValue())) {
				data.set("spic_issendtax", true);
				sendToTaxDatas.add(data);
				
				String sourcebilltype = data.getString("sourcebilltype");
				String sourcebill = data.getString("sourcebill");
				
				DynamicObject taxVoucher = BusinessDataServiceHelper.newDynamicObject("spic_taxvoucher");
				if(StringUtils.equals(sourcebilltype, "fr_manualtallybi") && StringUtils.isEmpty(sourcebill)) {
					DynamicObject manualtallybi = BusinessDataServiceHelper.loadSingle(sourcebilltype,"",
							new QFilter[] {new QFilter("id", QCP.equals, sourcebill)});
					if(StringUtils.equals(manualtallybi.getString("spic_othersys"), TAXSYS)) {
						
						taxVoucher.set("", manualtallybi.get(""));
					}
				}
				setProp(taxVoucher,data);
				taxVoucherMidDatas.add(taxVoucher);
			}
		}
		if(!sendToTaxDatas.isEmpty() && !taxVoucherMidDatas.isEmpty()) {
			SaveServiceHelper.save(taxVoucherMidDatas.toArray(new DynamicObject[taxVoucherMidDatas.size()]));
			SaveServiceHelper.save(sendToTaxDatas.toArray(new DynamicObject[sendToTaxDatas.size()]));
		}
	}

	private void setProp(DynamicObject target, DynamicObject source) {
		// TODO Auto-generated method stub


		target.set("billno", source.get("billno"));
		target.set("org", source.getDynamicObject("org").getString("number"));
		target.set("bookeddate", source.get("bookeddate"));
		target.set("bizdate", source.get("bizdate"));
		target.set("localcur", source.getDynamicObject("localcur").getString("number"));
		target.set("vouchertype", source.getDynamicObject("vouchertype").getString("number"));
		target.set("booktype", source.getDynamicObject("booktype").getString("number"));
		target.set("createtime", new Date());
		target.set("status", "A");
		target.set("billstatus", "A");
		DynamicObjectCollection sourceEntryRows = source.getDynamicObjectCollection("entries");
		DynamicObjectCollection targetEntryRows = new DynamicObjectCollection();
		EntryProp prop = (EntryProp) EntityMetadataCache.getDataEntityType(target.getDataEntityType().getName())
                .findProperty("spic_entryentity");
        DynamicObjectType dt = prop.getDynamicCollectionItemPropertyType();

		for (DynamicObject sourceEntryrow : sourceEntryRows) {
			DynamicObject targetEntryrow = new DynamicObject(dt);
	        DataEntityPropertyCollection entryPropties = dt.getProperties();
	        for (IDataEntityProperty entryProp : entryPropties) {
	        	if(Arrays.asList("account","currency").contains(entryProp.getName())) {
	        		
	        		targetEntryrow.set(entryProp.getName(), sourceEntryrow.getDynamicObject(entryProp.getName()).getString("number"));
	        	}else {
	        		
	        		targetEntryrow.set(entryProp.getName(), sourceEntryrow.get(entryProp.getName()));
	        	}
	        	
			}
	        targetEntryRows.add(targetEntryrow);
		}
		target.set("spic_entryentity", targetEntryRows);
	}
}
