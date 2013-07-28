/*
 * Copyright (c) 2011-2012 Jarek Sacha. All Rights Reserved.
 *
 * Author's e-mail: jarek.listy at gmail.com
 */

import static com.googlecode.javacv.cpp.opencv_core.CV_8U;
import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangleR;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvSetZero;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_COLOR;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GRAY2BGR;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2BGR;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;


/**
 * The example for section "Computing components' shape descriptors" in Chapter
 * 7, page 186.
 */
public class FindShapes{

	//
	// First part is the same as in example `Ex5ExtractContours`; extracts
	// contours.
	//

	// Read input image

//	public static final String STAMP = "D:/files/newphoto.png";
//	public static final String STAMP = "D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335\\6-2-600MP0053-02012-DR-IN-0008_A_20.png";
	
	public static int thresh = 50;

	public static List<CvRect> extractShapes(String STAMP) {
		Comparator<CvRect> comparator = new RectComparator();
		List<CvRect> rect = 
	            new ArrayList<CvRect>();
		//List<CvRect> rect = new ArrayList<CvRect>();
		try {
			// val src = loadAndShowOrExit(new File("data/binaryGroup.bmp"),
			// CV_LOAD_IMAGE_GRAYSCALE);
			File f = sharp(STAMP);

			IplImage src = OpenCVUtils.loadAndShowOrExit(f,
					CV_8U);

			// IplImage src = Ex3Sharpen.sharp(img);

			// Extract connected components
//			cvCanny(src, src, 0, thresh, 5);
			
			CvSeq contourSeq = new CvSeq(null);
			CvMemStorage storage = CvMemStorage.create();
//			cvFindContours(src, storage, contourSeq,
//					Loader.sizeof(CvContour.class), CV_RETR_LIST,
//					CV_CHAIN_APPROX_SIMPLE);
			cvFindContours(src, storage, contourSeq,
					Loader.sizeof(CvContour.class),   CV_RETR_LIST ,
					CV_CHAIN_APPROX_SIMPLE );

			// Convert to a Scala collection for easier manipulation

			List contours = toScalaSeq(contourSeq);

			// Draw extracted contours
			IplImage colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);
			cvCvtColor(src, colorDst, CV_GRAY2BGR);
//			drawAllContours(colorDst, contours, CvScalar.RED, 1);
//			 OpenCVUtils.show(colorDst, "Contours");

			
			
			// Eliminate too short or too long contours
			int lengthMin = 40;
			int lengthMax = Integer.MAX_VALUE;
			// List filteredContours = contours.filter(contour => (lengthMin <
			// contour.total() && contour.total() < lengthMax))
			List filteredContours = new ArrayList();
			for(int i=0;i<contours.size();i++){
//				CvSeq c = (CvSeq)contours.get(i);
				CvArr c = (CvArr)contours.get(i);
				
//				System.out.println("elemsize:"+c.elem_size());
//				System.out.println("cap:"+c.capacity());
//				System.out.println("total:"+c.total());
//				System.out.println(c.total());
//				if(c.total() > lengthMin && c.total() < lengthMax ){
				
//				if(c.total()>8){
					filteredContours.add(contours.get(i));
//				}
//				}
			}
			IplImage colorDest2 = OpenCVUtils.loadOrExit(f,
					CV_LOAD_IMAGE_COLOR);
//			 drawAllContours(colorDest2, filteredContours, CvScalar.BLUE, 2);
//			 OpenCVUtils.show(colorDest2, "Contours1");

			//
			// Second part computes shapes descriptors from the extracted
			// contours.
			//

			System.out.println(filteredContours.size());
			int k=0;
			// Testing the bounding box
			System.out.println("width:"+src.width());
			
			CvScalar colors[]  = {CvScalar.GRAY, CvScalar.RED, CvScalar.GREEN, CvScalar.BLUE, CvScalar.CYAN,CvScalar.MAGENTA,CvScalar.YELLOW};
			
			for (int i = 0; i < filteredContours.size(); i++) {
				int update = 1;
				CvArr arr=(CvArr) filteredContours.get(i);
				
				
//				CvRect rectangle0 = cvBoundingRect((CvArr) filteredContours.get(i),
//						update);
//				
//				if(rectangle0.isNull())
//				System.out.println(rectangle0.isNull());
////				System.out.println("capa:"+rectangle0.isNull());
////				System.out.println("limit:"+rectangle0.limit());
////				System.out.println("pos:"+rectangle0.position());
////				
//				
//				
//				if((rectangle0.height() > src.height() / 100) && (rectangle0.width() > src.width() /80)){
//					System.out.println(rectangle0.height() +"," +rectangle0.width()+"-"+rectangle0.x()+","+rectangle0.y()+":"+rectangle0.height()*rectangle0.width());
//					rect.add(rectangle0);
//					cvRectangleR(colorDest2, rectangle0, colors[i%7], 2, 8,
//						0);
//				}
				
				 CvSeq poly2 = cvApproxPoly((CvArr)filteredContours.get(i),
						 Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, 100,
						 0);
				 System.out.println(poly2.total());
				 if(poly2.total() == 4){
				 cvDrawContours(colorDest2, poly2, CvScalar.MAGENTA,
						 CvScalar.MAGENTA, 1, 2, CV_AA);
				 }
				 else if(poly2.total() >4){
					 cvDrawContours(colorDest2, poly2, CvScalar.YELLOW,
							 CvScalar.YELLOW, 1, 2, CV_AA);
				 }
				 
				 
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
			System.out.println("size:"+rect.size());
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
		RectComparator compar = new RectComparator();
		PriorityQueue<CvRect> queue = new PriorityQueue<CvRect>(rect.size(),compar);
		queue.addAll(rect);
		Iterator<CvRect> itr = queue.iterator();
		
//		CvRect first = itr.next();
//		CvRect second = itr.next();
//		CvRect third = itr.next();
		CvRect first = queue.remove();
		CvRect second = queue.remove();
		CvRect third = queue.peek();
		if((((second.x()-third.x()) - ((second.x()+second.width())) - (third.x()+third.width()) < 5) && (second.x()-third.x()) < 100)
				&& (((second.y()-third.y()) - ((second.y()+second.height())) - (third.y()+third.height())<5) && (second.y()-third.y()) < 100)){
			System.out.println("double bounday");
			System.out.println("maxX,maxY:"+(third.x()+third.width()+","+(third.y()+third.height())));
			
		}
		else{
			System.out.println("maxX,maxY1:"+(second.x()+second.width()+","+(second.y()+second.height())));
		}
			
		System.out.println("first:"+first.x()+","+first.y()+","+(first.x()+first.width())+","+(first.y()+first.height()));
		System.out.println("second:"+second.x()+","+second.y()+","+(second.x()+second.width())+","+(second.y()+second.height()));
		System.out.println("third:"+third.x()+","+third.y()+","+(third.x()+third.width())+","+(third.y()+third.height()));
		
		return new ArrayList<CvRect>(queue);
	}
	
	public static List<CvRect> extractShapes1(String STAMP) {
		
		List<CvRect> rect = 
	            new ArrayList<CvRect>();
		//List<CvRect> rect = new ArrayList<CvRect>();
		
		IplImage src = cvLoadImage(STAMP, CV_8U);
		IplImage gray = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 1); 
		cvCvtColor(src, gray, CV_RGB2BGR);
		cvSmooth(gray, gray, CV_GAUSSIAN, 7); 

		IplImage cc_img = cvCreateImage(cvGetSize(gray), gray.depth(), 3); 
		cvSetZero(cc_img);
		cvScalar(7.0,7.0,7.0,7.0);

		cvCanny(gray, gray, 10, 30, 3); 

		try {
			// val src = loadAndShowOrExit(new File("data/binaryGroup.bmp"),
			// CV_LOAD_IMAGE_GRAYSCALE);
			File f = sharp(STAMP);

//			IplImage src = OpenCVUtils.loadAndShowOrExit(f,
//					CV_8U);

			// IplImage src = Ex3Sharpen.sharp(img);

			// Extract connected components
//			cvCanny(src, src, 0, thresh, 5);
			
			CvSeq contourSeq = new CvSeq(null);
			CvMemStorage storage = CvMemStorage.create();
//			cvFindContours(src, storage, contourSeq,
//					Loader.sizeof(CvContour.class), CV_RETR_LIST,
//					CV_CHAIN_APPROX_SIMPLE);
			cvFindContours(gray, storage, contourSeq,
					Loader.sizeof(CvContour.class),   CV_RETR_LIST ,
					CV_CHAIN_APPROX_SIMPLE );

			// Convert to a Scala collection for easier manipulation

			List contours = toScalaSeq(contourSeq);

			// Draw extracted contours
			IplImage colorDst = cvCreateImage(cvGetSize(gray), gray.depth(), 3);
			cvCvtColor(gray, colorDst, CV_GRAY2BGR);
//			drawAllContours(colorDst, contours, CvScalar.RED, 1);
//			 OpenCVUtils.show(colorDst, "Contours");

			
			
			// Eliminate too short or too long contours
			int lengthMin = 40;
			int lengthMax = Integer.MAX_VALUE;
			// List filteredContours = contours.filter(contour => (lengthMin <
			// contour.total() && contour.total() < lengthMax))
			List filteredContours = new ArrayList();
			for(int i=0;i<contours.size();i++){
				CvSeq c = (CvSeq)contours.get(i);
//				System.out.println(c.total());
//				if(c.total() > lengthMin && c.total() < lengthMax ){
					filteredContours.add(contours.get(i));
//				}
			}
			IplImage colorDest2 = OpenCVUtils.loadOrExit(f,
					CV_LOAD_IMAGE_COLOR);
//			 drawAllContours(colorDest2, filteredContours, CvScalar.BLUE, 2);

			//
			// Second part computes shapes descriptors from the extracted
			// contours.
			//

			System.out.println(filteredContours.size());
			int k=0;
			// Testing the bounding box
			System.out.println("width:"+src.width());
			
			CvScalar colors[]  = {CvScalar.GRAY, CvScalar.RED, CvScalar.GREEN, CvScalar.BLUE, CvScalar.CYAN,CvScalar.MAGENTA,CvScalar.YELLOW};
			
			for (int i = 0; i < filteredContours.size(); i++) {
				CvSeq seq = (CvSeq)filteredContours.get(i);
				
				int update = 1;
				CvRect rectangle0 = cvBoundingRect((CvArr) filteredContours.get(i),
						update);
				
				
//				if((rectangle0.y() > src.height() * 5/6) && (rectangle0.x() >  src.width() * 2/3) && (rectangle0.height() > src.height() / 100) && (rectangle0.width() > src.width() /80)){
//					System.out.println(rectangle0.height() +"," +rectangle0.width()+"-"+rectangle0.x()+","+rectangle0.y()+":"+rectangle0.height()*rectangle0.width());
//					rect.add(rectangle0);
//					cvRectangleR(colorDest2, rectangle0, colors[i%7], 2, CV_AA,
//						0);
//				}
				
				
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
			System.out.println("size:"+rect.size());
			OpenCVUtils.show(colorDest2, "Contours");
			// // Testing the enclosing circle
			// val center1 = new CvPoint2D32f()
			// val radius1 = new Array[Float](1)
			// cvMinEnclosingCircle(filteredContours(1), center1, radius1)
			// cvCircle(colorDest2, cvPointFrom32f(center1), radius1(0).toInt,
			// CvScalar.MAGENTA, 2, CV_AA, 0)
			//
			// // Testing the approximate polygon
//			 CvSeq poly2 = cvApproxPoly(filteredContours.get(i),
//			 Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, 5,
//			 1);
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
		return rect;
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
		FindShapes desc = new FindShapes();
		List<CvRect> rects = desc.extractShapes("D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335\\6-2-600MP0053-02012-DR-IN-0008_A_20.png");
		
	}

	public static File sharp(String s) {
		File file = new File("D:\\tmp\\file"+UUID.randomUUID());
		try {
			File f = new File(s);
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

			
			file.createNewFile();
			ImageIO.write(img, "png", file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return file;
	}
	
	

	static class RectComparator implements Comparator<CvRect>
	{
	    @Override
	    public int compare(CvRect a, CvRect b)
	    {
	        // Assume neither string is null. Real code should
	        // probably be more robust
//	    	if(((a.y() +a.height())< (b.y()+b.height())) && ((a.x()+a.width()) < (b.x()+b.width()))){
//	    		return 1;
//	    	}
//	    	if(((a.y() +a.height())>(b.y()+b.height())) && ((a.x()+a.width()) >(b.x()+b.width()))){
//	    		return -1;
//	    	}
	    	if((a.width()*a.height()) < (b.width() *b.height())){
	    		return 1;
	    	}
	    	if((a.width()*a.height()) > (b.width() *b.height())){
	    		return -1;
	    	}
	        
	        return 0;
	    }
	}

}
