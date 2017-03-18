package ariba.ui.meta.persistence;

import ariba.ui.meta.persistence.Predicate;
import ariba.ui.meta.persistence.QueryGenerator;
import ariba.util.core.Fmt;

public class NullPredicate extends Predicate {

	private String key;
	private boolean negate;
	
	public NullPredicate(String key, boolean negate) {
		super();
		this.key = key;
		this.negate = negate;
	}
	
	@Override
	public void generate(QueryGenerator generator) {
		if(negate) {
			generator.appendToWhere(Fmt.S("%s IS NOT NULL", key));
		} else {
			generator.appendToWhere(Fmt.S("%s IS NULL", key));
		}
	}

}
