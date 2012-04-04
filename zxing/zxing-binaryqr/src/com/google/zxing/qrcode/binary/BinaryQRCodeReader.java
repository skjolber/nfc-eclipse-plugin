package com.google.zxing.qrcode.binary;
/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.qrcode.binary.decoder.Decoder;
import com.google.zxing.qrcode.detector.Detector;

/**
 * This implementation can detect and decode QR Codes in an image.
 *
 * @author Sean Owen
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

      return decoder.decode(detectorResult.getBits());
  }

  public void reset() {
    // do nothing
  }


}
