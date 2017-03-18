package universe;

import java.util.List;

import core.util.ListUtils;
import universe.command.AggregateFunction;
import universe.command.AggregateFunctions;
import universe.command.Count;
import universe.command.Select;
import universe.util.UniLogger;
import asjava.uniobjects.UniCommand;
import asjava.uniobjects.UniSessionException;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniCommandGeneration {

	private UniObjectsSession _session;

	public UniCommandGeneration(UniObjectsSession session) {
		this._session = session;
	}
	
	public UniCommand generate(Select select) throws UniSessionException {
		String commandString;
		UniQuerySpecification spec = select.querySpecification();
		StringBuffer buffer = new StringBuffer();
		buffer.append("SELECT ");
		buffer.append(spec.entity().filename());
		
		if(spec.sortOrderings() != null && spec.sortOrderings().size() > 0) {
			List<String> strings = ListUtils.list();
			for(UniSortOrdering so : spec.sortOrderings()) {
				strings.add(so.generateString(spec.entity()));
			}
			String orderby = ListUtils.listToString(strings, " ");
			if(orderby != null) {
				buffer.append(" ");
				buffer.append(orderby);
			}
		}

		if(spec.predicate() != null) {
			String condition = spec.predicate().generateString(spec.entity());
			if(condition != null) {
				buffer.append(" WITH ");
				buffer.append(condition);
			}
		}
		
		if(spec.fetchHint() != null && spec.fetchHint().get(UniQuerySpecification.FetchSample) != null) {
			Object value = spec.fetchHint().get(UniQuerySpecification.FetchSample);
			buffer.append(" SAMPLED " + value.toString());
		}
		if(spec.storedQueryNumber() != null) {
			buffer.append(" FROM " + spec.storedQueryNumber().toString());
		}
		buffer.append(" TO " + select.listNumber());
		commandString = buffer.toString();

		return createCommand(commandString);
	}

	public UniCommand generate(AggregateFunctions aggregateFunction) throws UniSessionException {
		UniQuerySpecification spec = aggregateFunction.querySpecification();
		UniEntity entity = spec.entity();
		UniField field = entity.fieldNamed(aggregateFunction.key());
		StringBuffer buffer = new StringBuffer();
		buffer.append("LIST ");
		buffer.append(entity.filename());
		if(field != null) {
			buffer.append(" TOTAL "); buffer.append(field.columnName());
			buffer.append(" MAX "); buffer.append(field.columnName());
			buffer.append(" MIN "); buffer.append(field.columnName());
			buffer.append(" AVG "); buffer.append(field.columnName());
		} else {
			buffer.append(" TOTAL "); buffer.append(aggregateFunction.key());
			buffer.append(" MAX "); buffer.append(aggregateFunction.key());
			buffer.append(" MIN "); buffer.append(aggregateFunction.key());
			buffer.append(" AVG "); buffer.append(aggregateFunction.key());
		}
		if(spec.predicate() != null) {
			String condition = spec.predicate().generateString(entity);
			buffer.append(" WITH ");
			buffer.append(condition);
		}
		if(spec.fetchHint() != null && spec.fetchHint().get(UniQuerySpecification.FetchSample) != null) {
			Object value = spec.fetchHint().get(UniQuerySpecification.FetchSample);
			buffer.append(" SAMPLED " + value.toString());
		}
		buffer.append(" DET.SUP");
		
		String commandString = buffer.toString();
		return createCommand(commandString);
	}

	public UniCommand generate(Count function) throws UniSessionException {
		UniQuerySpecification spec = function.querySpecification();
		UniEntity entity = spec.entity();
		StringBuffer buffer = new StringBuffer();
		buffer.append("COUNT ");
		buffer.append(entity.filename());
		
		if(spec.predicate() != null) {
			String condition = spec.predicate().generateString(entity);
			buffer.append(" WITH ");
			buffer.append(condition);
		}
		
		if(spec.fetchHint() != null && spec.fetchHint().get(UniQuerySpecification.FetchSample) != null) {
			Object value = spec.fetchHint().get(UniQuerySpecification.FetchSample);
			buffer.append(" SAMPLED " + value.toString());
		}
		String commandString = buffer.toString();
		return createCommand(commandString);
	}

	public UniCommand generate(AggregateFunction aggregateFunction) throws UniSessionException {
		UniQuerySpecification spec = aggregateFunction.querySpecification();
		UniEntity entity = spec.entity();
		UniField field = entity.fieldNamed(aggregateFunction.key());
		StringBuffer buffer = new StringBuffer();
		buffer.append("LIST ");
		buffer.append(entity.filename());
		buffer.append(" "); buffer.append(aggregateFunction.functionName().toUpperCase()); buffer.append(" ");  buffer.append(field.columnName());
		
		if(spec.predicate() != null) {
			String condition = spec.predicate().generateString(entity);
			buffer.append(" WITH ");
			buffer.append(condition);
		}
		if(spec.fetchHint() != null && spec.fetchHint().get(UniQuerySpecification.FetchSample) != null) {
			Object value = spec.fetchHint().get(UniQuerySpecification.FetchSample);
			buffer.append(" SAMPLED " + value.toString());
		}
		buffer.append(" DET.SUP");
		
		String commandString = buffer.toString();
		return createCommand(commandString);
	}
	
	private UniCommand createCommand(String commandString) throws UniSessionException {
		if(!_session.isConnected())
			try {
				_session.establishConnection();
			} catch (Exception e) {
				UniLogger.universe_command.error("UniCommandGeneration could not create command '" + commandString + "'");
			}
		
		return _session.uniSession().command(commandString);
	}

}
