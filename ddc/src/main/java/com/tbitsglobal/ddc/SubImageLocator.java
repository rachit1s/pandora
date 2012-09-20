package com.tbitsglobal.ddc;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.SurfCompare;

public class SubImageLocator {

	public static final String IMG = "/home/rahul/files/tilted.png";

	public static final String STAMP = "/home/rahul/files/newStamp.png";

	public static int threshold = 245;

	static {
		System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	}

	public static void main(String[] args) {
		Date startTime = new Date();
		surf();
		Date endTime = new Date();
		System.out.println("Time:"+(endTime.getTime()-startTime.getTime()));
	}

	public static Point surf() {
		Point p = new Point();
		try {
			
			File f1 = new File(IMG);
			BufferedImage img1 = ImageIO.read(f1);
			
			File f2 = new File(STAMP);
			BufferedImage img2 = ImageIO.read(f2);
			
			SurfCompare comp = new SurfCompare(img1,img2);
			
			
			
			Map<SURFInterestPoint, SURFInterestPoint> map= comp.getMBMatchingPoints();
			System.out.println(comp.getMImageAXScale());
			System.out.println(comp.getMImageAYScale());
			
			Set<SURFInterestPoint> keys = map.keySet();
			System.out.println(keys.size());
			int minX = Integer.MAX_VALUE,minY=Integer.MAX_VALUE;
			int oX=0,oY=0;
			for(SURFInterestPoint point:keys){
				SURFInterestPoint target = comp.getMBMatchingPoints().get(point);
//				int x = (int)(comp.getMImageAXScale() * target.getX());
//		    	int y = (int)(comp.getMImageAYScale() * target.getY());
				
				int x = (int)(target.getX());
		    	int y = (int)(target.getY());
				
				if(x<minX && y < minY){
					oX = (int)point.getX();
					oY = (int)point.getY();
					minX = x;
					minY = y;
				}
			}
			System.out.println(minX);
			System.out.println(minY);
			System.out.println(oX);
			System.out.println(oY);
			System.out.println(minX-oX);
			System.out.println(minY-oY);
			double m = 0.08735906539431659;
			double t = Math.atan2(oY, oX);
			double theta = Math.PI - (m+t);
			double dist = Math.sqrt((oX*oX + oY*oY));
			
			int x = (int)(minX + dist * Math.cos(theta));
			int y = (int)(minY - dist * Math.sin(theta));
			
			System.out.println("x,y"+x+","+y);
			
			System.out.println("oX,oY"+oX+","+oY);
			p.setLocation(x, y);
			
			
				for(int j=-10;j<10;j++){
					img1.setRGB(x, y+j, threshold);
				}
				for(int i=-10;i<10;i++){
					
				}
			
			File file = new File("/home/rahul/files/detector.png");
			file.createNewFile();
			ImageIO.write(img1, "png", file);

			//comp.display();
			
//			for (int i=0;i<img2.getHeight();i++){
//				for(int j=0;j<img2.getWidth();j++){
//					if(img1.getRGB(minX-oX+j, minY-oY+i) != img2.getRGB(j, i)){
//						System.out.print("("+(minX-oX+j)+","+(minY-oY+i)+") ,");
//						img2.setRGB(j, i, 0);
//						
//					}
//				}
//			}
			
			
			
//			double minSAD = 0;
//			double bestRow = 0;
//			double bestCol = 0;
//			double bestSAD = Double.MAX_VALUE;
//			for (int x = -50; x < 50; x++) {
//				for (int y = -50; y < 50; y++) {
//					double SAD = 0.0;
//
//					// loop through the template image
//					for (int i = 0; i < img2.getHeight(); i++) {
//						for (int j = 0; j < img2.getWidth(); j++) {
//
//							// pixel p_SearchIMG = S[x+i][y+j];
//							//
//							// pixel p_TemplateIMG = T[i][j];
//							System.out.println("x,y,i,j" + (x+minX+j) + "," + (y+minY+i) + ","
//									+ i + "," + j);
//							SAD += (img1.getRGB(minX+x + j, minY+y + i) - img2.getRGB(j,
//									i));
//						}
//					}
//
//					// save the best found position
//					if (minSAD > SAD) {
//						minSAD = SAD;
//						// give me VALUE_MAX
//						bestRow = x+minX;
//						bestCol = y+minY;
//						bestSAD = SAD;
//					}
//				}
//			}
//			System.out.println("BestRow,BestCol:" + bestRow + "," + bestCol
//					);
//			int shade = 0xFFDEAD;
//			for(int i=-10;i<10;i++)
//				for(int j=-10;j<10;j++)
//					img1.setRGB(minX+i,minY+j,shade);
//			
			//comp.display();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	public static void simpleMathod() {
		Date start = new Date();
		IIORegistry registry = IIORegistry.getDefaultInstance();
		registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi());
		registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi());

		BufferedImage img3, img4;

		int bestRow = 0;
		int bestCol = 0;
		double bestSAD = 0.0;
		double scale = 0.03125;

		try {
			File f3 = new File(IMG);
			img3 = ImageIO.read(f3);

			File f4 = new File(STAMP);
			img4 = ImageIO.read(f4);

			double minSAD = Double.MAX_VALUE;

			PixelReader
					.resize("/tmp/files/sampleImage.png", img3, scale, scale);
			File f1 = new File("/tmp/files/sampleImage.png");
			BufferedImage img1 = ImageIO.read(f1);

			PixelReader
					.resize("/tmp/files/sampleStamp.png", img4, scale, scale);
			File f2 = new File("/tmp/files/sampleStamp.png");
			BufferedImage img2 = ImageIO.read(f2);

			// loop through the search image
			for (int x = 0; x < img1.getHeight() - img2.getHeight(); x++) {
				for (int y = 0; y < img1.getWidth() - img2.getWidth(); y++) {
					double SAD = 0.0;

					// loop through the template image
					for (int i = 0; i < img2.getHeight(); i++) {
						for (int j = 0; j < img2.getWidth(); j++) {

							// pixel p_SearchIMG = S[x+i][y+j];
							//
							// pixel p_TemplateIMG = T[i][j];
							System.out.println("x,y,i,j" + x + "," + y + ","
									+ i + "," + j);
							SAD += (img1.getRGB(y + j, x + i) - img2.getRGB(j,
									i));
						}
					}

					// save the best found position
					if (minSAD > SAD) {
						minSAD = SAD;
						// give me VALUE_MAX
						bestRow = x;
						bestCol = y;
						bestSAD = SAD;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("BestRow,BestCol:" + bestRow / scale + "," + bestCol
				/ scale);
		System.out.println("bestSAD:" + bestSAD);
		Date end = new Date();
		System.out.println("time in milis : "
				+ (end.getTime() - start.getTime()));
	}

}
