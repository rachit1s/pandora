/**
 * 
 */
package transbit.tbits.addons;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class AddonInfoWithBytes extends AddonInfo
{
	private byte[] jarBytes;

	/**
	 * @return the jarBytes
	 */
	public byte[] getJarBytes() {
		return jarBytes;
	}

	/**
	 * @param jarBytes the jarBytes to set
	 */
	public void setJarBytes(byte[] jarBytes) {
		this.jarBytes = jarBytes;
	}

	/**
	 * @param id
	 * @param jarName
	 * @param status
	 * @param jarBytes
	 */
	public AddonInfoWithBytes(long jarId, String jarName, int status, byte[] jarBytes) 
	{
		super(jarId, jarName, status);
		this.jarBytes = jarBytes;
	}
}
