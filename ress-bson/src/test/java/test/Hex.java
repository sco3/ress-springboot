package test;
import com.google.common.io.BaseEncoding;

public class Hex {
	public static String encode(byte[] bytes) {
		return BaseEncoding.base16().lowerCase().encode(bytes);
	}

	public static String encodeUpper(byte[] bytes) {
		return BaseEncoding.base16().encode(bytes);
	}

	public static byte[] decode(String hex) {
		try {
			return BaseEncoding.base16().decode(hex.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid hex string: " + hex, e);
		}
	}
}