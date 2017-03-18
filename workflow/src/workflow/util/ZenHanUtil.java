package workflow.util;

import core.util.CharacterSet;

public final class ZenHanUtil {

	public static CharacterSet zenkakuDigitCharacterSet = new CharacterSet(ZenHan.sharedInstance().getZenkakuDigit());
	public static CharacterSet zenkakuNumberCharacterSet = new CharacterSet(ZenHan.sharedInstance().getZenkakuNumber());
	public static CharacterSet zenkakuSignCharacterSet = new CharacterSet(ZenHan.sharedInstance().getZenkakuKigou());
	public static CharacterSet zenkakuAlphabetCharacterSet = new CharacterSet(ZenHan.sharedInstance().getZenkakuAlphabet());

	public static final String convert(String string, CharacterSet set1, CharacterSet set2) {
		String cset1 = set1.characters();
		String cset2 = set2.characters();
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			int pos = cset1.indexOf(ch);
			if(pos >= 0) {
				buffer.append(cset2.charAt(pos));
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}
	
	public static final String convertZenkakuKanaToHankakuKana(String string) {
		StringBuffer buffer = new StringBuffer();
		String[] zenkaku = ZenHan.sharedInstance().getZenkakuKana();
		String[] hankaku = ZenHan.sharedInstance().getHankakuKana();
		for(int i = 0; i < string.length(); i++) {
			String ch = String.valueOf(string.charAt(i));
			String han = null;
			for(int j = 0; j < zenkaku.length; j++) {
				if(ch.equals(zenkaku[j])) {
					han = hankaku[j];
					break;
				}
			}
			if(han != null) {
				buffer.append(han);
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}
	
	public static final String convertHankakuKanaToZenkakuKana(String string) {
		StringBuffer buffer = new StringBuffer();
		String[] zenkaku = ZenHan.sharedInstance().getZenkakuKana();
		String[] hankaku = ZenHan.sharedInstance().getHankakuKana();
		for(int i = 0; i < string.length(); i++) {
			String ch = String.valueOf(string.charAt(i));
			String zen = null;
			for(int j = 0; j < hankaku.length; j++) {
				if(hankaku[j].startsWith(ch)) {
					if(hankaku[j].length() == 1) {
						zen = zenkaku[j];
						break;
					} else if(hankaku[j].length() == 2) {
						ch = string.substring(i, i+1);
						if(hankaku[j].equals(ch)) {
							zen = zenkaku[j];
							i++;
							break;
						}
					}
				}
			}
			if(zen != null) {
				buffer.append(zen);
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}
	
	public static final String convertHankakuToZenkaku(String string) {
		CharacterSet cset1 = CharacterSet.alphaCharacterSet.
				join(CharacterSet.digitCharacterSet).
				join(CharacterSet.signCharacterSet).
				join(new CharacterSet(" "));
		CharacterSet cset2 = zenkakuAlphabetCharacterSet.
				join(zenkakuDigitCharacterSet).
				join(zenkakuSignCharacterSet).
				join(CharacterSet.zenkakuSpaceCharacterSet);
		String str = convert(string, cset1, cset2);
		return convertHankakuKanaToZenkakuKana(str);
	}

	public static final String convertZenkakuToHankaku(String string) {
		String str1 = zenkakuAlphabetCharacterSet.characters() +
				zenkakuDigitCharacterSet.characters() +
				zenkakuSignCharacterSet.characters() +
				CharacterSet.zenkakuSpaceCharacterSet.characters();
		CharacterSet cset1 = new CharacterSet(str1);

		String str2 = CharacterSet.alphaCharacterSet.characters() +
				CharacterSet.digitCharacterSet.characters() + 
				CharacterSet.signCharacterSet.characters() +
				" ";
		CharacterSet cset2 = new CharacterSet(str2);
		String str = convert(string, cset1, cset2);
		return convertZenkakuKanaToHankakuKana(str);
	}
	
	public static final String convertHankakuSpaceToZenkakuSpace(String string) {
		CharacterSet cset1 = new CharacterSet(" ");
		CharacterSet cset2 = CharacterSet.zenkakuSpaceCharacterSet;
		return convert(string, cset1, cset2);
	}

	public static final String convertZenkakuSpaceToHankakuSpace(String string) {
		CharacterSet cset1 = CharacterSet.zenkakuSpaceCharacterSet;
		CharacterSet cset2 = new CharacterSet(" ");
		return convert(string, cset1, cset2);
	}
}
