package com.oreilly.nattubaba.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.ParamPart;

// A utility class to handle <code>multipart/form-data</code> requests.
public class MultipartRequest {
	private static final int DEFAULT_MAX_POST_SIZE = 1024 * 1024; // 1 Meg
	private Hashtable<String,Vector<String>> parameters = new Hashtable<String,Vector<String>>(); // name - Vector of values
	private Hashtable<String,UploadedFile> files = new Hashtable<String,UploadedFile>(); // name - UploadedFile

	public MultipartRequest(HttpServletRequest request, String saveDirectory)
			throws IOException {
		this(request, saveDirectory, DEFAULT_MAX_POST_SIZE);
	}

	public MultipartRequest(HttpServletRequest request, String saveDirectory,
			int maxPostSize) throws IOException {
		// Sanity check values
		if (request == null)
			throw new IllegalArgumentException("request cannot be null");
		if (saveDirectory == null)
			throw new IllegalArgumentException("saveDirectory cannot be null");
		if (maxPostSize <= 0) {
			throw new IllegalArgumentException("maxPostSize must be positive");
		}
		// Save the dir
		File dir = new File(saveDirectory);
		// Check saveDirectory is truly a directory
		if (!dir.isDirectory())
			throw new IllegalArgumentException("Not a directory: "
					+ saveDirectory);
		// Check saveDirectory is writable
		if (!dir.canWrite())
			throw new IllegalArgumentException("Not writable: " + saveDirectory);
		// Parse the incoming multipart, storing files in the dir provided,
		// and populate the meta objects which describe what we found
		MultipartParser parser = new MultipartParser(request, maxPostSize);
		Part part;
		while ((part = parser.readNextPart()) != null) {
			String name = part.getName();
			if (part.isParam()) {
				// It's a parameter part, add it to the vector of values
				ParamPart paramPart = (ParamPart) part;
				String value = paramPart.getStringValue();
				Vector<String> existingValues = (Vector<String>) parameters.get(name);
				if (existingValues == null) {
					existingValues = new Vector<String>();
					parameters.put(name, existingValues);
				}

				existingValues.addElement(value);
			} else if (part.isFile()) {
				// It's a file part
				FilePart filePart = (FilePart) part;
				String fileName = filePart.getFileName();
				if (fileName != null) {
					// The part actually contained a file
					filePart.writeTo(dir);
					files.put(name, new UploadedFile(dir.toString(), fileName,
							filePart.getContentType()));
				} else {
					// The field did not contain a file
					files.put(name, new UploadedFile(null, null, null));
				}
			}
		}
	}

	// Constructor with an old signature, kept for backward compatibility.
	public MultipartRequest(ServletRequest request, String saveDirectory)
			throws IOException {
		this((HttpServletRequest) request, saveDirectory);
	}

	// Constructor with an old signature, kept for backward compatibility.
	public MultipartRequest(ServletRequest request, String saveDirectory,
			int maxPostSize) throws IOException {
		this((HttpServletRequest) request, saveDirectory, maxPostSize);
	}

	public Enumeration<String> getParameterNames() {
		return parameters.keys();
	}

	public Enumeration<String> getFileNames() {
		return files.keys();
	}

	public String getParameter(String name) {
		try {
			Vector<String> values = (Vector<String>) parameters.get(name);
			if (values == null || values.size() == 0) {
				return null;
			}
			String value = (String) values.elementAt(values.size() - 1);
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	public String[] getParameterValues(String name) {
		try {
			Vector<String> values = (Vector<String>) parameters.get(name);
			if (values == null || values.size() == 0) {
				return null;
			}
			String[] valuesArray = new String[values.size()];
			values.copyInto(valuesArray);
			return valuesArray;
		} catch (Exception e) {
			return null;
		}
	}

	public String getFilesystemName(String name) {
		try {
			UploadedFile file = (UploadedFile) files.get(name);
			return file.getFilesystemName(); // may be null
		} catch (Exception e) {
			return null;
		}
	}

	public String getContentType(String name) {
		try {
			UploadedFile file = (UploadedFile) files.get(name);
			return file.getContentType(); // may be null
		} catch (Exception e) {
			return null;
		}
	}

	public File getFile(String name) {
		try {
			UploadedFile file = (UploadedFile) files.get(name);
			return file.getFile(); // may be null
		} catch (Exception e) {
			return null;
		}
	}

	// A class to hold information about an uploaded file.
	class UploadedFile {
		private String dir;
		private String filename;
		private String type;

		UploadedFile(String dir, String filename, String type) {
			this.dir = dir;
			this.filename = filename;
			this.type = type;
		}

		public String getContentType() {
			return type;
		}

		public String getFilesystemName() {
			return filename;
		}

		public File getFile() {
			if (dir == null || filename == null) {
				return null;
			} else {
				return new File(dir + File.separator + filename);
			}
		}
	}
}