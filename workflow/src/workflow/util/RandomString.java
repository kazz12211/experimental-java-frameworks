package workflow.util;

import java.util.Random;

public final class RandomString {
	
	public static String generate(char charset[], int length) {
		char buffer[] = new char[length+1];
		int size = 0;
		String retVal = null;
		int charsetLen = charset.length;
		int div = length / 4;
		int mod = length % 4;
		
		Random seed = randomize();
		
		for(int i = 0; i < length+1 ; i++)
			buffer[i] = (char)0;
		
		for(int i = 0; i < div; i++) {
			for(int len = 0; len < 4; len++) {
				long num = seed.nextLong();
				int index = (int)(num % (long)charsetLen);
				buffer[size] = charset[index < 0 ? -index : index];
				num /= (long)charsetLen;
				size++;
			}
		}
		
		for(int len = 0; len < mod ; len++) {
			long num = seed.nextLong();
			int index = (int)(num % (long)charsetLen);
			buffer[size] = charset[index < 0 ? -index : index];
			num /= (long)charsetLen;
			size++;
		}
		
		retVal = new String(buffer, 0, size);
		
		return retVal;
	}

	private static Random randomize() {
		long currentMillis = System.currentTimeMillis();
		Random rand = new Random();
		rand.setSeed(currentMillis);
		return rand;
	}
}
