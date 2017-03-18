package core.util;

public class EncodingDetector {

	private static EncodingDetector _sharedInstance = null;
	private String _fallbackEncoding;
	private Delegate _delegate;
	
	public static interface EncodingDetectorDelegate {
		public abstract String encodingDetectorWillFallback(EncodingDetector detector);
	}
	
    private static boolean couldBeUTF16BigEndian(byte bytes[], int offset)
    {
        return bytes[offset] == -2 && bytes[offset + 1] == -1;
    }

    private static boolean couldBeUTF16SmallEndian(byte bytes[], int offset)
    {
        return bytes[offset] == -1 && bytes[offset + 1] == -2;
    }

    protected static boolean is7bitString(byte bytes[], int offset, int length)
    {
        for(int anOffset = offset; anOffset < length; anOffset++)
            if((bytes[anOffset] & 0x80) != 0)
                return false;

        return true;
    }

    protected static boolean isISOSequence(byte bytes[], int offset, boolean isThreeBytes)
    {
        switch((char)bytes[1 + offset])
        {
        case 96: // '`'
        case 97: // 'a'
        case 98: // 'b'
        case 99: // 'c'
        case 110: // 'n'
        case 111: // 'o'
        case 124: // '|'
        case 125: // '}'
        case 126: // '~'
            return true;

        case 33: // '!'
        case 34: // '"'
        case 36: // '$'
        case 40: // '('
            if(isThreeBytes && (bytes[2 + offset] >= 32 || bytes[2 + offset] != 128))
                return true;
            break;
        }
        return false;
    }

    protected static boolean isISO2022JPString(byte bytes[], int offset, int length)
    {
        for(int anOffset = offset; anOffset < length; anOffset++)
        {
            if(bytes[anOffset] != 27)
                continue;
            if(length > 3)
            {
                if(isISOSequence(bytes, anOffset, true))
                    return true;
                continue;
            }
            if(length > 2 && isISOSequence(bytes, anOffset, false))
                return true;
        }

        return false;
    }

    protected static boolean isUnicodeString(byte bytes[], int offset, int length)
    {
        for(int anOffset = offset; anOffset < length; anOffset++)
            if(bytes[anOffset] == 0 && length != 0)
                return true;

        return false;
    }

    protected static boolean isShiftJISString(byte bytes[], int offset, int len)
    {
        int anOffset = offset;
        do
        {
            if(anOffset >= len)
                break;
            int first = 0xff & bytes[anOffset];
            anOffset++;
            if((first & 0x80) != 0 && anOffset < len && (first >= 129 && first <= 159 || first >= 224 && first <= 239))
            {
                int second = 0xff & bytes[anOffset];
                if(second <= 252 && second >= 64 && second != 127 && (first != 239 || second < 159))
                    anOffset++;
                else
                    return false;
            }
        } while(true);
        return true;
    }
    
    protected static boolean isJapaneseEUCString(byte bytes[], int offset, int len) {
    	label0:
        {
            int anOffset = offset;
            do
            {
                int first;
                do
                {
                    if(anOffset >= len)
                        break label0;
                    first = 0xff & bytes[anOffset];
                    anOffset++;
                } while((first & 0x80) == 0);
                int second;
                if(first == 142 && anOffset < len)
                {
                    second = 0xff & bytes[anOffset];
                    if(second < 161 || second > 223)
                        break;
                    anOffset++;
                    continue;
                }
                if(first == 143 && anOffset + 1 < len)
                {
                    second = 0xff & bytes[anOffset];
                    int third = 0xff & bytes[anOffset + 1];
                    if(second < 161 || second >= 255 || third < 161 || third >= 255)
                        break;
                    anOffset += 2;
                    continue;
                }
                if(first < 161 || first >= 255 || anOffset >= len)
                    break;
                second = 0xff & bytes[anOffset];
                if(second < 161 || second >= 255)
                    break;
                anOffset++;
            } while(true);
            return false;
        }
        return true;
    }
    
