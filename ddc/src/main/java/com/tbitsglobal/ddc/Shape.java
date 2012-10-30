/*
 * Copyright (c) 2011-2012 Jarek Sacha. All Rights Reserved.
 *
 * Author's e-mail: jarek.listy at gmail.com
 */

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangleR;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_COLOR;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_TREE;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import java.util.Comparator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * The example for section "Computing components' shape descriptors" in Chapter
 * 7, page 186.
 */
public class Shape{

	//
	// First part is the same as in example `Ex5ExtractContours`; extracts
	// contours.
	//

	// Read input image

	public static final String STAMP = "D:/files/new.png";
	
	public static int thresh = 50;

	public static List<CvRect> extractShapes() {
		Comparator<CvRect> comparator = new RectComparator();
		PriorityQueue<CvRect> rect = 
	            new PriorityQueue<CvRect>(10, comparator);
		//List<CvRect> rect = new ArrayList<CvRect>();
		try {
			// val src = loadAndShowOrExit(new File("data/binaryGroup.bmp"),
			// CV_LOAD_IMAGE_GRAYSCALE);
			sharp();

			IplImage src = OpenCVUtils.loadAndShowOrExit(new File(STAMP),
					CV_8U);

			// IplImage src = Ex3Sharpen.sharp(img);

			// Extract connected components
//			cvCanny(src, src, 0, thresh, 5);
			
			CvSeq contourSeq = new CvSeq(null);
			CvMemStorage storage = CvMemStorage.create();
			cvFindContours(src, storage, contourSeq,
					Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);

			// Convert to a Scala collection for easier manipulation

			List contours = toScalaSeq(contourSeq);

			// Draw extracted contours
			IplImage colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);
			cvCvtColor(src, colorDst, CV_GRAY2BGR);
			drawAllContours(colorDst, contours, CvScalar.RED, 1);
//			 OpenCVUtils.show(colorDst, "Contours");

			
			
			// Eliminate too short or too long contours
			int lengthMin = 1;
			int lengthMax = 100;
			// List filteredContours = contours.filter(contour => (lengthMin <
			// contour.total() && contour.total() < lengthMax))
			List filteredContours = new ArrayList();
			for(int i=0;i<contours.size();i++){
				CvSeq c = (CvSeq)contours.get(i);
				System.out.println(c.total());
				if(c.total() > lengthMin && c.total() < lengthMax){
					filteredContours.add(contours.get(i));
				}
			}
			IplImage colorDest2 = OpenCVUtils.loadOrExit(new File(STAMP),
					CV_LOAD_IMAGE_COLOR);
//			 drawAllContours(colorDest2, filteredContours, CvScalar.BLUE, 2);

			//
			// Second part computes shapes descriptors from the extracted
			// contours.
			//

			System.out.println(filteredContours.size());
			int k=0;
			// Testing the bounding box
			
			
			for (int i = 0; i < filteredContours.size(); i++) {
				int update = 0;
				CvRect rectangle0 = cvBoundingRect((CvArr) filteredContours.get(i),
						update);
				cvRectangleR(colorDest2, rectangle0, CvScalar.GREEN, 2, CV_AA,
						0);
				
//				if(rectangle0.height() > 40 && rectangle0.width() > 40 && rectangle0.height() <100 && rectangle0.width() <100 ){
//					k++;
//					if(matching(rect,rectangle0)){
//						continue;
//					}
//					//if(k==2){
//					CvSeq c = (CvSeq)filteredContours.get(i);
//					System.out.println(c.total());
//					System.out.println(rectangle0.height() +"," +rectangle0.width()+"-"+rectangle0.x()+","+rectangle0.y());
//					cvRectangleR(colorDest2, rectangle0, CvScalar.GREEN, 2, CV_AA,
//						0);
//					rect.add(rectangle0);
//					//}
//				}
				
			}
			OpenCVUtils.show(colorDest2, "Contours");
			// // Testing the enclosing circle
			// val center1 = new CvPoint2D32f()
			// val radius1 = new Array[Float](1)
			// cvMinEnclosingCircle(filteredContours(1), center1, radius1)
			// cvCircle(colorDest2, cvPointFrom32f(center1), radius1(0).toInt,
			// CvScalar.MAGENTA, 2, CV_AA, 0)
			//
			// // Testing the approximate polygon
			// val poly2 = cvApproxPoly(filteredContours(2),
			// Loader.sizeof(classOf[CvContour]), storage, CV_POLY_APPROX_DP, 5,
			// 1)
			// // Draw only the first poly
			// cvDrawContours(colorDest2, poly2, CvScalar.MAGENTA,
			// CvScalar.MAGENTA, -1, 2, CV_AA)
			//
			// // Testing the convex hull
			// val orientation = CV_CLOCKWISE
			// val returnPoints = 1
			// val convexHullPoints3 = cvConvexHull2(filteredContours(3),
			// storage, CV_CLOCKWISE, returnPoints)
			// drawAllContours(colorDest2, toScalaSeq(convexHullPoints3),
			// CvScalar.MAGENTA, 2)
			//
			// Testing the moments for all filtered contours, and marking center
			// of mass on the image
			// for (c <- filteredContours) {
			// val moments = new CvMoments()
			// cvMoments(c, moments, 0)
			// val xCenter = math.round(moments.m10() / moments.m00).toInt
			// val yCenter = math.round(moments.m01() / moments.m00).toInt
			// cvCircle(colorDest2, cvPoint(xCenter, yCenter), 2,
			// CvScalar.GREEN, 2, CV_AA, 0)
			// }

			// Show the image with marked contours and shape descriptors
			// show(colorDest2, "Contours Filtered");

			// ------------------------------------------------------------------------

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList(rect);
	}

