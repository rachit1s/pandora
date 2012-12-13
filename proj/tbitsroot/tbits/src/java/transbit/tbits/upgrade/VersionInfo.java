package transbit.tbits.upgrade;

public class VersionInfo implements Comparable<VersionInfo> {
	
	private String sysType;
	private String major;
	private String minor;

	public VersionInfo(String sysType, String major, String minor) {
		this.sysType = sysType;
		this.major = major;
		this.minor = minor;
	}
	
	public String getSysType() {
		return sysType;
	}

	public String getMajor() {
		return major;
	}

	public String getMinor() {
		return minor;
	}

	public int compareTo(VersionInfo o) {
		return compareMajors(major, o.getMajor());
	}

	// return m1 - m2
	// 6.1.0 < 6.2
	public static int compareMajors(String m1, String m2) {
		if ((m1 == null) || (m2 == null))
			throw new NullPointerException(
					"The versions to be compared can not be null.");

		String[] versionParts1 = m1.split("\\.");
		String[] versionParts2 = m2.split("\\.");
		int i = 0;
		for (; i < versionParts1.length; i++) {
			if (i >= versionParts2.length)
				return 1;
			// TODO: handle exceptions
			int v1 = 0;
			try {
				v1 = Integer.parseInt(versionParts1[i]);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("The version '" + m1
						+ "' is invalid. It should be . separted numbers.");
			}
			int v2 = 0;
			try {
				v2 = Integer.parseInt(versionParts2[i]);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("The version '" + m2
						+ "' is invalid. It should be . separted numbers.");
			}
			if (v1 > v2)
				return 1;
			if (v1 < v2)
				return -1;
		}
		if (i < versionParts2.length) {
			return -1;
		}
		return 0;
	}
	
	public String toString()
	{
		return major+"_"+sysType;
	}
}
