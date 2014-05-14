/*
 * The MIT License
 *
 * Copyright 2014 Paulius Šukys.
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package smartfood.mobile;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author Paulius Šukys
 */
public class Reader 
{
    public Reader(String file) throws FileNotFoundException, IOException
    {
        InputStream barCodeInputStream;
        barCodeInputStream = new FileInputStream(file);
        BufferedImage barCodeBufferedImage = ImageIO.read(barCodeInputStream);
        LuminanceSource source = new BufferedImageLuminanceSource(barCodeBufferedImage);
        MultiFormatReader barcodeReader = new MultiFormatReader();
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        String finalResult = "";
        try
        {
            result = barcodeReader.decode(bitmap);
            finalResult = String.valueOf(result.getText());
        }catch (Exception e)
        {
            System.out.println("IM BAD");
        }
        System.out.println(finalResult);
    }
}
