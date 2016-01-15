package utilities;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
	private static byte[] randomAESKey = null;
	private static final int AES_KEY_LENGTH = 32;
	private static final String publicKeyBase64 = 
		"MIIBUzANBgkqhkiG9w0BAQEFAAOCAUAAMIIBOwKCATIAgucoka9J2PXcNdjcu6CuDmgteIMB+rih" +
		"2UZJIuSoNT/0J/lEKL/W4UYbDA4U/6TDS0dkMhOpDsSCIDpO1gPG6+6JfhADRfIJItyHZflyXNUj" +
		"WOBG4zuxc/L6wldgX24jKo+iCvlDTNUedE553lrfSU23Hwwzt3+doEfgkgAf0l4ZBez5Z/ldp9it" +
		"2NH6/2/7spHm0Hsvt/YPrJ+EK8ly5fdLk9cvB4QIQel9SQ3JE8UQrxOAx2wrivc6P0gXp5Q6bHQo" +
		"ad1aUp81Ox77l5e8KBJXHzYhdeXaM91wnHTZNhuWmFS3snUHRCBpjDBCkZZ+CxPnKMtm2qJIi57R" +
		"slALQVTykEZoAETKWpLBlSm92X/eXY2DdGf+a7vju9EigYbX0aXxQy2Ln2ZBWmUJyZE8B58CAwEA" +
		"AQ==";
	
	public static byte[] decryptReceivedKey(byte[] encryptedKey) {
		byte[] resultKey = null;
		try {
		    byte[] decodedKey = Base64.getDecoder().decode(publicKeyBase64);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(spec);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, pk);	
			resultKey = cipher.doFinal(encryptedKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultKey;
	}
	
	public static String makePEM(byte[] decryptedKey) {
		String encodedKey = Base64.getEncoder().encodeToString(decryptedKey);
		int len = encodedKey.length();
		String resultKey = "";
		for(int i = 0; i < len; i += 76)
			if(i + 76 < len)	
				resultKey += encodedKey.substring(i, i + 76) + '\n';
			else
				resultKey += encodedKey.substring(i, len - 1) + '\n';
		return "-----BEGIN PUBLIC KEY-----\n" + resultKey + "-----END PUBLIC KEY-----";
	}
	
	public static byte[] encryptCredentials(byte[] decryptedKey, String login, String password, String salt) {
		byte[] encryptedCredentials = null;
		ByteArray buffer = new ByteArray();
		buffer.writeUTFBytes(salt);
		randomAESKey = generateRandomAESKey();
		buffer.writeBytes(randomAESKey);
		buffer.writeByte((byte) login.length());
		buffer.writeUTFBytes(login);
		buffer.writeUTFBytes(password);
		
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec spec = new X509EncodedKeySpec(decryptedKey);
			PublicKey publicKey = kf.generatePublic(spec);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			encryptedCredentials = cipher.doFinal(buffer.bytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptedCredentials;
	}
	
	private static byte[] generateRandomAESKey() {
		byte[] key = new byte[AES_KEY_LENGTH];
		for(int i = 0; i < AES_KEY_LENGTH; ++i)
			key[i] = (byte) Math.floor(Math.random() * 256);
		return key;
	}
	
	public static byte[] decodeWithAES(byte[] ticket) {
		byte[] decryptedTicket = null;
		try {
			SecretKeySpec AESKeySpec = new SecretKeySpec(randomAESKey, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		    IvParameterSpec ivspec = new IvParameterSpec(Arrays.copyOf(randomAESKey, 16));
			cipher.init(Cipher.DECRYPT_MODE, AESKeySpec, ivspec);
			decryptedTicket = cipher.doFinal(ticket);
			randomAESKey = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedTicket;
	}
}