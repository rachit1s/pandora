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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

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

public class FindRectangles {
	public static int thresh = 200;
	public static int N = 12;

	public static List<CvRect> findRectangles(String IMG, CvMemStorage storage) {

		IplImage src = // IplImage.createFrom(im);
		cvLoadImage(IMG, CV_32F);
		Comparator comp = new RectComparator();
		PriorityQueue<CvRect> rect = new PriorityQueue<CvRect>(10, comp);

		CvScalar colors[] = { CvScalar.RED, CvScalar.GREEN, CvScalar.BLUE,
				CvScalar.CYAN, CvScalar.MAGENTA, CvScalar.YELLOW };

		
		int count = 0;

		// CvSeq squares = new CvContour();
		// squares = cvCreateSeq(0, Loader.sizeof(CvContour.class),
		// Loader.sizeof(CvSeq.class), storage);

		CvSize sz = cvSize(src.width(), src.height());
		IplImage pyr = null, timg = null, gray = null, tgray;
		IplImage[] channels = { cvCreateImage(sz, 8, 1),
				cvCreateImage(sz, 8, 1), cvCreateImage(sz, 8, 1) };
		List<CvRect> list = null;
		try {
			timg = cvCloneImage(src);

			tgray = cvCreateImage(sz, src.depth(), 1);
			gray = cvCreateImage(sz, src.depth(), 1);
			pyr = cvCreateImage(cvSize(sz.width() / 2, sz.height() / 2),
					src.depth(), src.nChannels());
			// System.out.println("channel:" + src.nChannels());
			// down-scale and upscale the image to filter out the noise
			cvPyrDown(timg, pyr, CV_GAUSSIAN_5x5);
			cvPyrUp(pyr, timg, CV_GAUSSIAN_5x5);

			CvSeq contours = new CvContour();

			// request closing of the application when the image window is
			// closed
			// show image on window
			// find squares in every color plane of the image

			if (src.nChannels() > 1) {
				cvSplit(timg, channels[0], channels[1], channels[2], null);
			} else {
				tgray = cvCloneImage(timg);
			}
			for (int c = 0; c < 3; c++) {
				try {
					// channels[c] = cvCreateImage(sz, 8, 1);

					tgray = channels[c]; // try several threshold levels
					// cvSaveImage("D:b" + c + ".png", tgray);
					cvThreshold(
							tgray,
							tgray,
							200,
							255,
							com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV);

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

								if (approx.total() == 4
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
									if (maxCosine < 0.2) {
										// System.out.println(maxCosine);
										// cvSeqPush(squares, approx);
										// cvDrawContours(timg, approx,
										// colors[count%6],
										// colors[count%6], 1, 2, CV_AA);
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
										// * (float) timg
										// .width())) {
										// System.out.println(rectangle0.x() +
										// ","
										// + rectangle0.y() + "-:"
										// + rectangle0.width() + ","
										// + rectangle0.height());
										// cvRectangleR(timg, rectangle0,
										// colors[count % 6], 2, CV_AA, 0);

										// }
										if (rectangle0.height() < 0.25 * src
												.height()
												&& rectangle0.width() < 0.25 * src
														.width()
												&& !matching(rect, rectangle0)
												&& rectangle0.height() > 20
												&& rectangle0.width() > 20) {

											// if(matching(rect,rectangle0)){
											// continue;
											// }

											rect.add(rectangle0);
										} else {
											if (rectangle0 != null && !rectangle0.isNull()) {
//												rectangle0.deallocate();
//												rectangle0.setNull();
//												rectangle0 = null;
											}
										}

										// if (
										// // true
										// (rectangle0.height() < (0.80 *
										// (float)
										// timg
										// .height()))
										// && (rectangle0.width() < (0.80 *
										// (float)
										// timg
										// .width()))
										// && (rectangle0.width() > 50)
										// && (rectangle0.height() > 50)
										// && (((rectangle0.x() + rectangle0
										// .width()) - p.x() >= 0) || (p
										// .x() - (rectangle0.x() + rectangle0
										// .width())) < 2)
										// && (((rectangle0.y() + rectangle0
										// .height()) - p.y() >= 0) || (p
										// .y() - (rectangle0.y() + rectangle0
										// .height())) < 2)) {
										//
										//
										//
										// p.x(rectangle0.x() +
										// rectangle0.width());
										// p.y(rectangle0.y() +
										// rectangle0.height());
										// }
										//
										// if (
										// // true
										// (rectangle0.height() < (0.80 *
										// (float)
										// timg
										// .height()))
										// && (rectangle0.width() < (0.80 *
										// (float)
										// timg
										// .width()))
										// && (rectangle0.width() > 40)
										// && (rectangle0.height() > 40)
										// && (((rectangle0.x()) - p1.x() <= 0)
										// ||
										// (( rectangle0.x() -p1.x()) < 2))
										// && (((rectangle0.y() + rectangle0
										// .height()) - p1.y() >= 0) || (p1
										// .y() - (rectangle0.y() + rectangle0
										// .height())) < 2)) {
										//
										//
										//
										// p1.x(rectangle0.x());
										// p1.y(rectangle0.y() +
										// rectangle0.height());
										// }
										//
										// count++;
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
//										System.out.println(approx.first());
//										try{
//										approx.deallocate();
//										}catch(Exception f){
//											
//										}
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

			int startIndex = -1;
			int endIndex = -1;
			int currentArea = 0;
			int maxStart = -1;
			int maxEnd = -1;
			list = new ArrayList<CvRect>();
			while (!rect.isEmpty()) {
				list.add(rect.remove());
			}
			for (int i = 0; i < list.size(); i++) {
				CvRect rectangle = list.get(i);
				int area = rectangle.width() * rectangle.height();

				if (Math.abs(area - currentArea) > 300) {

					startIndex = i;

				}
				currentArea = area;
				endIndex = i;
				if ((maxEnd - maxStart) <= (endIndex - startIndex)) {
					maxEnd = endIndex;
					maxStart = startIndex;
				}
			}
			// for(int i=maxStart;i<maxEnd+1;i++){
			// cvRectangleR(timg, list.get(i),
			// colors[1 % 6], 2, CV_AA, 0);
			// }
			for (int i = 0; i < list.size(); i++) {
				if (i < maxStart || i > maxEnd) {
					list.get(i).deallocate();
					list.get(i).setNull();
				}
			}
			list = list.subList(maxStart, maxEnd + 1);

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			cvReleaseImage(channels[0]);
			cvReleaseImage(channels[1]);
			cvReleaseImage(channels[2]);
			channels[0].setNull();
			channels[1].setNull();
			channels[2].setNull();

			// OpenCVUtils.show(timg, "Contours");
			// timg.deallocate();
			cvReleaseImage(timg);
			// cvReleaseImage(tgray);
			cvReleaseImage(gray);
			cvReleaseImage(pyr);
			// timg.release();
			// tgray.release();
			// gray.release();
			// pyr.release();
			rect.clear();
			timg = null;
			tgray = null;
			gray = null;
			pyr = null;
			// src=null;
			System.gc();
			// timg.release();+
			// tgray.release();
			// gray.release();
			// pyr.release();
			// src.release();
		}
		// System.out.println("count:" + list.size());

		return list;
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
							"10-2-600MP0053-02012-DR-IN-0012_A_20.png")) {
				System.out.println("file:" + fi[i].getAbsolutePath());
				BufferedImage im = ImageIO.read(fi[i]);

				IplImage src = // IplImage.createFrom(im);
				cvLoadImage(fi[i].getAbsolutePath(), CV_32F);

				CvPoint p = new CvPoint();
				CvPoint q = new CvPoint();
				CvMemStorage storage = CvMemStorage.create();
				List<CvRect> rect = FindRectangles.findRectangles(fi[i]
						.getAbsolutePath(),storage);

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

	public static boolean matching(PriorityQueue<CvRect> rects, CvRect rect0) {
		for (CvRect rect : rects) {
			if (Math.abs(rect.height() - rect0.height()) < 15
					&& Math.abs(rect.width() - rect0.width()) < 15
					&& Math.abs(rect.x() - rect0.x()) < 15
					&& Math.abs(rect.y() - rect0.y()) < 15) {
				return true;
			}
		}
		return false;

	}

	static class RectComparator implements Comparator<CvRect> {
		@Override
		public int compare(CvRect x, CvRect y) {
			// Assume neither string is null. Real code should
			// probably be more robust
			if ((x.height() * x.width()) > (y.height() * y.width())) {
				return 1;
			} else if ((x.height() * x.width()) < (y.height() * y.width())) {

				return -1;
			}

			return 0;
		}
	}

}
