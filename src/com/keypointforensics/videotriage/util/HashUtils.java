package com.keypointforensics.videotriage.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	public static final String DEFAULT_MESSAGE_DIGEST_METHOD = "SHA-1";
	
	public static String getBlobImageSha1Hash(final BufferedImage blobImage) throws NoSuchAlgorithmException {
		if(blobImage == null) {			
			return null;
		}
		
		byte[] data = ((DataBufferByte) blobImage.getData().getDataBuffer()).getData();
		
		MessageDigest messageDigest = null;
		messageDigest = MessageDigest.getInstance(DEFAULT_MESSAGE_DIGEST_METHOD);
		messageDigest.update(data);
		
        byte[] hash = messageDigest.digest();
        data = null;
        
        return returnHex(hash);
	}

	private static String returnHex(final byte[] inBytes) {
		String hexString = "";
		
		for (int i = 0; i < inBytes.length; ++i) { 
			hexString += Integer.toString((inBytes[i] & 0xff) + 0x100, 16).substring(1);
		} 
		
		return hexString;
	} 
	
}