    private static boolean _properFollowingUTF8Bytes(byte bytes[], int offset, int length, int count)
    {
        if((offset + count) - 1 >= length)
            return false;
        for(int i = 0; i < count; i++)
        {
            int testByte = 0xff & bytes[offset + i];
            if((testByte & 0xc0) != 128)
                return false;
        }

        return true;
    }

    protected static boolean isUTF8String(byte bytes[], int offset, int len)
    {
        int anOffset = offset;
        int matchedCount = 0;
        do
        {
            if(anOffset >= len)
                break;
            int first = 0xff & bytes[anOffset];
            anOffset++;
            if((first & 0x80) != 0)
                if((first & 0xe0) == 192)
                {
                    if(_properFollowingUTF8Bytes(bytes, anOffset, len, 1))
                    {
                        anOffset++;
                        matchedCount++;
                    } else
                    {
                        return false;
                    }
                } else
                if((first & 0xf0) == 224)
                {
                    if(_properFollowingUTF8Bytes(bytes, anOffset, len, 2))
                    {
                        anOffset += 2;
                        matchedCount++;
                    } else
                    {
                        return false;
                    }
                } else
                if((first & 0xf8) == 240)
                {
                    if(_properFollowingUTF8Bytes(bytes, anOffset, len, 3))
                    {
                        anOffset += 3;
                        matchedCount++;
                    } else
                    {
                        return false;
                    }
                } else
                {
                    return false;
                }
        } while(true);
        return matchedCount > 0;
    }

    private String _realGuessEncoding(byte bytes[], int offset, int length, String otherFallbackEncoding)
    {
        if(is7bitString(bytes, offset, length))
            if(isISO2022JPString(bytes, offset, length))
                return "ISO2022JP";
            else
                return "ASCII";
        if(isUnicodeString(bytes, offset, length))
            return "Unicode";
        if(isUTF8String(bytes, offset, length))
            return "UTF8";
        boolean isEUC = isJapaneseEUCString(bytes, offset, length);
        boolean isShiftJIS = isShiftJISString(bytes, offset, length);
        if(isEUC && isShiftJIS)
            if(_delegate.respondsTo("encodingDetectorWillFallback"))
				try {
					return (String)_delegate.perform("encodingDetectorWillFallback", this);
				} catch (Exception e) {
					return "SJIS";
				}
			else
                return otherFallbackEncoding;
        if(!isEUC && !isShiftJIS)
            return otherFallbackEncoding;
        else
            return isEUC ? "EUC_JP" : "SJIS";
    }

    public static synchronized EncodingDetector sharedInstance() {
    	if(_sharedInstance == null)
    		_sharedInstance = new EncodingDetector();
    	return _sharedInstance;
    }
    
    public EncodingDetector() {
    	_fallbackEncoding = "UTF8";
    	_delegate = new Delegate(core.util.EncodingDetector.EncodingDetectorDelegate.class);
    }
    
    public String fallbackEncoding() {
    	return _fallbackEncoding;
    }
    public void setFallbackEncodiung(String encoding) {
    	_fallbackEncoding = encoding;
    }
    public void setDelegate(Object delegate) {
    	_delegate.setDelegate(delegate);
    }
    
    public String guessEncodingForBytes(byte[] bytes) {
    	return guessEncodingForBytes(bytes, _fallbackEncoding);
    }

	public String guessEncodingForBytes(byte[] bytes, String otherFallbackEncoding) {
		int offset = 0;
		int length = bytes.length;
		if(bytes == null || length == 0)
			return "UTF8";
		if(length == 1)			// IsoLatain1
			return "ISO8859_1";
		if(length == 2 && couldBeUTF16(bytes, offset))
			return "Unicode";
		else
			return _realGuessEncoding(bytes, offset, length, otherFallbackEncoding);
	}
	
	public boolean couldBeUTF16(byte bytes[], int offset) {
		return couldBeUTF16BigEndian(bytes, offset) || couldBeUTF16SmallEndian(bytes, offset);
	}
	
	public static String detectEncodingWithDefaultEncoding(byte bytes[], String enc) {
		return sharedInstance().guessEncodingForBytes(bytes, enc);
	}
}
