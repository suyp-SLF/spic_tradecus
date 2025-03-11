package spic.cus.fi.common.utils;

import java.util.Iterator;

import kd.bd.master.consts.GroupConst;
import kd.bd.master.consts.query.QueryReferenceIdentifyConst;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;

public class CusOrgUtils {

	//获取最近一个基础数据管控单元
	public static Long getCtrlOrgId(Long orgId, String entity) {
		DynamicObject ctrlview = BaseDataServiceHelper.getCtrlview(entity);
		if(null != ctrlview) {
			Long ctrlviewId = ctrlview.getLong(GroupConst.PROP_ID);
			return getCtrlOrgId(orgId,ctrlviewId,entity,null);
		}
		return null;
	}
	
	
	private static Long getCtrlOrgId(Long orgId,Long ctrlviewId,String entity,ORM ormOv) {
		String bdCtrlStrgy = BaseDataServiceHelper.getBdCtrlStrgy(entity, orgId.toString());
		if(!StringUtils.isEmpty(bdCtrlStrgy)) {
			return orgId;
		}else {
			Long parentid = 0L;
			ORM ormOb = ormOv == null ? ORM.create(): ormOv;
			try(DataSet ds = ormOv.queryDataSet("getorgParent",QueryReferenceIdentifyConst.ID_ORG_STRUCTURE,
					"parent",new QFilter[] {
							new QFilter("org", QCP.equals, orgId),
							new QFilter("view.id", QCP.equals, ctrlviewId)
			})){for(Iterator<Row> iter = ds.iterator();iter.hasNext();) {
					Row row = iter.next();
					if(!StringUtils.isEmpty(row.getString("parent"))) {
							parentid = row.getLong("parent");
					}
				}
			}
			
			return parentid == 0L ? null : getCtrlOrgId(parentid, ctrlviewId, entity, ormOb);
		}
	}
}
