package com.tbitsglobal.ddc;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class ImageResizer {

	public static void main(String[] args) {
		try {
			File f = new File("/tmp/files/stamp.png");

			BufferedImage img = ImageIO.read(f);
			BufferedImage im = Scalr.resize(img,800,700);
			File f1 = new File("/tmp/files/newStamp.png");
			f1.createNewFile();
			ImageIO.write(im,"png",f1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

}
