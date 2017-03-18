package workflow.tools;

import java.util.List;
import java.util.Map;

import core.util.ListUtils;
import core.util.MapUtils;
import workflow.util.PropertyList;

public class UserDictionary {
	
	private static final String PATH = "/opt/local/workflow/userdict/";
	private static final String PREFIX = "userdic_";
	private static final String SUFFIX = ".dict";
	private static final String WORDS = "words";
	
	PropertyList plist;
	String filename;
	
	public UserDictionary(String userId) {
		filename = PATH + PREFIX + userId + SUFFIX;
	}
	
	public void load() throws Exception {
		plist = new PropertyList();
		plist.initWithPath(filename);
	}
	
	public void save() throws Exception {
		plist.saveToPath(filename);
	}
	
	public Map<String, List<String>> category(String category) {
		Map<String, List<String>> cat = (Map<String, List<String>>) plist.get(category);
		if(cat == null) {
			cat = MapUtils.map();
			plist.add(category, cat);
			List<String> words = (List<String>) cat.get(WORDS);
			if(words == null) {
				words = ListUtils.list();
				cat.put(WORDS, words);
			}
		}
		return cat;
	}
	
	public List<String> words(String category) {
		Map<String, List<String>> cat = category(category);
		List<String> words = (List<String>) cat.get(WORDS);
		return words;
	}
	
	public void add(String word, String category) {
		Map<String, List<String>> cat = category(category);
		List<String> words = (List<String>) cat.get(WORDS);
		words.add(word);
		cat.put(WORDS, words);
	}
	
	public void remove(String word, String category) {
		Map<String, List<String>> cat = category(category);
		List<String> words = (List<String>) cat.get(WORDS);
		words.remove(word);
		cat.put(WORDS, words);
	}
}
