package com.tbitsglobal.ddc;
/*
 * Copyright (c) 2011-2012 Jarek Sacha. All Rights Reserved.
 *
 * Author's e-mail: jarek.listy at gmail.com
 */

import java.io.File;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_COLOR;

/**
 * Use kernel convolution to sharpen an image.
 */
public class Ex3Sharpen {

	// Read input image

	public static final String STAMP = "/tmp/files/newStamp.png";

	public static IplImage sharp(IplImage image) {
//		IplImage image = OpenCVUtils.loadAndShowOrExit(new File(STAMP),
//				CV_LOAD_IMAGE_COLOR);

		// Define output image
		IplImage dest = cvCreateImage(cvGetSize(image), image.depth(), 3);

		// Construct sharpening kernel, oll unassigned values are 0
		CvMat kernel = CvMat.create(3, 3, CV_32F);

		kernel.put(1, 1, 5);
		kernel.put(0, 1, -1);
		kernel.put(2, 1, -1);
		kernel.put(1, 0, -1);
		kernel.put(1, 2, -1);

		// Filter the image
		filter2D(image, dest, -1, kernel, new CvPoint(-1, -1), 0,
				BORDER_DEFAULT);

		// Display
//		OpenCVUtils.show(dest, "Sharpened");
		return dest;
	}
	public static void main(String[] args) {
//		sharp();
	}
}