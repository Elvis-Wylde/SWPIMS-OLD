package com.cjwsjy.rmip.util;

public class StringUtils {
	public static boolean isEmpty(String str) {
        return str == null || str.trim().equals("") || str.trim().length() <= 0 || str.equals("null")
                || str.equals("undefined");
    }

	public static boolean isPositiveInt(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			int i = Integer.valueOf(str);
			if (i <= 0) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isInt(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			Integer.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isPositiveLong(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			long l = Long.valueOf(str);
			if (l <= 0) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isPositiveFloat(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			float i = Float.valueOf(str);
			if ((i - 0.000000001) <= 0) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isFloat(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			Float.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static String trim(String str) {
		if (isEmpty(str)) return "";
		if (str.charAt(0) == '\'' || str.charAt(0) == '\"') {
			str = str.substring(1);
		}
		if (isEmpty(str)) return "";
		if (str.charAt(str.length() - 1) == '\'' || str.charAt(str.length() - 1) == '\"') {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	/**
	 * prefix of ASCII string of native character
	 */
	private static String PREFIX = "\\u";

	/**
	 * ASCII to native string. It's as same as execute native2ascii.exe -reverse.
	 * 
	 * @param str ASCII string
	 * @return native string
	 */
	public static String ascii2Native(String str) {
		str = trim(str);
		if (isEmpty(str)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int begin = 0;
		int index = str.indexOf(PREFIX);
		while (index != -1) {
			sb.append(str.substring(begin, index));
			sb.append(ascii2Char(str.substring(index, index + 6)));
			begin = index + 6;
			index = str.indexOf(PREFIX, begin);
		}
		sb.append(str.substring(begin));
		return sb.toString();
	}

	/**
	 * Ascii to native character.
	 * 
	 * @param str ascii string
	 * @return native character
	 */
	private static char ascii2Char(String str) {
		if (str.length() != 6) {
			throw new IllegalArgumentException("Ascii string of a native character must be 6 character.");
		}
		if (!PREFIX.equals(str.substring(0, 2))) {
			throw new IllegalArgumentException("Ascii string of a native character must start with \"\\u\".");
		}
		String tmp = str.substring(2, 4);
		int code = Integer.parseInt(tmp, 16) << 8;
		tmp = str.substring(4, 6);
		code += Integer.parseInt(tmp, 16);
		return (char) code;
	}

}
