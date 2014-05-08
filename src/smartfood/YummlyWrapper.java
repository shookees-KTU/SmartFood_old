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

package smartfood;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A wrapper for Yummly
 * @author Paulius Šukys
 */
public class YummlyWrapper 
{
    //singleton since only one connection is needed
    private static YummlyWrapper instance = null;
    
    private static final String API_URL = "https://api.yummly.com/v1";
    
    private YummlyWrapper()
    {
        //exists to defeat instantiation
    }
    
    public static YummlyWrapper getInstance()
    {
        if (instance == null)
        {
            setup();
            instance = new YummlyWrapper();
        }
        return instance;
    }
    
    protected static void setup()
    {
        try
        {
            URL url = new URL(API_URL);
            
        }catch(MalformedURLException exc)
        {
            
        }
    }
}
