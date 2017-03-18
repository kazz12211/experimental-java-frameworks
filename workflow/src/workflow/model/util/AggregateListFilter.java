package workflow.model.util;

import java.util.List;

import core.util.ListFilter;

public class AggregateListFilter implements ListFilter<Object> {
	List<ListFilter<Object>> filters;
	
	public AggregateListFilter(List<ListFilter<Object>> filters) {
		this.filters = filters;
	}
	
	@Override
	public boolean filter(Object object) {
		for(ListFilter<Object> filter : filters) {
			if(filter.filter(object) == false)
				return false;
		}
		return true;
	}
}
