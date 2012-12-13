package transbit.tbits.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides utility functions to calculate the hash of any file.
 * 
 * @author Karan Gupta
 *
 */

public class HashUtilities {

	public static final int FILE_SIZE_FOR_TRANSFER = 1024*1024;
	
	//====================================================================================

	/**
	 * Computes the SHA1 hash of the mentioned file.
	 * 
	 * @param file
	 * @return SHA1 hash
	 */
	public static String computeSHA1(File file){
		try {
			return computeHash(file, "sha1");
		} 
		catch (NoSuchAlgorithmException e) {
			// Ignore this exception
		}
		return null;
	}
	
	//====================================================================================

	/**
	 * Computes the MD5 hash of the mentioned file.
	 * 
	 * @param file
	 * @return MD5 hash
	 */
	public static String computeMD5(File file){
		try {
			return computeHash(file, "md5");
		} 
		catch (NoSuchAlgorithmException e) {
			// Ignore this exception
		}
		return null;
	}
	
	//====================================================================================

	/**
	 * Computes the hash of the mentioned file using the mentioned algorithm.
	 * Returns null if the file is null.
	 * 
	 * @param file
	 * @param algorithm
	 * @return hash
	 * @throws NoSuchAlgorithmException
	 */
	public static String computeHash(File file, String algorithm) throws NoSuchAlgorithmException {
		try {
			if(null == file)
				return null;

			MessageDigest digest = MessageDigest.getInstance(algorithm);

			FileInputStream fis = new FileInputStream(file);

			int size = FILE_SIZE_FOR_TRANSFER;
			int read = 0;
			byte[] tempBytes = new byte[size];
			while((read = fis.read(tempBytes, 0,size)) >= 0)
				digest.update(tempBytes, 0, read);
			fis.close();
			return toHexDigest(digest);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	//====================================================================================

	// Utility Functions
	
	private static String toHexDigest(MessageDigest digest) {
		if (digest == null) {
			return null;
		}
		byte[] result = digest.digest();
		String hexDigest = "";
		for (int i = 0; i < result.length; i++) {
			hexDigest += getHexNumberFromByte(result[i]);
		}
		return hexDigest;
	}
	
	private static String getHexNumberFromByte(byte b) {
		int lo = b & 0xf;
		int hi = (b >> 4) & 0xf;
		return Integer.toHexString(hi) + Integer.toHexString(lo);
	}
	
	//====================================================================================

}
