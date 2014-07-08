package simpleorm.dataset;

import java.util.ArrayList;
import java.util.List;

import simpleorm.utils.SException;
import simpleorm.utils.SUte;
 
public class SQueryResult<R extends SRecordGeneric> extends ArrayList<R> implements List<R>{

	private static final long serialVersionUID = 1L;
	private String sqlQuery;
	
	public SQueryResult(String query) {
		this.sqlQuery = query;
	}
	
	public R exactlyOne() {
		if (this.size() == 0)
			throw new SException.Data("Query failed to return any rows " + sqlQuery +SUte.arrayToString(this));
		if (this.size() > 1)
			throw new SException.Data("Query returned more than one row " + sqlQuery +SUte.arrayToString(this));
		return this.get(0);
	}
	
	public R oneOrNone() {
		if (this.size() > 1)
			throw new SException.Data("Query returned more than one row " + sqlQuery +SUte.arrayToString(this));
		if (this.size() == 0)
			return null;
		else
			return get(0);
	}

}