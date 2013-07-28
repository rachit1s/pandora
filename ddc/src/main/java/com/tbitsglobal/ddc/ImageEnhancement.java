import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.DFTDescriptor;


public class ImageEnhancement {
	static
	{
	System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	}
	
	private static void saveGridImage(File output) throws IOException {
	    //output.delete();
		BufferedImage gridImage = ImageIO.read(output);

	    final String formatName = "png";

	    for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
	       ImageWriter writer = iw.next();
	       ImageWriteParam writeParam = writer.getDefaultWriteParam();
	       ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
	       IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
	       if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
	          continue;
	       }

	       setDPI(metadata);

	       final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
	       try {
	          writer.setOutput(stream);
	          writer.write(metadata, new IIOImage(gridImage, null, metadata), writeParam);
	       } finally {
	          stream.close();
	       }
	       break;
	    }
	 }

	 private static void setDPI(IIOMetadata metadata) throws IIOInvalidTreeException {

		 double DPI = 350;
		 double INCH_2_CM = 2.54;
	    // for PMG, it's dots per millimeter
	    double dotsPerMilli = 1.0 * DPI / 10 / INCH_2_CM;

	    IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
	    horiz.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
	    vert.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
	    dim.appendChild(horiz);
	    dim.appendChild(vert);

	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
	    root.appendChild(dim);

	    metadata.mergeTree("javax_imageio_1.0", root);
	 }
	 
	 public static void main(String[] args) {
		File f = new File("/home/rahul/files/file_12cdb342-764b-4a2b-b7be-94508d2095ad.png");
//		try{
//			saveGridImage(f);
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//		sharp();
		try{
		BufferedImage img = ImageIO.read(f);
		resize("/home/rahul/files/file1.png",img,2,2);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	 
	 public static void sharp() {

			try {
				File f = new File("/home/rahul/files/file_12cdb342-764b-4a2b-b7be-94508d2095ad.png");
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

				File file = new File("/home/rahul/files/first.png");
				file.createNewFile();
				ImageIO.write(img, "png", file);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	 
	 public static void resize(String filename, RenderedImage image,
				int wScale, int hScale) {
			// now resize the image
			ParameterBlock pb = new ParameterBlock();
			pb.addSource(image); // The source image
//			pb.add(wScale); // The xScale
//			pb.add(hScale); // The yScale
//			pb.add(0.0F); // The x translation
//			pb.add(0.0F); // The y translation
			pb.add(DFTDescriptor.SCALING_NONE);
			pb.add(DFTDescriptor.REAL_TO_COMPLEX);

			RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			RenderedOp resizedImage = JAI.create("DFT", pb, null);

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
