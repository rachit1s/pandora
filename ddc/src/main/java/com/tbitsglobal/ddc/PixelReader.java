import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

public class PixelReader {

	public static final String IMG = "/home/rahul/files/file_12cdb342-764b-4a2b-b7be-94508d2095ad.png";

	public static int threshold = 239;
	
	public static int grayColor = 0xE0E0E0;

	static {
		System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	}

	public static void main(String[] args) {
		Date start = new Date();
		IIORegistry registry = IIORegistry.getDefaultInstance();
		registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi());
		registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi());

		BufferedImage img,img1;

		try {
			File f = new File(IMG);
			img1 = ImageIO.read(f);
			
			
			
			double scale = 0.25; 

			resize("/home/rahul/files/output2.png", img1, scale, scale);

			File f1 = new File("/home/rahul/files/output2.png");
			img = ImageIO.read(f1);
			int length = (int)(700*scale);
			int width = (int)(579*scale);
			

			int l1 = 0;
			int w1 = 0;

			System.out.println("height:" + img.getHeight());
			System.out.println("width:" + img.getWidth());

			int whitePixels = 0;
			int maxWhitePixels = 0;
			int linePixels[] = new int[width];
			for (int i = 0; i < img.getHeight() - length; i++) {
				int j = 0;
				for(int index =0;index<linePixels.length;index++){
					linePixels[index]=0;
				}
				whitePixels = 0;
				for (int k = i; k < length + i; k++) {
					for (int l = 0; l < width; l++) {
						if (isPixelWhite(img, k, l)) {
							whitePixels++;
							linePixels[l]++;

						}
					}
				}

				if (whitePixels > maxWhitePixels) {
					maxWhitePixels = whitePixels;
					l1 = i;
					w1 = j;
				}
				
				for (j = width; j < img.getWidth() - width; j++) {
					whitePixels -= linePixels[j % width];
					linePixels[j%width]=0;
					for (int p = i; p < length + i; p++) {
						if (isPixelWhite(img, p, j)) {
							linePixels[j % width]++;
							whitePixels++;
						}

					}
					if (whitePixels > maxWhitePixels) {
						maxWhitePixels = whitePixels;
						l1 = i;
						w1 = j-width;
					}
					
					// System.out.println("position:"+i+","+j);
				}
			}

			// w1=864;
			// l1=546;
			//
			File f2= new File("/home/rahul/files/newStamp.png");

			BufferedImage img2 = ImageIO.read(f2);
			
			
			l1/=scale;
			w1/=scale;
			int shade = 0xFFDEAD;
			System.out.println("position:" + l1 + "," + w1);
			for (int i = w1; i < w1 + width/scale; i++) {
				for (int j = l1; j < l1 + length/scale; j++) {
					if(isPixelWhite(img1,j,i)){
						img1.setRGB(i, j, img1.getRGB(i, j) & img2.getRGB(i-w1, j-l1));
					}
					else{
						img1.setRGB(i, j, img1.getRGB(i, j) & img2.getRGB(i-w1, j-l1));
					//	img1.setRGB(i, j, grayColor);
					}
					
				}
			}

			System.out.println("finished shading");

			File file = new File("/home/rahul/files/output3.png");
			file.createNewFile();
			ImageIO.write(img1, "png", file);

			System.out.println("finished");

		} catch (Exception e) {
			e.printStackTrace();
		}

		Date end = new Date();
		System.out.println("time in milis : "
				+ (end.getTime() - start.getTime()));
	}

	private static int[] getPixelData(BufferedImage img, int y, int x) {
		int argb = img.getRGB(x, y);

		int rgb[] = new int[] { (argb >> 16) & 0xff, // red
				(argb >> 8) & 0xff, // green
				(argb) & 0xff // blue
		};

		// System.out.println("rgb: " + rgb[0] + " " + rgb[1] + " " + rgb[2]);
		return rgb;
	}

	public static boolean isPixelWhite(BufferedImage img, int y, int x) {
		int rgb[] = null;
		try{
		int argb = img.getRGB(x, y);

		rgb = new int[] { (argb >> 16) & 0xff, // red
				(argb >> 8) & 0xff, // green
				(argb) & 0xff // blue
		};

		for (int i = 0; i < rgb.length; i++) {
//			System.out.println(rgb[i]);
			if (rgb[i] < threshold) {
				return false;
			}
		}
		}catch(Exception e){
			System.out.println("Problems with x,y:"+x+","+y +" for image with dimensions:"+img.getWidth()+"*"+img.getHeight());
			e.printStackTrace();
		}
		finally{
			
			rgb = null;
			
		}

		// System.out.println("rgb: " + rgb[0] + " " + rgb[1] + " " + rgb[2]);
		return true;
	}

	public static void resize(String filename, RenderedImage image,
			double wScale, double hScale) {
		// now resize the image
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image); // The source image
		pb.add(wScale); // The xScale
		pb.add(hScale); // The yScale
		pb.add(0.0F); // The x translation
		pb.add(0.0F); // The y translation

		RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		RenderedOp resizedImage = JAI.create("SubsampleAverage", pb, hints);

		// lastly, write the newly-resized image to an
		// output stream, in a specific encoding
		try {
			FileOutputStream fos = new FileOutputStream(new File(filename));
			JAI.create("encode", resizedImage, fos, "png", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
