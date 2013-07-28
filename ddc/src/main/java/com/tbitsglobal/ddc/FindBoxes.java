import static com.googlecode.javacv.cpp.opencv_core.CV_32F;
import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_WHOLE_SEQ;
import static com.googlecode.javacv.cpp.opencv_core.cvCloneImage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangleR;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_core.cvSplit;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN_5x5;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCheckContourConvexity;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourArea;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvPyrDown;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvPyrUp;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FindBoxes {
	public static int thresh = 200;
	public static int N = 12;
	public static int DOC1TOP = 2;
	public static int DOC1RIGHT = 5;
	public static int DOC2TOP = 3;
	public static int DOC2RIGHT = 5;
	public static int w = 0;

	public static List<CvRect> findRectangles(IplImage src, CvMemStorage storage)

	{

		CvRect p = new CvRect(), p1 = new CvRect();
		List<CvRect> rect = new ArrayList<CvRect>();

		

		// int count = 0;

		// CvSeq squares = new CvContour();
		// squares = cvCreateSeq(0, Loader.sizeof(CvContour.class),
		// Loader.sizeof(CvSeq.class), storage);

		IplImage pyr = null, timg = null, gray = null, tgray;
		// finalImg = cvCloneImage(src);
		IplImage im1 = null;
		im1 = cvCloneImage(src);
		timg = cvCloneImage(src);

		CvSize sz = cvSize(src.width(), src.height());

		IplImage[] channels = { cvCreateImage(sz, 8, 1),
				cvCreateImage(sz, 8, 1), cvCreateImage(sz, 8, 1) };

		try {

			tgray = cvCreateImage(sz, src.depth(), 1);
			gray = cvCreateImage(sz, src.depth(), 1);
			pyr = cvCreateImage(cvSize(sz.width() / 2, sz.height() / 2),
					src.depth(), src.nChannels());
			// System.out.println("channel:" + src.nChannels());
			// down-scale and upscale the image to filter out the noise
			cvPyrDown(im1, pyr, CV_GAUSSIAN_5x5);
			cvPyrUp(pyr, im1, CV_GAUSSIAN_5x5);

			// cvRunningAvg(timg,finalImg,0.1,null);

			cvConvertScaleAbs(im1, timg, 0.95, 0);
			// cvSaveImage("D://a"+(w++)+".png", im1);
			CvSeq contours = new CvContour();
			CvScalar colors[] = { CvScalar.RED, CvScalar.GREEN, CvScalar.BLUE,
					CvScalar.CYAN, CvScalar.MAGENTA, CvScalar.YELLOW };
			// request closing of the application when the image window is
			// closed
			// show image on window
			// find squares in every color plane of the image
			p.x(0);
			p.y(0);
			p.height(0);
			p.width(0);
			p1.x(Integer.MAX_VALUE);
			p1.width();
			p1.y(0);
			p1.height(0);

			if (src.nChannels() > 1) {
				cvSplit(timg, channels[0], channels[1], channels[2], null);
			} else {
				tgray = cvCloneImage(timg);
			}
			for (int c = 0; c < 3; c++) {

				try {

					// channels[c] = cvCreateImage(sz, 8, 1);

					tgray = channels[c]; // try several threshold levels
					// cvSaveImage("D://b" + c + ".png", tgray);
					cvThreshold(
							tgray,
							tgray,
							200,
							255,
							com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV);
					// cvSaveImage("D://a" + c + ".png", tgray);
					int q = 1;

					for (int l = 1; l < N; l++) {
						// hack: use Canny instead of zero threshold level.
						// Canny helps to catch squares with gradient shading
						// System.out.println(c + "," + l);
						q = l;
						if (l == 0) {
							// apply Canny. Take the upper threshold from slider
							// and set the lower to 0 (which forces edges
							// merging)
							cvCanny(tgray, gray, 0, 250, 3);
							// dilate canny output to remove potential
							// // holes between edge segments
							cvDilate(gray, gray, null, 1);
						} else {
							// apply threshold if l!=0:
							cvThreshold(
									tgray,
									gray,
									(l + 1) * 255 / N,
									255,
									com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY);
							// cvThreshold(tgray, gray, 200, 254,
							// com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV);
						}

						// cvSaveImage("D://c" + c + l + ".png", gray);
						// find contours and store them all as a list
						cvFindContours(gray, storage, contours,
								Loader.sizeof(CvContour.class), CV_RETR_LIST,
								CV_CHAIN_APPROX_SIMPLE);

						CvSeq approx;

						// test each contour
						while (contours != null && !contours.isNull()) {
							if (contours.elem_size() > 0) {
								approx = cvApproxPoly(contours,
										Loader.sizeof(CvContour.class),
										storage, CV_POLY_APPROX_DP,
										cvContourPerimeter(contours) * 0.03, 0);
								// 3549, 3250

								// cvDrawContours(timg, approx,
								// colors[4%6],
								// colors[4%6], 1, 2, CV_AA);
								//
								if ((approx.total() == 4)
										&& Math.abs(cvContourArea(approx,
												CV_WHOLE_SEQ, 0)) > 100
										&& cvCheckContourConvexity(approx) != 0) {

									double maxCosine = 0;
									//
									for (int j = 2; j < 5; j++) {
										// find the maximum cosine of the angle
										// between
										// joint edges
										double cosine = Math.abs(angle(
												new CvPoint(cvGetSeqElem(
														approx, j % 4)),
												new CvPoint(cvGetSeqElem(
														approx, j - 2)),
												new CvPoint(cvGetSeqElem(
														approx, j - 1))));
										// System.out.println(cosine);
										maxCosine = Math.max(maxCosine, cosine);
									}

									if (maxCosine < 0.3) {
										// System.out.println(maxCosine);
										// cvSeqPush(squares, approx);

										CvRect rectangle0 = cvBoundingRect(
												approx, 0);
										// if ((Math.abs(rectangle0.x()) < (0.30
										// *
										// (float) timg
										// .width())) &&
										// (Math.abs(rectangle0.x()) >
										// (0.20 * (float) timg
										// .width()))
										// && (Math.abs(rectangle0.y()) > (0.80
										// *
										// (float) timg
										// .height())) && rectangle0.width() >
										// (0.05
										// *
										// (float) timg
										// .width())) {
										// System.out.println(rectangle0.x() +
										// ","
										// + rectangle0.y() + "-:"
										// + rectangle0.width() + ","
										// + rectangle0.height());

										// }

										// if(rectangle0.x() < 1700 &&
										// rectangle0.x() > 900
										// && rectangle0.y() > 3150 &&
										// rectangle0.y() < 3480
										// ){
										// System.out.println("cosine:"+maxCosine);
										// cvRectangleR(timg,
										// rectangle0,
										// colors[(1) % 6], 2,
										// CV_AA,
										// 0);
										// System.out.println("rect:"
										// + rectangle0.x() + ","
										// + rectangle0.y() + ","
										// + rectangle0.width() + ","
										// + rectangle0.height());

										// }
										if ((rectangle0.height() < (0.80 * (float) timg
												.height()))
												&& (rectangle0.width() < (0.80 * (float) timg
														.width()))
												&& (rectangle0.width() >= 38)
												&& (rectangle0.height() >= 38)) {

											if (added(rect, rectangle0)) {
												// cvRectangleR(timg,
												// rectangle0,
												// colors[(count +2 )% 6], 2,
												// CV_AA,
												// 0);
												// System.out.println("not added");
												if (rectangle0 != null && !rectangle0.isNull()) {
													
//													rectangle0.deallocate();
//													rectangle0.setNull();
//													rectangle0 = null;
												}
											} else {
												rect.add(rectangle0);
												//

												// System.out.println("added");
												// if(rectangle0.x() < 1150 &&
												// rectangle0.x() > 1100 &&
												// rectangle0.y() > 3200 &&
												// rectangle0.y() < 3250 ){
												// cvRectangleR(timg,
												// rectangle0,
												// colors[0 % 6], 2,
												// CV_AA,
												// 0);

												// System.out.println("rect:"
												// + rectangle0.x() + ","
												// + rectangle0.y() + ","
												// + rectangle0.width() + ","
												// + rectangle0.height());
												// }
												// count++;
												//
												if (((((rectangle0.x() + rectangle0
														.width()) - (p.x() + p
														.width())) >= 0) || (((p
														.width() + p.x()) - (rectangle0
														.x() + rectangle0
														.width())) < 15))
														&& ((((rectangle0.y() + rectangle0
																.height()) - (p
																.y() + p
																.height())) >= 0) || (((p
																.height() + p
																.y()) - (rectangle0
																.y() + rectangle0
																.height())) < 15))) {

													p = rectangle0;

												}
												if ((((rectangle0.x() - p1.x()) <= 0) || ((rectangle0
														.x() - p1.x()) < 15))
														&& ((((rectangle0.y() + rectangle0
																.height()) - (p1
																.y() + p1
																.height())) >= 0) || (((p1
																.height() + p1
																.y()) - (rectangle0
																.y() + rectangle0
																.height())) < 15))) {
													p1 = rectangle0;
												}

											}
										} else {
											if (rectangle0 != null && !rectangle0.isNull()) {
//												try{
//													rectangle0.deallocate();
//													}catch(Exception f){
//														
//													}
//												
//												rectangle0.setNull();
//												rectangle0 = null;
											}
										}

									} else {
										if (approx != null && !approx.isNull()) {
//											try{
//												approx.deallocate();
//												}catch(Exception f){
//												
//												}
//											approx.setNull();
//											approx = null;
										}
									}

								} else {
									if (approx != null && !approx.isNull()) {
//										try{
//											approx.deallocate();
//											}catch(Exception f){
//											
//											}
//										approx.setNull();
//										approx = null;
									}
								}
							}
							contours = contours.h_next();
						}
						contours = new CvContour();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					cvReleaseImage(tgray);
					// cvReleaseImage(gray);
					tgray = null;
					// gray=null;
					System.gc();
				}
			}
			// System.out.println("no of rectangles:"+rect.size());
			// cvRectangleR(timg, p,
			// colors[count % 6], 2, CV_AA, 0);
			// cvRectangleR(timg, p1,
			// colors[count % 6], 2, CV_AA, 0);
			// System.out.println("origin0:" + p.x() + "," + p.y());
			// cvRectangleR(timg, p, colors[1], 2, CV_AA, 0);
			//
			// cvRectangleR(timg, p1, colors[1], 2, CV_AA, 0);
			/*
			 * finding the boxes start
			 */

			// first top
			// count++;
			//
			// List<CvRect> verticalRect = new ArrayList<CvRect>();
			// for (CvRect r : rect) {
			// if (Math.abs(r.x() - p1.x()) <= 10
			// && Math.abs(r.width() - p1.width()) <= 10) {
			// verticalRect.add(r);
			// // cvRectangleR(timg, r,
			// // colors[count % 6], 2, CV_AA, 0);
			// // cvRectangleR(timg, r,
			// // colors[count % 6], 2, CV_AA, 0);
			// }
			// }
			// OpenCVUtils.show(timg, "Contours1");
			// Comparator<CvRect> comp1 = new TopComparator();
			// Collections.sort(verticalRect, comp1);
			// CvRect ele = verticalRect.get(DOC1TOP);
			// CvRect doc2 = verticalRect.get(DOC2TOP);
			// count++;
			// List<CvRect> horizontalRect = new ArrayList<CvRect>();
			// for (CvRect r : rect) {
			// if (Math.abs(r.y() - ele.y()) <= 10
			// && Math.abs(r.height() - ele.height()) <= 10) {
			// horizontalRect.add(r);
			// // cvRectangleR(timg, r,
			// // colors[count % 6], 2, CV_AA, 0);
			// }
			// }
			//
			// Comparator<CvRect> comp2 = new RightComparator();
			// Collections.sort(horizontalRect, comp2);
			// dr1 = horizontalRect.get(DOC1RIGHT);
			// count++;
			// // cvRectangleR(timg, ele,
			// // colors[count % 6], 2, CV_AA, 0);
			// count++;
			// cvRectangleR(timg, dr1,
			// colors[count % 6], 2, CV_AA, 0);
			//
			// horizontalRect.clear();
			//
			// for (CvRect r : rect) {
			// if (Math.abs(r.y() - doc2.y()) <= 10
			// && Math.abs(r.height() - doc2.height()) <= 10) {
			// horizontalRect.add(r);
			// // cvRectangleR(timg, r,
			// // colors[count % 6], 2, CV_AA, 0);
			// }
			// }
			//
			// Collections.sort(horizontalRect, comp2);
			// dr2 = horizontalRect.get(DOC2RIGHT);
			// count++;
			//
			// horizontalRect.clear();
			// verticalRect.clear();
			// horizontalRect = null;
			// verticalRect = null;
			//
			// cvRectangleR(timg, dr2,
			// colors[count % 6], 2, CV_AA, 0);

			/*
			 * end
			 */

			rect.remove(p);
			rect.remove(p1);

			rect.add(0, p);
			rect.add(1, p1);

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			cvReleaseImage(channels[0]);
			cvReleaseImage(channels[1]);
			cvReleaseImage(channels[2]);
			channels[0].setNull();
			channels[1].setNull();
			channels[2].setNull();
			// System.out.println("count:" + count);

			// OpenCVUtils.show(timg, "Contours");
			// timg.deallocate();
			cvReleaseImage(im1);
			cvReleaseImage(timg);

			// cvReleaseImage(tgray);
			cvReleaseImage(gray);
			cvReleaseImage(pyr);
			// timg.release();
			// tgray.release();
			// gray.release();
			// pyr.release();
			timg = null;
			tgray = null;
			gray = null;
			pyr = null;
			im1 = null;
			// src=null;
			System.gc();
		}
		// timg.release();+
		// tgray.release();
		// gray.release();
		// pyr.release();
		// src.release();
		// System.out.println("origin1:" + p.x() + "," + p.y());
		return rect;
	}

	public static final String STAMP = "D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335\\16-2-600MP0053-02012-DR-ME-0017_A_20.png";

	public static void main(String[] args) throws Exception {
		// IplImage src = cvLoadImage(STAMP, CV_32F);
		// // IplImage src = IplImage(STAMP, CV_32F);
		// CvPoint p = new CvPoint();
		//
		// FindRectangles.findRectangles(src, p);
		//
		// System.out.println("origin:" + p.x() + "," + p.y());

		// IplImage colorDest2 = OpenCVUtils.loadOrExit(new File(STAMP),
		// CV_LOAD_IMAGE_COLOR);
		// while(seq != null && !seq.isNull()){
		// cvDrawContours(colorDest2, seq, CvScalar.YELLOW,
		// CvScalar.YELLOW, 1, 2, CV_AA);
		// seq = seq.h_next();
		// }

		File f = new File(
				"D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335");
		File fi[] = f.listFiles();
		for (int i = 0; i < fi.length; i++) {
			if (fi[i].isFile()
					&& fi[i].getName().endsWith(
							"77-2-600MP0053-02012-DR-IN-0033_A_20.png")
					&& (fi[i].getName().startsWith("") || fi[i].getName()
							.startsWith(""))) {
				System.out.println("file:" + fi[i].getAbsolutePath());
				BufferedImage im = ImageIO.read(fi[i]);

				IplImage src = // IplImage.createFrom(im);
				cvLoadImage(fi[i].getAbsolutePath(), CV_32F);

				CvRect p = new CvRect();
				CvRect q = new CvRect();
				CvMemStorage storage = CvMemStorage.create();

				List<CvRect> rect = FindBoxes.findRectangles(src,storage);

				for (CvRect r : rect) {

					r.deallocate();

					r.setNull();
				}
				CvRect.deallocateReferences();
				im.flush();
				cvReleaseImage(src);
				im = null;
				src = null;

				rect.clear();
				rect = null;

				System.gc();

				// System.out.println("origin:" + p.x() + "," + p.y());

			}
		}

	}

	public static void main2(String[] args) {
		for (int i = 0; i < 1000000; i++) {
			IplImage image = cvLoadImage(STAMP);
			cvReleaseImage(image);
			image = null;
			System.gc();
		}
	}

	public static boolean added(List<CvRect> rect, CvRect rectangle0) {

		for (CvRect r : rect) {
			double dX = Math.abs(r.x() - rectangle0.x());
			double dY = Math.abs(r.y() - rectangle0.y());
			double dWidth = Math.abs(r.width() - rectangle0.width());
			double dHeight = Math.abs(r.height() - rectangle0.height());

			if ((dX <= 10) && (dY <= 10) && (dWidth <= 10) && (dHeight <= 10)) {
				return true;

			}

		}
		return false;

	}

	/**
	 * cos-1((P122 + P132 - P232)/(2 * P12 * P13))
	 * 
	 * where P12 is the length of the segment from P1 to P2, calculated by
	 * 
	 * sqrt((P1x - P2x)2 + (P1y - P2y)2)
	 */

	public static double angle(CvPoint P2, CvPoint P3, CvPoint P1) {
		double result = 0;
		double P12 = Math.sqrt(((P1.x() - P2.x()) * ((P1.x() - P2.x())))
				+ ((P1.y() - P2.y()) * ((P1.y() - P2.y()))));
		double P23 = Math.sqrt(((P3.x() - P2.x()) * ((P3.x() - P2.x())))
				+ ((P3.y() - P2.y()) * ((P3.y() - P2.y()))));
		double P13 = Math.sqrt(((P1.x() - P3.x()) * ((P1.x() - P3.x())))
				+ ((P1.y() - P3.y()) * ((P1.y() - P3.y()))));
		// System.out.println(P1.x()+","+P2.x()+","+P3.x());
		// System.out.println(P1.y()+","+P2.y()+","+P3.y());
		result = (((P12 * P12) + (P13 * P13) - (P23 * P23))) / (2 * P12 * P13);

		return result;

	}

	static class TopComparator implements Comparator<CvRect> {
		@Override
		public int compare(CvRect x, CvRect y) {
			// Assume neither string is null. Real code should
			// probably be more robust
			if (x.y() < y.y()) {
				return 1;
			}
			if (x.y() > y.y()) {
				return -1;
			}

			return 0;
		}
	}

	static class RightComparator implements Comparator<CvRect> {
		@Override
		public int compare(CvRect x, CvRect y) {
			// Assume neither string is null. Real code should
			// probably be more robust
			if (x.x() > y.x()) {
				return 1;
			}
			if (x.x() < y.x()) {
				return -1;
			}

			return 0;
		}
	}

}
