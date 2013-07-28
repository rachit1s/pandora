import static com.googlecode.javacv.cpp.opencv_core.cvReleaseMemStorage;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;


public class OptionDetector {
	
//	public static final String IMG = "/home/rahul/files/tilted.png";
//	
//	public static final String IMG1 = "/home/rahul/files/final.png";
	
	public static int getOption(String IMG, String STAMP, List<CvRect> rect){
		
		Point p = SubImageLocator.surf(IMG, STAMP);
		
		
//		List<Integer> pixels = new ArrayList<Integer>();
		int option = 0;
		int maxOption = 0;
		BufferedImage img = null;
		try{
			File f1 = new File(IMG);
			img = ImageIO.read(f1);
			int maxCount = 0;
			for(CvRect rect0 : rect){
				option++;
				int count = 0;
				for(int i=0;i<rect0.width();i++){
					for(int j=0;j<rect0.height();j++){
						if(!PixelReader.isPixelWhite(img,(int)p.getY()+rect0.y()+j,(int)p.getX()+rect0.x()+i)){
//							if(PixelReader.grayColor != img.getRGB((int)p.getX()+rect0.x()+i, (int)p.getY()+rect0.y()+j) ){
								count++;
//							}
						}
					}
				}
				
				if(maxCount < count){
//					System.out.println("no:"+count);
					maxCount = count;
					maxOption = option;
				}
//				System.out.println(count);
//				pixels.add(count);
			}
			
//			System.out.println("option:"+maxOption);
			
			
			
//			System.out.println("Time:"+(endTime.getTime() - startTime.getTime()));
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(img != null){
				img.flush();
				img = null;
				System.gc();
			}
			
		}
		return maxOption;
	}
	
	public static void getOptionInTiltedImage(String IMG, String STAMP){
		Date startTime = new Date();
		Point p = SubImageLocator.surf(IMG,STAMP);
		CvMemStorage storage = CvMemStorage.create();
		
		List<CvRect> rect = FindRectangles.findRectangles(STAMP,storage);
		List<Integer> pixels = new ArrayList<Integer>();
		int option = 0;
		int maxOption = 0;
		try{
			File f1 = new File(IMG);
			BufferedImage img = ImageIO.read(f1);
			int maxCount = 0;
			for(CvRect rect0 : rect){
				option++;
				int count = 0;
				double a = p.getX();
				double b = p.getY();
				
				for(int i=0;i<rect0.height();i++){
					for(int j=0;j<rect0.width();j++){
						double x = rect0.x() + j;
						double y = rect0.y() + i;
						double m = 0.08735906539431659;
						double t = Math.atan2(y, x);
						double theta = Math.PI - (m+t);
						int x1 = (int)(a - (Math.sqrt(x*x + y*y)*Math.cos(theta)));
						int y1 = (int)(b + (Math.sqrt(x*x + y*y)*Math.sin(theta)));
						
						//System.out.println("x1,y1:"+x1+","+y1);
						//System.out.print(img.getRGB(x1, y1)+",");
						
						if(!PixelReader.isPixelWhite(img, y1, x1)){
				//			if(PixelReader.grayColor != img.getRGB(x1, y1) ){
							System.out.print(img.getRGB(x1,y1)+",");
							img.setRGB(x1,y1,	0xffd700);	
							
								count++;
					//		}
						}
					//	
						//
					}
				}
				if(maxCount <= count){
					maxCount = count;
					maxOption = option;
				}
				System.out.println();
				System.out.println("option:"+option+",count:"+count);
				pixels.add(count);
			}
			

			File file = new File("/home/rahul/files/finalImg.png");
			file.createNewFile();
			ImageIO.write(img, "png", file);

			System.out.println("option:"+maxOption);
			Date endTime = new Date();
			
			System.out.println("Time:"+(endTime.getTime() - startTime.getTime()));
			cvReleaseMemStorage(storage);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String IMG = "D:\\DTN Zipped File\\DTN Zipped File\\600MP_DTN\\FMG-EXT-600-7335\\14-3-600MP0053-02012-DR-ME-0005_A_21.pdf";

		String STAMP = "D:\\AECOM.png";

//		System.out.println(getOption(IMG,STAMP));
	}

}
