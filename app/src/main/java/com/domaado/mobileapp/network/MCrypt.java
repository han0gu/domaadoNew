package com.domaado.mobileapp.network;

import android.text.TextUtils;

import com.domaado.mobileapp.Constant;
import com.domaado.mobileapp.widget.myLog;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 암호화!
 */
public class MCrypt {
	
	final static String TAG = "MCrypt";

	private IvParameterSpec ivspec;
	private SecretKeySpec keyspec;
	private Cipher cipher;

	public final static int enc_no = 9;
	private String SecretKey = "1234567890qwerty";	// 서버측 보안키와 연동됨.

	public static int maxbyte = 16;

	public MCrypt(byte[] iv, byte[] key) {

		try {
			ivspec = new IvParameterSpec(iv);
			keyspec = new SecretKeySpec(key, "AES");

			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MCrypt(String iv) {

		try {
			ivspec = new IvParameterSpec(iv.getBytes(Constant.ENCODING));
			keyspec = new SecretKeySpec(SecretKey.getBytes(Constant.ENCODING), "AES");
			
//			myLog.d(TAG, "-- onTextReplaceEncrypt iv("+iv+")");
//			myLog.d(TAG, "-- onTextReplaceEncrypt key("+SecretKey+")");
			
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			//cipher = Cipher.getInstance("AES/CBC/NoPadding");
			//cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
//		} catch (NoSuchProviderException e) {
//			e.printStackTrace();
		}
	}

	public byte[] encrypt(String text) throws Exception {
		if (text == null || text.length() == 0)
			throw new Exception("Empty string");

		byte[] encrypted = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			
//			byte[] cipherText = new byte[cipher.getOutputSize(text.getBytes().length)];
//			int ctLength = cipher.update(text.getBytes(), 0, text.getBytes().length, cipherText, 0);
//			ctLength += cipher.doFinal(cipherText, ctLength);
//			encrypted = new String(cipherText).getBytes();
			
			encrypted = cipher.doFinal(padString(text).getBytes(Constant.ENCODING));
		} catch (Exception e) {
			throw new Exception("[encrypt] " + e.getMessage());
		}

		return encrypted;
	}

	public byte[] decrypt(String code) throws Exception {
		if (code == null || code.length() == 0)
			throw new Exception("Empty string");

		byte[] decrypted = null;

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			
//			byte[] plainText = new byte[cipher.getOutputSize(code.getBytes().length)];
//		    int ptLength = cipher.update(code.getBytes(), 0, code.getBytes().length, plainText, 0);
//		    ptLength += cipher.doFinal(plainText, ptLength);
//		    decrypted = new String(plainText).getBytes();
		    
			decrypted = cipher.doFinal(hexToBytes(code));
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return decrypted;
	}
	
	public byte[] encrypt(byte[] text) throws Exception {
		if (text == null || text.length == 0)
			throw new Exception("Empty string");

		byte[] encrypted = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

			encrypted = cipher.doFinal(text);
		} catch (Exception e) {
			throw new Exception("[encrypt] " + e.getMessage());
		}

		return encrypted;
	}

	public byte[] decrypt(byte[] code) throws Exception {
		if (code == null || code.length == 0)
			throw new Exception("Empty string");

		byte[] decrypted = null;

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			decrypted = cipher.doFinal(code);
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return decrypted;
	}

	public static String bytesToHex(byte[] data) {
		if (data == null) {
			return null;
		}

		int len = data.length;
		String str = "";
		for (int i = 0; i < len; i++) {
			if ((data[i] & 0xFF) < 16)
				str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
			else
				str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
		}
		return str;
	}

	public static byte[] hexToBytes(String str) {
		if (str == null) {
			return null;
		} else if (str.length() < 2) {
			return null;
		} else {
			int len = str.length() / 2;
			byte[] buffer = new byte[len];
			for (int i = 0; i < len; i++) {
				buffer[i] = (byte) Integer.parseInt(
						str.substring(i * 2, i * 2 + 2), 16);
			}
			return buffer;
		}
	}

	private static String padString(String source) {
		char paddingChar = ' ';
		int size = 16;
		int x = source.length() % size;
		int padLength = size - x;

		for (int i = 0; i < padLength; i++) {
			source += paddingChar;
		}

		return source;
	}
	
	public static String binaryToHex(String bin) {
		StringBuffer hex = new StringBuffer("");
		
		if(TextUtils.isEmpty(bin)) return "";
		else if(bin.length()<8) return "";
		
		for(int i=0; i<(maxbyte*8); i=i+8) {
			String x = bin.substring(i, i+8);
			//hex.append(Integer.toHexString(Integer.parseInt(x, 2)).charAt(1));
			hex.append(String.format("%02X", Integer.parseInt(x, 2)).charAt(1));
//			myLog.d(TAG, "-- hex: "+ String.format("%02X", Integer.parseInt(x, 2)));
//			myLog.d(TAG, "-- binaryToHex: "+ hex);
		}
		
		myLog.d(TAG, "++ binaryToHex: "+ hex);
		
		return hex.toString();
	}
	
	public static String convertHexToString(String hex){

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		  
		//49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for(int i=0; i<hex.length()-1; i+=2) {
			  
			//grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			//convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			//convert the decimal to character
			sb.append((char)decimal);
			  
			temp.append(decimal);
		}
		myLog.d(TAG, "Decimal : " + temp.toString());
		  
		return sb.toString();
	}

}