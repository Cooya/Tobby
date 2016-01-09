package main;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class Encryption {
	private static final int AES_KEY_LENGTH = 32;
	private static final String publicKey = 
		"MIIBUzANBgkqhkiG9w0BAQEFAAOCAUAAMIIBOwKCATIAq8EYkkGCUg86Bf2CHaM1z1Q2ahQgVXkx" +
		"49I0igwTVCIqG86jsgNb22na1DThZ+IP7DfyBszIecVSP8nwbYPbx6Z7dwq4pnMVx/lx5lyMZUO1" +
		"n/HGEkw1S06AlfXzSg58ci5DL9RJ9ZIa1oMDKtrZiNYA5C3L+7NSCVp/2H/yypWkDjzkFan65+TN" +
		"RExo/2O3+MytJtQ/BXVkbYD58+iiZegddNTNGvz8WlPz2cZvPQt4x1TN+KOgJRKZH5imNAxCtRg6" +
		"l1OLVxfwwUjKFgM4uAsto8vJv5DUFZQMO1Sh9gMpmzeMwXIF4fDD4O1TNiVmu3ABybt2Y4EdaQhs" +
		"/ponC0SNcWbrY0stYbX+Wpk9/Hcxmo3zoduf1ZAdGM01E1g3IjQMd0gOP4v1KQtBjoHim2MCAwEA" +
		"AQ==";
	
	public static byte[] encrypt(byte[] encryptedKey, char[] login, char[] password, char[] salt) {
		byte[] decryptedKey = decryptReceivedKey(encryptedKey);
		return encryptCredentials(decryptedKey, login, password, salt);
	}
	
	private static byte[] decryptReceivedKey(byte[] receivedKey) {
		byte[] resultKey = null;
		try {
		    byte[] decodedKey = Base64.getDecoder().decode(publicKey);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(spec);
			
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, pk);	
			resultKey = cipher.doFinal(receivedKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultKey;
	}
	
	private static byte[] encryptCredentials(byte[] key, char[] login, char[] password, char[] salt) {
		byte[] encryptedCredentials = null;
		ByteArray buffer = new ByteArray();
		buffer.writeBytes(new String(salt).getBytes());
		buffer.writeBytes(generateRandomAESKey());
		buffer.writeByte((byte) login.length);
		buffer.writeUTFBytes(new String(login).toCharArray());
		buffer.writeUTFBytes(new String(password).toCharArray());
		
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec x509 = new X509EncodedKeySpec(key);
			PublicKey publicKey = kf.generatePublic(x509);
			
			Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
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