	/**
	 * Draw `contours` on the `image`.
	 */
	
	public static boolean matching(PriorityQueue<CvRect> rects,CvRect rect0){
		for(CvRect rect:rects){
			if(Math.abs(rect.height() - rect0.height()) < 15 && Math.abs(rect.width() - rect0.width()) < 15 && Math.abs(rect.x() - rect0.x()) < 15 && Math.abs(rect.y() - rect0.y()) < 15){
				return true;
			}
		}
		return false;
		
	}
	public static void drawAllContours(IplImage image, List contours, CvScalar color,
			int width) {
		for (int i = 0; i < contours.size(); i++) {

			cvDrawContours(image, (CvSeq) contours.get(i), color, color, -1,
					width, CV_AA);
		}
		// contours.foreach(c => cvDrawContours(image, c, color, color, -1,
		// width, CV_AA));
	}

	/**
	 * Convert OpenCV sequence to a Scala collection for easier handling.
	 */
	public static List toScalaSeq(CvSeq cvSeq) {
		List seqBuilder = new ArrayList();
		CvSeq element = cvSeq;
		while (element != null && !element.isNull()) {
			if (element.elem_size() > 0) {
				seqBuilder.add(element);
			}
			element = element.h_next();
		}
		return seqBuilder;
	}

	public static void main(String[] args) {
		Shape desc = new Shape();
		desc.extractShapes();
	}

	public static void sharp() {

		try {
			File f = new File("D:/files/newStamp.png");
			BufferedImage img = ImageIO.read(f);
			for (int i = 0; i < img.getHeight(); i++) {
				for (int j = 0; j < img.getWidth(); j++) {
					// System.out.println(img.getRGB(j, i));
					if (!PixelReader.isPixelWhite(img, i, j)) {
						img.setRGB(j, i, 0);
					} else {
						// System.out.println("j,i:"+j+","+i);
					}
				}
			}

			File file = new File("D:/files/new.png");
			file.createNewFile();
			ImageIO.write(img, "png", file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	

	static class RectComparator implements Comparator<CvRect>
	{
	    @Override
	    public int compare(CvRect x, CvRect y)
	    {
	        // Assume neither string is null. Real code should
	        // probably be more robust
	    	if(x.y() > y.y()){
	    		return 1;
	    	}
	        if (x.y() < y.y())
	        {
	            return -1;
	        }
	        
	        return 0;
	    }
	}

}
