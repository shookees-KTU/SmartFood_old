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

import java.util.logging.Formatter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
/**
 * Custom log formatter
 * @author Paulius Šukys
 */
public class LogFormatter extends Formatter
{
    //called for every log records
    public String format(LogRecord rec)
    {
        StringBuffer buf = new StringBuffer(1000);
        buf.append("<tr>");
        buf.append("<td>");
        
        //if above warning -> bolded
        if (rec.getLevel().intValue() >= Level.WARNING.intValue())
        {
            buf.append("<b>");
            buf.append(rec.getLevel());
            buf.append("</b>");
        } else
        {
            buf.append(rec.getLevel());
        }
        buf.append("</td>");
        buf.append("<td>");
        buf.append(formatDate(rec.getMillis()));
        buf.append("<br />");
        buf.append(formatMessage(rec));
        buf.append("</td>");
        buf.append("</tr>");
        return buf.toString();
    }
    
    private String formatDate(long ms)
    {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(ms);
        return date_format.format(date);
    }
}
