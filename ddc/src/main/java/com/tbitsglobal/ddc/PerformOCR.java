import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_core.CV_AA;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import com.abbyy.FREngine.BlockTypeEnum;
import com.abbyy.FREngine.Engine;
import com.abbyy.FREngine.FieldMarkingTypeEnum;
import com.abbyy.FREngine.IBlock;
import com.abbyy.FREngine.IEngine;
import com.abbyy.FREngine.IFRDocument;
import com.abbyy.FREngine.IImageDocument;
import com.abbyy.FREngine.ILayout;
import com.abbyy.FREngine.ILayoutBlocks;
import com.abbyy.FREngine.IPageProcessingParams;
import com.abbyy.FREngine.IParagraphs;
import com.abbyy.FREngine.IPrepareImageMode;
import com.abbyy.FREngine.IRegion;
import com.abbyy.FREngine.IText;
import com.abbyy.FREngine.ITextBlock;
import com.abbyy.FREngine.ImageCompressionEnum;
import com.abbyy.FREngine.RotationTypeEnum;
import com.abbyy.FREngine.TextTypeEnum;
import com.abbyy.FREngine.WritingStyleEnum;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class PerformOCR {

	private static boolean done = false;

	static CvScalar colors[] = { CvScalar.RED, CvScalar.GREEN, CvScalar.BLUE,
			CvScalar.CYAN, CvScalar.MAGENTA, CvScalar.YELLOW };

	public static DocumentsData getData(File file) {
		DocumentsData data = null;
		try {

			// 4584,3309
			// 4762,3361
			//
			// 4763,3310
			// 4811,3360

			// 4430,3258
			// 4608,3310
			//
			// 4609,3259
			// 4657,3309

			// 4435,3270
			// 4730, 3344
			//
			// 4733,3274
			// 4791,3344

			int dLeft = 1247;
			int dTop = 3153;
			int dRight = 1628;
			int dBottom = 3192;

			int dLeft1 = 1244;
			int dTop1 = 3201;
			int dRight1 = 1616;
			int dBottom1 = 3243;

			int dLeft2 = 3819;
			int dTop2 = 3284;
			int dRight2 = 4381;
			int dBottom2 = 3353;

			int rLeft = 4621;
			int rTop = 3284;
			int rRight = 4706;
			int rBottom = 3357;

			FindText application = new FindText();
			data = application.Run(file, dLeft, dTop, dRight, dBottom, dLeft1,
					dTop1, dRight1, dBottom1, dLeft2, dTop2, dRight2, dBottom2,
					rLeft, rTop, rRight, rBottom);
		} catch (Exception ex) {
			displayMessage(ex.getMessage());
		}
		return data;
	}

	public static final String STAMP1 = "D:\\AECOM.png";
	public static final String STAMP2 = "D:\\FMG.png";

	public static BufferedWriter out = null;

	public static void main(String args[]) {
		Date start = new Date();
//		List<File> drawingFiles = new ArrayList<File>();
		List<File> reportFiles = new ArrayList<File>();
		CvMemStorage storage1 = CvMemStorage.create();
		CvMemStorage storage2 = CvMemStorage.create();
		List<CvRect> stamp2Rects =  
				FindRectangles.findRectangles(STAMP2,storage2);
		List<CvRect> stamp1Rects =  //new ArrayList<CvRect>(); 
				FindRectangles.findRectangles(STAMP1,storage1);
		
		try {
			FileWriter fstream = new FileWriter("D:\\out.csv");
			out = new BufferedWriter(fstream);
//			out.write("File Name" + "," + "Revision" + "," + "Drawing No."
//					+ "," + "Title" + "," + "FMG DR No." + "," + "IMP DR. NO"
//					+ "\n");
			out.write("File Name" + "," + "Revision" + "," + "Drawing No."
					+ "," + "AECOM Decision" + "," + "FMG Decision"
					+ "\n");
			loadEngine();
			setupFREngine();
			File f = new File(
					"D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335\\");
			File fi[] = f.listFiles();
			String transmitalText = new String();
			PDF2ImageConverter conv = new PDF2ImageConverter();

			for (int i = 0; i < fi.length; i++) {
				if (fi[i].isFile() && (fi[i].getName().endsWith(".pdf") || fi[i].getName().endsWith(".pdf")) ){

					File file = conv.getImage(fi[i]);
					

					String text = FileContentExtracter.extractContent(fi[i]);
					if (text.trim().length() == 0) {
						System.out.println(file.getAbsolutePath());
//						drawingFiles.add(file);
						 process(file,stamp1Rects,stamp2Rects);
					} else {
						reportFiles.add(file);
//						transmitalText.concat(text);
					}

					

				}
			}
			

//			for (File f2 : drawingFiles) {
//				System.out.println(f2.getAbsolutePath());
//				process(f2);
//			}

			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			drawingFiles.clear();
			reportFiles.clear();
			stamp1Rects.clear();
			stamp2Rects.clear();
			storage1.release();
			storage2.release();

			try {
				out.close();
				unloadEngine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Date end = new Date();

		System.out.println("time taken:" + (end.getTime() - start.getTime()));
	}
	
	public static void getResult(String STAMP1, String STAMP2, List<File> fi){
		List<File> reportFiles = new ArrayList<File>();
		CvMemStorage storage1 = CvMemStorage.create();
		CvMemStorage storage2 = CvMemStorage.create();
		List<CvRect> stamp2Rects =  
				FindRectangles.findRectangles(STAMP2,storage2);
		List<CvRect> stamp1Rects =  //new ArrayList<CvRect>(); 
				FindRectangles.findRectangles(STAMP1,storage1);
		
		try {
			
			loadEngine();
			setupFREngine();
			
			
//			String transmitalText = new String();
			

			for (int i = 0; i < fi.size(); i++) {
				if (fi.get(i).isFile() && (fi.get(i).getName().endsWith(".pdf"))){

					File file = PDF2ImageConverter.getImage(fi.get(i));
					

					String text = FileContentExtracter.extractContent(fi.get(i));
					if (text.trim().length() == 0) {
						System.out.println(file.getAbsolutePath());
//						drawingFiles.add(file);
						 process(file,stamp1Rects,stamp2Rects);
					} else {
						reportFiles.add(file);
//						transmitalText.concat(text);
					}

					text = null;

				}
			}
			

//			for (File f2 : drawingFiles) {
//				System.out.println(f2.getAbsolutePath());
//				process(f2);
//			}

			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			drawingFiles.clear();
			reportFiles.clear();
			for(CvRect r : stamp1Rects){
				r.deallocate();
				r.setNull();
				r = null;
			}
			stamp1Rects.clear();
			for(CvRect r : stamp2Rects){
				r.deallocate();
				r.setNull();
				r = null;
			}
			CvRect.deallocateReferences();
			stamp2Rects.clear();
			storage1.release();
			storage2.release();
			try {
			
				unloadEngine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	}

	public static void process(File file, List<CvRect> stamp1Rects, List<CvRect> stamp2Rects) {
		
		int decision1 = OptionDetector.getOption(file.getAbsolutePath(), STAMP1, stamp1Rects);
		int decision2 = OptionDetector.getOption(file.getAbsolutePath(), STAMP2, stamp2Rects);
	
		List<CvRect> filteredRect = new ArrayList<CvRect>();
		List<CvRect> rects = null;
		BufferedImage im = null;
		IplImage src = null;
		CvMemStorage storage = CvMemStorage.create();
		try {

			im = ImageIO.read(file);

			src = cvLoadImage(file.getAbsolutePath(), CV_32F);
			rects = FindBoxes.findRectangles(src,storage);
		}catch(Exception ef){
			ef.printStackTrace();
		}
		finally{
			cvReleaseImage(src);

			src = null;
		}
		
		try{

			CvRect originBox = rects.get(0);
			CvRect originBox1 = rects.get(1);

			// System.out.println("no of rectangles1:"+rects.size());

			int maxX = originBox.x() + originBox.width();
			int maxY = originBox.y() + originBox.height();

			// System.out.println("origin:" + maxX + "," + maxY);

			// List<CvRect> rectangles = new ArrayList<CvRect>();
			// filteredRect.add(maxRect);
			// CvRect docRec = new CvRect();

			//
			// for (CvRect rectangle : rects) {
			// if (!(rectangle.height() < 60
			// || rectangle.width() < 60)) {
			// rectangles.add(rectangle);
			// }
			// }
			//
			// CvRect maxRect = new CvRect();
			// for (CvRect rectangle : rectangles) {
			// if ((rectangle.x()+rectangle.width() >
			// maxRect.x()+maxRect.width())
			// && (rectangle.y()+rectangle.height() >
			// maxRect.y()+maxRect.height())) {
			// maxRect = rectangle;
			// }
			// }
			// CvRect first = rects.get(0);
			// CvRect second = rects.get(1);
			// CvRect third = rects.get(2);
			//
			// System.out.println("first:"+first.x()+","+first.y()+","+(first.x()+first.width())+","+(first.y()+first.height()));
			// System.out.println("second:"+second.x()+","+second.y()+","+(second.x()+second.width())+","+(second.y()+second.height()));
			// System.out.println("third:"+third.x()+","+third.y()+(third.x()+third.width())+","+(third.y()+third.height()));
			//
			// int maxX = maxRect.x()+maxRect.width();
			// int maxY = maxRect.y()+maxRect.height();
			// System.out.println("maxX,Y:"+maxX+","+maxY);
			// System.out.println("maxRect:"+maxRect.x()+","+maxRect.y());
			// filteredRect.add(docRec);
			// rev1:-94,-84
			// dr1:-904,-84
			// title1:-904,-302

			// int revX = maxX-94;
			// int revY = maxY-84;
			// int drX = maxX-846;
			// int drY = maxY-84;
			// int titleX=maxX-846;
			// int titleY=maxY-302;

			int revX = maxX - 45;
			int revY = maxY - 45;
			int drX = maxX - 565;
			int drY = maxY - 45;
			int titleX = maxX - 512;
			int titleY = maxY - 185;

			CvRect revRect = new CvRect();
			CvRect drRect = new CvRect();
			CvRect titleRect = new CvRect();
			// CvRect dr1Rect = new CvRect();
			// CvRect dr2Rect = new CvRect();
			double minRevDiff = Double.MAX_VALUE;
			double minDrDiff = Double.MAX_VALUE;
			double minTitleDiff = Double.MAX_VALUE;
			// double minDr1Diff = Double.MAX_VALUE;
			// double minDr2Diff = Double.MAX_VALUE;
			// for (CvRect rectangle : rects) {
			// double diff = Math.abs(Math.hypot(revX-rectangle.x(),
			// revY-rectangle.y())) ;
			// if(diff < minRevDiff){
			// revRect = rectangle;
			// minRevDiff = diff;
			// }
			// diff = Math.abs(Math.hypot(drX-rectangle.x(),
			// drY-rectangle.y()));
			// if(diff < minDrDiff){
			// drRect = rectangle;
			// minDrDiff = diff;
			// }
			// diff = Math.abs(Math.hypot(titleX-rectangle.x(),
			// titleY-rectangle.y()));
			// if(diff < minTitleDiff){
			// titleRect = rectangle;
			// minTitleDiff = diff;
			// }
			// }

			for (CvRect rectangle : rects) {
//				if (rectangle.width() <= 37 || rectangle.height() <= 37) {
//					continue;
//				}
//				if(rectangle.x() < 1150 && rectangle.x() > 1100 && rectangle.y() > 3200 && rectangle.y() < 3250 ){
				
//				}

				if (revX >= rectangle.x()
						&& revX - rectangle.x() <= rectangle.width()
						&& revY >= rectangle.y()
						&& revY - rectangle.y() <= rectangle.height()) {
					double diff = Math.hypot(revX - rectangle.x(), revY
							- rectangle.y());
					if (diff < minRevDiff) {
						minRevDiff = diff;
						revRect = rectangle;
					}

				}

				if (drX >= rectangle.x()
						&& drX - rectangle.x() <= rectangle.width()
						&& drY >= rectangle.y()
						&& drY - rectangle.y() <= rectangle.height()) {
					double diff = Math.hypot(drX - rectangle.x(), drY
							- rectangle.y());
					if (diff < minDrDiff) {
						minDrDiff = diff;
						drRect = rectangle;
					}

				}

				if (titleX >= rectangle.x()
						&& titleX - rectangle.x() <= rectangle.width()
						&& titleY >= rectangle.y()
						&& titleY - rectangle.y() <= rectangle.height()) {
					double diff = Math.hypot(titleX - rectangle.x(), titleY
							- rectangle.y());
					if (diff < minTitleDiff) {
						minTitleDiff = diff;
						titleRect = rectangle;
					}

				}

			}

			filteredRect.add(revRect);
			filteredRect.add(drRect);
			filteredRect.add(titleRect);
			
//			 cvRectangleR(src, revRect,
//					 colors[0 % 6], 2, CV_AA, 0);
//			 cvRectangleR(src, drRect,
//					 colors[0 % 6], 2, CV_AA, 0);
//			 cvRectangleR(src, titleRect,
//					 colors[0 % 6], 2, CV_AA, 0);

//			int leftX = originBox1.x();
//			int leftY = originBox1.y() + originBox1.height();
//
//			int midPointX = leftX + 924;
//			int midPointY = leftY - 136;
//
//			int gap = 300;
//
//			int X1 = midPointX - gap;
//			int X2 = midPointX + gap;
//
//			int Y1 = midPointY - gap;
//			int Y2 = midPointY + gap;
//
//			for (CvRect r : rects) {
//				
//
//				int Xi = r.x();
//				int Xj = r.x() + r.width();
//
//				int Yi = r.y();
//				int Yj = r.y() + r.height();
//
//				if (((Xi >= X1 && Xi <= X2) || (Xj >= X1 && Xj <= X2))
//						&& ((Yi >= Y1 && Yi <= Y2) || (Yj >= Y1 && Yj <= Y2))) {
//
//					// r.x(r.x() - 4);
//					// r.y(r.y() - 4);
//					// r.height(r.height() + 8);
//					// r.width(r.width() + 8);
//					filteredRect.add(r);
////					if(r.x() < 1150 && r.x() > 1100 && r.y() > 3200 && r.y() < 3250 ){
////					 cvRectangleR(src, r,
////							 colors[0 % 6], 2, CV_AA, 0);
////					}
//
//				}
//				else{
////					 cvRectangleR(src, r,
////							 colors[3 % 6], 2, CV_AA, 0);
//				}
//			}

//			 OpenCVUtils.show(src, "Contours");

			// System.out.println("origin:" + maxX + "," + maxY);
			//
			// System.out.println("revRect:" + revRect.x() + "," + revRect.y()
			// + "," + revRect.width() + "," + revRect.height());
			// System.out.println("docRect:" + drRect.x() + "," + drRect.y() +
			// ","
			// + drRect.width() + "," + drRect.height());
			// System.out.println("titleRect:" + titleRect.x() + ","
			// + titleRect.y() + "," + titleRect.width() + ","
			// + titleRect.height());
			// System.out.println("doc1Rect:" + doc1Box.x() + "," + doc1Box.y()
			// + ","
			// + doc1Box.width() + "," + doc1Box.height());
			// System.out.println("doc2Rect:" + doc2Box.x() + "," + doc2Box.y()
			// + ","
			// + doc2Box.width() + "," + doc2Box.height());
			

			List<String> result = Run(file, filteredRect);
			int j = 0;
//			for (CvRect r : filteredRect) {
				// System.out.println("r:" + r.x() + "," + r.y() + ","
				// + (r.x() + r.width()) + "," + (r.y() + r.height())
				// + "---" + result.get(j++));

//			}

			// System.out.println("result:" + result);

//			String dr1 = "FMG DR. NO.";
//			String dr2 = "IMP DR. NO.";
//
//			CvRect dr1Rect = null, dr2Rect = null;
//			double minScore1 = 1.0, minScore2 = 1.0;
//			int index1 = -1, index2 = -1;
//			for (String str : result) {
//				double score = Bitap.match(str.replaceAll(" ", ""),
//						dr1.replaceAll(" ", ""));
//				if (score < minScore1) {
//					minScore1 = score;
//					index1 = result.indexOf(str);
//				}
//				score = Bitap.match(str.replaceAll(" ", ""),
//						dr2.replaceAll(" ", ""));
//				if (score < minScore2) {
//					minScore2 = score;
//					index2 = result.indexOf(str);
//				}
//			}
			// System.out.println("index1:"+index1);
			// System.out.println("index2:"+index2);

			String rev = result.get(0);
			//
			// System.out.println("revision:" + rev);
			//
			String dr = result.get(1);
			//
			// System.out.println("dr No:" + dr);
			//
			String title = result.get(2);
//			String doc1 = null;
//			String doc2 = null;
//			if (index1 != -1 && index2 != -1) {
//
//				dr1Rect = filteredRect.get(index1);
//				dr2Rect = filteredRect.get(index2);
//
//				// if(dr1Rect == null){
//				// System.out.println("dr1rect null");
//				// }else{
//				// System.out.println("dr1Rect:" + dr1Rect.x() + "," +
//				// dr1Rect.y()
//				// + "," + (dr1Rect.x() + dr1Rect.width()) + ","
//				// + (dr1Rect.y() + dr1Rect.height()));
//				// }
//				//
//				// if(dr2Rect == null){
//				// System.out.println("dr2rect null");
//				// }else{
//				//
//				// System.out.println("dr2Rect:" + dr2Rect.x() + "," +
//				// dr2Rect.y()
//				// + "," + (dr2Rect.x() + dr2Rect.width()) + ","
//				// + (dr2Rect.y() + dr2Rect.height()));
//				// }
//				int i = 0;
//				for (CvRect r : filteredRect) {
//					// System.out.println("r:" + r.x() + "," + r.y() + ","
//					// + (r.x() + r.width()) + "," + (r.y() + r.height())
//					// + "---" + result.get(i++));
//
//					if ((doc1 == null && dr1Rect != null)
//							&& (Math.abs(dr1Rect.x() + dr1Rect.width() - r.x()) < 15)
//							&& (r.x() > dr1Rect.x())
//							&& (Math.abs(r.y() - dr1Rect.y()) < 15)
//							&& (Math.abs(r.height() - dr1Rect.height()) < 15)) {
//						int index = filteredRect.indexOf(r);
//						// System.out.println("index1:"+index);
//						doc1 = result.get(index);
//					}
//
//					if ((doc2 == null && dr2Rect != null)
//							&& (Math.abs(dr2Rect.x() + dr2Rect.width() - r.x()) < 15)
//							&& (r.x() > dr2Rect.x())
//							&& (Math.abs(r.y() - dr2Rect.y()) < 15)
//							&& (Math.abs(r.height() - dr2Rect.height()) < 15)) {
//						int index = filteredRect.indexOf(r);
//						// System.out.println("index1:"+index);
//						doc2 = result.get(index);
//					}
//				}
//
//			}

			// System.out.println("Result:");
			//

			//
			// System.out.println("title:" + title);
			//
			// System.out.println("dr1 No:" + doc1);
			//
			// System.out.println("dr2 No:" + doc2);

//			out.write(file.getName() + "," + rev + "," + dr + "," + title + ","
//					+ doc1 + "," + doc2 + "\n");
			
		
//			
			out.write(file.getName() + "," + rev + "," + dr + ","  +decision1+ ","+decision2
					+ "\n");

			result.clear();

		} catch (Exception ex) {
			displayMessage(ex.getMessage());
			try {
				out.write(file.getName() + "," + "null" + "," + "," + "null"
						+ "," + "null" + "," + "null" + "," + "null" + "\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
//			if (rects != null) {
//				for (CvRect r : rects) {
//
//					r.deallocate();
//
//					r.setNull();
//					r = null;
//				}
//			}
//
//			if (filteredRect != null) {
//				for (CvRect r : filteredRect) {
//
//					r.deallocate();
//
//					r.setNull();
//					r = null;
//				}
//			}
			im.flush();
			im = null;
//			CvRect.deallocateReferences();
			
		
			rects.clear();
			filteredRect.clear();
			rects = null;
			filteredRect = null;
			storage.release();

			System.gc();
		}

	}

	public static List<String> Run(File file, List<CvRect> rects) {
		// Load ABBYY FineReader Engine
		List<String> data = null;
		// loadEngine();
		// setupFREngine();
		try {
			// Process with ABBYY FineReader Engine
			data = processImage(file, rects);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Unload ABBYY FineReader Engine
			// unloadEngine();
		}
		return data;
	}

	private static void loadEngine() {
		displayMessage("Initializing Engine...");
		engine = Engine.LoadEx(SamplesConfig.GetDllFolder(),
				SamplesConfig.GetDeveloperSN(), null, null, null, null);
	}

	private static List<String> processImage(File file, List<CvRect> rects) {
		List<String> data = new ArrayList<String>();
		IPrepareImageMode mode = engine.CreatePrepareImageMode();
//		mode.setAutoOverwriteResolution(false);
//		mode.setOverwriteResolution(true);
//		mode.setXResolutionToOverwrite(4960);
//		mode.setYResolutionToOverwrite(3506);
//		mode.setImageCompression(ImageCompressionEnum.IC_NoCompression);
		
		// mode.setCorrectSkewByHorizontalText(true);
		// mode.setUseFastBinarization(true);

		// IFRDocument document =
		// engine.CreateFRDocumentFromImage("C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title2.jpg",mode);
		IFRDocument document = engine.CreateFRDocumentFromImage(
				file.getAbsolutePath(),mode);
		
		
		
//		 System.out.println("height:"
//		 + document.getPages().Item(0).getLayout().getHeight());
//		 System.out.println("width:"
//		 + document.getPages().Item(0).getLayout().getWidth());
//		// System.out.println("text:" + document.getPlainText().getText());
//		 
//		 System.out.println("height:"
//				 + document.getPages().Item(0).getImageDocument().getSourceImageXResolution());
//				 System.out.println("width:"
//				 + document.getPages().Item(0).getImageDocument().getSourceImageYResolution());
//		
//				 System.out.println("height:"
//						 + document.getPages().Item(0).getImageDocument().getGrayImage().getHeight());
//						 System.out.println("width:"
//						 + document.getPages().Item(0).getImageDocument().getGrayImage().getWidth());
//
//						 System.out.println("height:"
//								 + document.getPages().Item(0).getImageDocument().getColorImage().getHeight());
//								 System.out.println("width:"
//								 + document.getPages().Item(0).getImageDocument().getColorImage().getWidth());
						
		RotationTypeEnum rotation = document.getPages().Item(0)
				.DetectOrientation(null, null, null).getRotationType();
		// System.out.println("rotation:" + rotation);
		if (rotation == RotationTypeEnum.RT_Clockwise) {
			document.getPages()
					.Item(0)
					.getImageDocument()
					.Transform(RotationTypeEnum.RT_Counterclockwise, false,
							false);
		} else if (rotation == RotationTypeEnum.RT_Counterclockwise) {
			document.getPages().Item(0).getImageDocument()
					.Transform(RotationTypeEnum.RT_Clockwise, false, false);
		} else if (rotation == RotationTypeEnum.RT_Upsidedown) {
			document.getPages().Item(0).getImageDocument()
					.Transform(RotationTypeEnum.RT_Upsidedown, false, false);
		}

		// document.getPages().Item(0).getImageDocument().SaveModified();

		rotation = document.getPages().Item(0).getImageDocument()
				.getImageRotation();
		// System.out.println("rotation:" + rotation);
		// System.out.println("text:" + document.getPlainText().getText());
		try {
			// Add image file to document
			displayMessage("Loading image...");
			// String imagePath =
			// "C:\\Users\\Rahu\\Downloads\\fwdfilesattached\\title1.png";
			// document.AddImageFile(imagePath, null, null);
			IPageProcessingParams params = engine.CreatePageProcessingParams();
			
			

			// IRecognizerParams rParams = engine.CreateRecognizerParams();
			// rParams.setFieldMarkingType(FieldMarkingTypeEnum.FMT_SimpleComb);
			//
			//
			// params.setRecognizerParams(rParams);
			

			ILayout layout1 = engine.CreateLayout();
			
			
			try {
				for (CvRect rectangle : rects) {
					IRegion pRegion = engine.CreateRegion();
					// left, top, right, bottom
					try {
						pRegion.AddRect(rectangle.x(), rectangle.y(),
								(rectangle.x() + rectangle.width()),
								(rectangle.y() + rectangle.height()));
						

//						 System.out.println("rectangle:" + rectangle.x() + ","
//						 + rectangle.y() + ","
//						 + (rectangle.x() + rectangle.width()) + ","
//						 + (rectangle.y() + rectangle.height()));

						IBlock newBlock = layout1.AddBlock(
								BlockTypeEnum.BT_Text, pRegion);
						
						ITextBlock textBlock = newBlock.GetAsTextBlock();
						// Specify the text type
						textBlock.getRecognizerParams().setTextType(
								TextTypeEnum.TT_ToBeDetected);
						// Specify the type of marking around the letters
						textBlock.getRecognizerParams().setFieldMarkingType(
								FieldMarkingTypeEnum.FMT_SimpleComb);
						// Specify the letters writing style
						textBlock.getRecognizerParams().setWritingStyle(
								WritingStyleEnum.WS_American);
						
					} catch (Exception ex) {
						System.out.println("exception:");
						ex.printStackTrace();
					

					} finally {
//						pRegion.Release();
						
					}
				}
				displayMessage("Process...");
				
				engine.RecognizePage(document.getPages().getElement(0)
						.getImageDocument(), null, null, layout1, null);

				ILayoutBlocks blocks = layout1.getBlocks();

				
				for (int j = 0; j < blocks.getCount(); j++) {
					IBlock block = blocks.Item(j);
					// System.out.println(block.getType());

					if (block.getType() == BlockTypeEnum.BT_Text) {
						ITextBlock textB = block.GetAsTextBlock();
						IText text = textB.getText();
						IParagraphs paras = text.getParagraphs();
						// System.out.println(paras.getCount());
						// System.out.println("paras:" +
						// paras.Item(0).getText());
						data.add(paras.Item(0).getText());
						block.Release();

						// if (paras.Item(0).getText().indexOf("REV") != -1) {
						//
						// String value = "";
						//
						// if (paras.getCount() == 1) {
						// value = paras
						// .Item(0)
						// .getText()
						// .substring(
						// paras.Item(0)
						// .getText()
						// .indexOf(
						// ' ',
						// paras.Item(0)
						// .getText()
						// .indexOf(
						// "REV")));
						// }
						//
						// for (int i = 1; i < paras.getCount(); i++) {
						// String t1 = paras.Item(i).getText();
						// System.out.println("text:" + t1);
						// value = value.concat(t1.trim());
						// }
						//
						// data.setRev(value);
						//
						// } //DWG
						// else if
						// (paras.Item(0).getText().indexOf("FMG DR. NO.")
						// != -1) {
						//
						// String value = "";
						//
						// if (paras.getCount() == 1) {
						// value = paras
						// .Item(0)
						// .getText()
						// .substring(
						// paras.Item(0)
						// .getText()
						// .indexOf(
						// ' ',
						// paras.Item(0)
						// .getText()
						// .indexOf(
						// "FMG DR. NO.")));
						// }
						//
						// for (int i = 1; i < paras.getCount(); i++) {
						// String t1 = paras.Item(i).getText();
						// System.out.println("text:" + t1);
						// value = value.concat(t1.trim());
						// }
						//
						// data.setDocNo1(value);
						//
						// } else if (paras.Item(0).getText()
						// .indexOf("DRAWING NUMBER") != -1) {
						//
						// String value = "";
						//
						// if (paras.getCount() == 1) {
						// value = paras
						// .Item(0)
						// .getText()
						// .substring(
						// paras.Item(0)
						// .getText()
						// .indexOf(
						// ' ',
						// paras.Item(0)
						// .getText()
						// .indexOf(
						// "DRAWING NUMBER")));
						// }
						//
						// for (int i = 1; i < paras.getCount(); i++) {
						// String t1 = paras.Item(i).getText();
						// System.out.println("text:" + t1);
						// value = value.concat(t1.trim());
						// }
						//
						// data.setDocNo1(value);
						//
						// }
						//
						// else if
						// (paras.Item(0).getText().indexOf("DRAWING NO") !=
						// -1) {
						//
						// String value = "";
						//
						// if (paras.getCount() == 1) {
						// value = paras
						// .Item(0)
						// .getText()
						// .substring(
						// paras.Item(0)
						// .getText()
						// .indexOf(
						// ' ',
						// paras.Item(0)
						// .getText()
						// .indexOf(
						// "DRAWING NO")));
						// }
						//
						// for (int i = 1; i < paras.getCount(); i++) {
						// String t1 = paras.Item(i).getText();
						// System.out.println("text:" + t1);
						// value = value.concat(t1.trim());
						// }
						//
						// data.setDocNo1(value);
						//
						// }
						//
					}
					// System.out.println();
				}
			} catch (Exception ex2) {
				System.out.println("ex2");
				ex2.printStackTrace();
				
				
			} finally {
				layout1.Release();
				
			}

			displayMessage("Saving results...");

		}
		catch(Exception ex3){
			System.out.println("ex3");
			ex3.printStackTrace();
			
		}
		finally {
			// Close document
//			document.Release();
			document.Close();

		}

		return data;
	}

	private static void setupFREngine() {
		displayMessage("Loading predefined profile...");
		if (!done) {
			engine.LoadPredefinedProfile("DocumentConversion_Accuracy");
			done = true;
		}
		// Possible profile names are:
		// "DocumentConversion_Accuracy", "DocumentConversion_Speed",
		// "DocumentArchiving_Accuracy", "DocumentArchiving_Speed",
		// "BookArchiving_Accuracy", "BookArchiving_Speed",
		// "TextExtraction_Accuracy", "TextExtraction_Speed",
		// "FieldLevelRecognition",
		// "BarcodeRecognition",
		// "Version9Compatibility",
		// "Default"
	}

	private static void unloadEngine() {
		displayMessage("Deinitializing Engine...");
		engine = null;
		System.gc();
		System.runFinalization();
		Engine.Unload();
	}

	public static void displayMessage(String message) {
		System.out.println(message);
	}

	private static IEngine engine = null;

}
