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

public class Orientation {

	public static final String IMG = "D:/file20.png";

	public static int threshold = 245;

	public static int grayColor = 0xE0E0E0;

	static {
		System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	}

	public static void main(String[] args) {
		Date start = new Date();
		IIORegistry registry = IIORegistry.getDefaultInstance();
		registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi());
		registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi());

		BufferedImage img, img1;

		try {
			File f = new File(IMG);

			img = ImageIO.read(f);

			System.out.println("height:" + img.getHeight());
			System.out.println("width:" + img.getWidth());

			int x1 = 0;
			int y1 = 0;
			boolean found = false;
			for (int i = 0; i < img.getWidth(); i++) {
				for (int j = 0; j < img.getHeight(); j++) {
					if (!isPixelWhite(img, j, i)) {
						x1 = i;
						y1 = j;
						found = true;
						break;
					}

				}
				if (found) {
					break;
				}

			}

			int x2 = 0;
			int y2 = 0;
			found = false;
			for (int j = 0; j < img.getHeight(); j++) {
				for (int i = 0; i < img.getWidth(); i++) {

					if (!isPixelWhite(img, j, i)) {
						x2 = i;
						y2 = j;
						found = true;
						break;
					}

				}
				if (found) {
					break;
				}

			}

			int x3 = 0;
			int y3 = 0;
			found = false;
			for (int i = img.getWidth() - 1; i >= 0; i--) {
				for (int j = img.getHeight() - 1; j >= 0; j--) {

					if (!isPixelWhite(img, j, i)) {
						x3 = i;
						y3 = j;
						found = true;
						break;
					}

				}
				if (found) {
					break;
				}

			}

			int x4 = 0;
			int y4 = 0;
			found = false;
			for (int j = img.getHeight() - 1; j >= 0; j--) {
				for (int i = img.getWidth() - 1; i >= 0; i--) {

					if (!isPixelWhite(img, j, i)) {
						x4 = i;
						y4 = j;
						found = true;
						break;
					}

				}
				if (found) {
					break;
				}

			}

			System.out.println("finished");
			System.out.println("x1,y1:" + x1 + "," + y1);
			System.out.println("x2,y2:" + x2 + "," + y2);
			System.out.println("x3,y3:" + x3 + "," + y3);
			System.out.println("x4,y4:" + x4 + "," + y4);
			double m = Math.atan2(Math.abs(y1 - y4), Math.abs(x1 - x4));
			System.out.println(m);
			System.out.println((m * 180 / Math.PI));
			
			double m1 = Math.atan2(Math.abs(y2 - y3), Math.abs(x2 - x3));
			System.out.println(m1);
			System.out.println((m1 * 180 / Math.PI));
			
			rotateImage(m1);

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
		int argb = img.getRGB(x, y);

		int rgb[] = new int[] { (argb >> 16) & 0xff, // red
				(argb >> 8) & 0xff, // green
				(argb) & 0xff // blue
		};

		for (int i = 0; i < rgb.length; i++) {
			if (rgb[i] < threshold) {
				return false;
			}
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

	

	public static void rotateImage(double m) {

		BufferedImage img;

		try {
			File f = new File(IMG);
			
			img = ImageIO.read(f);

			ParameterBlock pb = new ParameterBlock();
			pb.addSource(img);

			pb.add(new java.lang.Float(0)); // x-origin
			pb.add(new java.lang.Float(0)); // y-origin
			pb.add(new java.lang.Float(-m)); // rotation amount

			RenderedOp renderedOp = JAI.create("rotate", pb);
			
			// image = renderedOp.getRendering().getAsBufferedImage();
			//
			// Panel.setImage( image );

			FileOutputStream fos = new FileOutputStream(new File(
					"D:/file23.png"));
			JAI.create("encode", renderedOp, fos, "png", null);
		} catch (Exception e) {

		}
	}

}
