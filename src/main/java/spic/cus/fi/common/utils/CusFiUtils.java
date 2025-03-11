package spic.cus.fi.common.utils;

import java.math.BigDecimal;
import java.util.Date;

import kd.bos.servicehelper.basedata.BaseDataServiceHelper;

public class CusFiUtils {
	
	public static  BigDecimal getExchangeRate(Long exratetableId, Long srcCurrencyId, Long destCurrencyId, Date exrateDate) {
	      BigDecimal exchangeRate = BigDecimal.ONE;
	      if (exrateDate == null) {         exrateDate = new Date();      }
	      if (srcCurrencyId != null && srcCurrencyId != 0L && destCurrencyId != null && destCurrencyId != 0L && !srcCurrencyId.equals(destCurrencyId)) {
	   	   exchangeRate = BaseDataServiceHelper.getExchangeRate(exratetableId, srcCurrencyId, destCurrencyId, exrateDate);
	      }

	      return exchangeRate;
	   }
	

}
