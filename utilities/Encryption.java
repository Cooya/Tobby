package utilities;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class Encryption {
	private static final int AES_KEY_LENGTH = 32;
	private static final String publicKey = 
		"MIIBUzANBgkqhkiG9w0BAQEFAAOCAUAAMIIBOwKCATIAgucoka9J2PXcNdjcu6CuDmgteIMB+rih" +
		"2UZJIuSoNT/0J/lEKL/W4UYbDA4U/6TDS0dkMhOpDsSCIDpO1gPG6+6JfhADRfIJItyHZflyXNUj" +
		"WOBG4zuxc/L6wldgX24jKo+iCvlDTNUedE553lrfSU23Hwwzt3+doEfgkgAf0l4ZBez5Z/ldp9it" +
		"2NH6/2/7spHm0Hsvt/YPrJ+EK8ly5fdLk9cvB4QIQel9SQ3JE8UQrxOAx2wrivc6P0gXp5Q6bHQo" +
		"ad1aUp81Ox77l5e8KBJXHzYhdeXaM91wnHTZNhuWmFS3snUHRCBpjDBCkZZ+CxPnKMtm2qJIi57R" +
		"slALQVTykEZoAETKWpLBlSm92X/eXY2DdGf+a7vju9EigYbX0aXxQy2Ln2ZBWmUJyZE8B58CAwEA" +
		"AQ==";
	
	public static byte[] encrypt(byte[] encryptedKey, char[] login, char[] password, char[] salt) {
		byte[] decryptedKey = decryptReceivedKey(encryptedKey);
		return encryptCredentials(decryptedKey, login, password, salt);
	}
	
	private static byte[] decryptReceivedKey(byte[] encryptedKey) {
		byte[] resultKey = null;
		try {
		    byte[] decodedKey = Base64.getDecoder().decode(publicKey);
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
	
	private static byte[] encryptCredentials(byte[] key, char[] login, char[] password, char[] salt) {
		byte[] encryptedCredentials = null;
		ByteArray buffer = new ByteArray();
		buffer.writeUTFBytes(salt);
		buffer.writeBytes(generateRandomAESKey());
		buffer.writeByte((byte) login.length);
		buffer.writeUTFBytes(login);
		buffer.writeUTFBytes(password);
		
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
			PublicKey publicKey = kf.generatePublic(spec);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			encryptedCredentials = cipher.doFinal(buffer.bytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptedCredentials;
	}
	
	private static ByteArray generateRandomAESKey() {
		ByteArray array = new ByteArray();
		for(int i = 0; i < AES_KEY_LENGTH; ++i)
			array.writeByte((byte) Math.floor(Math.random() * 256));
		return array;
	}
}