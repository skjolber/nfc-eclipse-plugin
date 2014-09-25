package org.nfc.eclipse.plugin;

/***************************************************************************
 *
 * This file is part of the NFC Eclipse Plugin project at
 * http://code.google.com/p/nfc-eclipse-plugin/
 *
 * Copyright (C) 2012 by Thomas Rorvik Skjolberg / Antares Gruppen AS.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ****************************************************************************/



import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.binary.BinaryQRCodeReader;
import com.google.zxing.qrcode.binary.BinaryQRCodeWriter;

public class TestEncoderDecoder {

	@Test
	public void testRoundSingle() throws Exception {

		int awidth = 300;
		int aheight = 300;
		
		List<byte[]> content = new ArrayList<byte[]>();
		content.add(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07});

		byte[] generated = new byte[255];
		for(int i = 0; i < generated.length; i++) {
			generated[i] = (byte)i;
		}
		content.add(generated);
		
		for(int k = 0; k < content.size(); k++) {
			byte[] bytes = content.get(k);
			
			//get a byte matrix for the data
			BinaryQRCodeWriter writer = new BinaryQRCodeWriter();
			BitMatrix matrix = writer.encode(bytes, com.google.zxing.BarcodeFormat.QR_CODE, awidth, aheight);

			//generate an image from the byte matrix
			int width = matrix.getWidth(); 
			int height = matrix.getHeight(); 

			//create buffered image to draw to
			BufferedImage image = new BufferedImage(width * 2, height * 4, BufferedImage.TYPE_INT_RGB);
			image.getGraphics().setColor(Color.white);
			image.getGraphics().fillRect(0, 0, image.getWidth(), image.getHeight());
			//iterate through the matrix and draw the pixels to the image
			for (int y = 0; y < height; y++) { 
				for (int x = 0; x < width; x++) { 
					int grayValue = matrix.get(x, y) ? 0 : 0xff; 
					image.setRGB((image.getWidth() - width) / 2 + x, (image.getHeight() - height) / 2 + y, (grayValue == 0 ? 0 : 0xFFFFFF));
				}
			}

			//convert the image to a binary bitmap source
			LuminanceSource source = new BufferedImageLuminanceSource(image);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

			
			/*
			//write the image to the output stream
			File output = new File("./output/" + k + ".png");
			FileOutputStream fout = new FileOutputStream(output);
			ImageIO.write(image, "png", fout);
			fout.close();
			*/
			
			//decode the barcode
			BinaryQRCodeReader reader = new BinaryQRCodeReader();

			byte[] result = reader.decode(bitmap);
			
			for(int i = 0; i < bytes.length; i++) {
				if(bytes[i] != result[i]) {
					Assert.fail("Problem decoding " + i); 
				}
			}
			 
		}
	}

    /**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {
		StringBuilder sb = new StringBuilder();
		for(byte b: buffer)
			sb.append(String.format("%02x", b&0xff));
		return sb.toString();
    }
}
