package main;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Encryption {
	private static final String publicKey = "-----BEGIN PUBLIC KEY-----\n" +
		"MIIBUzANBgkqhkiG9w0BAQEFAAOCAUAAMIIBOwKCATIAq8EYkkGCUg86Bf2CHaM1z1Q2ahQgVXkx\n" +
		"49I0igwTVCIqG86jsgNb22na1DThZ+IP7DfyBszIecVSP8nwbYPbx6Z7dwq4pnMVx/lx5lyMZUO1\n" +
		"n/HGEkw1S06AlfXzSg58ci5DL9RJ9ZIa1oMDKtrZiNYA5C3L+7NSCVp/2H/yypWkDjzkFan65+TN\n" +
		"RExo/2O3+MytJtQ/BXVkbYD58+iiZegddNTNGvz8WlPz2cZvPQt4x1TN+KOgJRKZH5imNAxCtRg6\n" +
		"l1OLVxfwwUjKFgM4uAsto8vJv5DUFZQMO1Sh9gMpmzeMwXIF4fDD4O1TNiVmu3ABybt2Y4EdaQhs\n" +
		"/ponC0SNcWbrY0stYbX+Wpk9/Hcxmo3zoduf1ZAdGM01E1g3IjQMd0gOP4v1KQtBjoHim2MCAwEA\n" +
		"AQ==\n" +
		"-----END PUBLIC KEY-----";
	
	public int[] encrypt(byte[] encryptedKey, char[] login, char[] password, char[] salt) {
		byte[] decryptedKey = decryptReceivedKey(encryptedKey);
		//char[] PEM = makePEM(decryptedKey);
		return encryptCredentials(decryptedKey, login, password, salt);
	}
	
	public static byte[] decryptReceivedKey(byte[] receivedKey) {
		byte[] resultKey = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			InputStream publicKeyBytes = new ByteArrayInputStream(publicKey.getBytes());
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(publicKeyBytes);
			cipher.init(Cipher.DECRYPT_MODE, cert.getPublicKey());
			resultKey = cipher.doFinal(receivedKey);
		} catch (CertificateException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return resultKey;
	}
	
	public static char[] makePEM(byte[] key) {
		//byte[] encodedBytes = Base64.getEncoder().encode(key);
		return null;
	}
	
	public int[] encryptCredentials(byte[] key, char[] login, char[] password, char[] salt) {
		int[] encryptedCredentials = null;
		ByteBuffer buffer = ByteBuffer.wrap(new String(salt).getBytes());
		buffer.put((byte) login.length);
		buffer.put(new String(login).getBytes());
		buffer.put(new String(password).getBytes());
		buffer.position(0);
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(key));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] result = cipher.doFinal(buffer.array());
			encryptedCredentials = new int[result.length];
			for(int i = 0; i < result.length; ++i)
				encryptedCredentials[i] = (int) result[i];
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return encryptedCredentials;
	}
}


