package transbit.tbits.common;

import java.io.Serializable;

public class TBitsFileInfo implements Serializable
{
	public TBitsFileInfo(String fileName, String fileLocation)
	{
		this.fileName = fileName;
		this.fileLocation = fileLocation;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	private String fileName;
	private String fileLocation;
	
}