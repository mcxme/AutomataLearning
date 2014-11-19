package model;

import java.util.LinkedList;
import java.util.List;


public class StringUtil {

	public static List<String> getPrefixes(String string, String[] alphabet)
	{
		LinkedList<String> prefixes = new LinkedList<String>();
		prefixes.add(string);
		
		String copy = new String(string);
		
		while (copy.length() > 0)
		{
			for (int i = 0; i < alphabet.length; i++) {
				String symbol = alphabet[i];
				if (string.endsWith(symbol))
				{
					String prefix = copy.substring(0, copy.length() - symbol.length());
					prefixes.addFirst(prefix);
					copy = prefix;
					break;
				}
			}
		}
		
		return prefixes;
	}
	
}
