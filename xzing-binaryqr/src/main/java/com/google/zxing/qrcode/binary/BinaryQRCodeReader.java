package com.google.zxing.qrcode.binary;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.qrcode.decoder.Decoder;
import com.google.zxing.qrcode.detector.Detector;

/**
 * This implementation can detect and decode QR Codes in an image into bytes.
 *
 * @author Thomas Skjolberg
 */
public class BinaryQRCodeReader{

  private final Decoder decoder = new Decoder();

  protected Decoder getDecoder() {
    return decoder;
  }

  /**
   * Locates and decodes a QR code in an image.
   *
   * @return a String representing the content encoded by the QR code
   * @throws NotFoundException if a QR code cannot be found
   * @throws FormatException if a QR code cannot be decoded
   * @throws ChecksumException if error correction fails
   */
  public byte[] decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
      DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect();

      DecoderResult decode = decoder.decode(detectorResult.getBits());
      
      return decode.getRawBytes();
  }

  public void reset() {
    // do nothing
  }


}
