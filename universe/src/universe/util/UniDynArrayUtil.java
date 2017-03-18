package universe.util;

import java.util.ArrayList;
import java.util.List;

import asjava.uniclientlibs.UniDataSet;
import asjava.uniclientlibs.UniDynArray;
import asjava.uniclientlibs.UniException;
import asjava.uniclientlibs.UniRecord;
import asjava.uniclientlibs.UniString;
import asjava.uniobjects.UniFile;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniDynArrayUtil {

	public static List<String> readDynArray(UniFile file, UniString recordId, String fieldName) throws UniException {
		UniDataSet rowSet = new UniDataSet();
		rowSet.append(UniStringUtil.coerceToString(recordId));
		return readDynArray(file, rowSet, fieldName);
	}
	public static List<String> readDynArray(UniFile file, UniString recordId, String fieldName, String encoding) throws UniException {
		UniDataSet rowSet = new UniDataSet();
		rowSet.append(UniStringUtil.guessEncodingAndCoerceToString(recordId, encoding));
		return readDynArray(file, rowSet, fieldName);
	}
	
	public static List<String> readDynArray(UniFile file, UniDataSet rowSet, String fieldName) throws UniException {
		UniDataSet dataSet = file.readNamedField(rowSet, fieldName);
		UniRecord record = dataSet.getUniRecord();
		UniDynArray array = new UniDynArray(record.getRecord());
		int size = array.dcount(1);
		List<String> result = new ArrayList<String>();
		for(int i = 1; i <= size; i++) {
			UniString value = array.extract(1, i);
			result.add(UniStringUtil.coerceToString(value));
		}
		return result;
	}
	public static List<String> readDynArray(UniFile file, UniDataSet rowSet, String fieldName, String encoding) throws UniException {
		UniDataSet dataSet = file.readNamedField(rowSet, fieldName);
		UniRecord record = dataSet.getUniRecord();
		UniDynArray array = new UniDynArray(record.getRecord());
		int size = array.dcount(1);
		List<String> result = new ArrayList<String>();
		for(int i = 1; i <= size; i++) {
			UniString value = array.extract(1, i);
			result.add(UniStringUtil.guessEncodingAndCoerceToString(value, encoding));
		}
		return result;
	}

	public static List<String> toStringList(UniDynArray array) {
		return toStringList(array, 1);
	}
	public static List<String> toStringList(UniDynArray array, String encoding) {
		return toStringList(array, 1, encoding);
	}

	public static List<String> toStringList(UniDynArray array, int level) {
		List<String> list = new ArrayList<String>();
		int size = array.dcount(level);
		for(int i = 1; i <= size; i++) {
			UniString value = array.extract(level, i);
			list.add(UniStringUtil.coerceToString(value));
		}
		return list;
	}

	public static List<String> toStringList(UniDynArray array, int level, String encoding) {
		List<String> list = new ArrayList<String>();
		int size = array.dcount(level);
		for(int i = 1; i <= size; i++) {
			UniString value = array.extract(level, i);
			list.add(UniStringUtil.guessEncodingAndCoerceToString(value, encoding));
		}
		return list;
	}

}
