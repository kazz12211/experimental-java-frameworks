package core.util;

public class CharacterSet {

	private String characters;

	public static CharacterSet whitespaceCharacterSet = new CharacterSet(" \n\t\r");
	public static CharacterSet digitCharacterSet = new CharacterSet("0123456789");
	public static CharacterSet numberCharacterSet = new CharacterSet("+,-.0123456789");
	public static CharacterSet signCharacterSet = new CharacterSet("!#$%&()*+,-./:;<=>?@[]^_{|}");
	public static CharacterSet alphaCharacterSet = new CharacterSet("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
	public static CharacterSet zenkakuSpaceCharacterSet = new CharacterSet("\u3000");
	
	public CharacterSet(String characters) {
		this.characters = characters;
	}
	
	public boolean contains(char ch) {
		for(int i = characters.length() - 1; i >= 0; i--) {
			if(characters.charAt(i) == ch)
				return true;
		}
		return false;
	}
	
	public String characters() {
		return characters;
	}
	
	public CharacterSet join(CharacterSet charSet) {
		return new CharacterSet(this.characters + charSet.characters());
	}
	
	public String stringContainingCharacters(String string) {
		if(string == null)
			return null;
		StringBuffer buffer = new StringBuffer();
		int len = string.length();
		for(int i = 0; i < len; i++) {
			if(this.contains(string.charAt(i)))
				buffer.append(string.charAt(i));
		}
		return buffer.toString();
		
	}
}
