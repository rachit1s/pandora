package transbit.tbits.api;

import java.io.File;
import java.util.ArrayList;

import transbit.tbits.common.IResourceManager;

public class FileResourceManager implements IResourceManager{
	private ArrayList<FileTuple> fileTuples = new ArrayList<FileTuple>();

	public FileResourceManager() {

	}

	/*
	 * Keeps track of The file which was moved from source to destination.
	 */
	public void addTuple(File source, File destination) {
		try {
			fileTuples.add(new FileTuple(source, destination));
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/*
	 * Rolls back. Moves files from destination to source.
	 */
	public void rollback() {
		for (FileTuple fileTuple : fileTuples) {
			try {
				boolean isRenameSuccessful = fileTuple.getDest().renameTo(
						fileTuple.getSource());
				if (!isRenameSuccessful) {
					System.out.println("Rollback: Unable to rename the file: '"
							+ fileTuple.getDest().getAbsolutePath() + "' to '"
							+ fileTuple.getSource().getAbsolutePath());
				}
			} catch (Exception exp) {
				System.out
						.println("Error occurred while rolling backing the file copying.");
				exp.printStackTrace();
			}
		}
	}
	
	public void commit()
	{
		fileTuples.clear();
	}
}

class FileTuple {
	private File dest;
	private File source;

	public FileTuple(File source, File dest) {
		this.source = source;
		this.dest = dest;
	}

	public File getSource() {
		return source;
	}

	public File getDest() {
		return dest;
	}
}