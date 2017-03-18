package universe;

import java.util.Arrays;
import java.util.List;

import universe.util.UniLogger;

public abstract class UniStoredQuery {

	public static UniStoredQuery union = new Union();
	public static UniStoredQuery intersect = new Intersect();
	public static UniStoredQuery diff = new Diff();
	
	protected UniStoredQuery() {
	}
	
	public int execute(UniObjectsSession session, List<String> listNames) throws Exception {
		int size = listNames.size();
		int listNumber = 3;
		int list1 = 1;
		int list2 = 2;

		UniLogger.universe_test.debug("listNames = " + listNames);
		if(size == 1) {
			return UniStoredQuery.executeSavedQuery(session, listNames.get(0), listNumber);
		} else {
			if(size == 2) {
				list1 = UniStoredQuery.executeSavedQuery(session, listNames.get(0), list1);
				list2 = UniStoredQuery.executeSavedQuery(session, listNames.get(1), list2);
				String command = generateCommandString(list1, list2, listNumber);
				session.executeUniCommand(command);
			} else if(size > 2) {
				list1 = UniStoredQuery.executeSavedQuery(session, listNames.get(0), list1);
				list2 = UniStoredQuery.executeSavedQuery(session, listNames.get(1), list2);
				String command = generateCommandString(list1, list2, listNumber);
				session.executeUniCommand(command);
				for(int i = 2; i < size; i++) {
					list1 = listNumber;
					list2 = UniStoredQuery.executeSavedQuery(session, listNames.get(i), 1);
					listNumber = listNumber == 3 ? 2 : 3;
					command = generateCommandString(list1, list2, listNumber);
					session.executeUniCommand(command);
				}
			}
		}
		return listNumber;
	}
	public int execute(UniObjectsSession session, String...listNames) throws Exception {
		List<String> names = Arrays.asList(listNames);
		return execute(session, names);
	}
	
	public int execute(UniObjectsSession session, int lastQuery, List<String> listNames) throws Exception {
		int size = listNames.size();
		int listNumber = 3;
		int list1 = 1;
		int list2 = 2;
		if(size == 1) {
			list1 = lastQuery;
			list2 = lastQuery == 2 ? 1 : 2;
			listNumber = lastQuery == 3 ? 4 : 3;
			list2 = UniStoredQuery.executeSavedQuery(session, listNames.get(0), list2);
			String command = generateCommandString(list1, list2, listNumber);
			session.executeUniCommand(command);
		} else {
			list1 = lastQuery;
			list2 = lastQuery == 2 ? 1 : 2;
			listNumber = lastQuery == 3 ? 4 : 3;
			list2 = UniStoredQuery.executeSavedQuery(session, listNames.get(0), list2);
			String command = generateCommandString(list1, list2, listNumber);
			session.executeUniCommand(command);
			for(int i = 1; i < size; i++) {
				list1 = listNumber;
				list2 = UniStoredQuery.executeSavedQuery(session, listNames.get(i), 1);
				listNumber = listNumber == 3 ? 2 : 3;
				command = generateCommandString(list1, list2, listNumber);
				session.executeUniCommand(command);
			}
		}
		return listNumber;
	}
	
	public int execute(UniObjectsSession session, int lastQuery, String...listNames) throws Exception {
		List<String> names = Arrays.asList(listNames);
		return execute(session, lastQuery, names);
	}
	
	abstract protected String generateCommandString(int list1, int list2, int returnList);
			
	public static class Union extends UniStoredQuery {

		public Union() {}
		
		@Override
		protected String generateCommandString(int list1, int list2,
				int returnList) {
			String command = "MERGE.LIST " + list1 + " UNION " + list2 + " TO " + returnList;
			return command;
		}
		
	}
	
	public static class Intersect extends UniStoredQuery {

		public Intersect() {}
		
		@Override
		protected String generateCommandString(int list1, int list2,
				int returnList) {
			String command = "MERGE.LIST " + list1 + " INTERSECT " + list2 + " TO " + returnList;
			return command;
		}
		
	}
	
	public static class Diff extends UniStoredQuery {

		public Diff() {}
		
		@Override
		protected String generateCommandString(int list1, int list2,
				int returnList) {
			String command = "MERGE.LIST " + list1 + " DIFF " + list2 + " TO " + returnList;
			return command;
		}
		
	}
	
	public static int executeSavedQuery(UniObjectsSession session, String listName, int listNumber) throws Exception {
		session.executeUniCommand("GET.LIST " + listName + " TO " + listNumber);
		return listNumber;

	}
}
