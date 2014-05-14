/*
 * The MIT License
 *
 * Copyright 2014 shookees.
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

import com.github.sarxos.webcam.WebcamPanel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author 
 */
public class GUI extends JFrame
{
    private static final long serialVersionUID = 1L;
    private JTabbedPane tabbedPane;
    private JComponent addPanel;
    private JComponent remPanel;
    private JComponent viePanel;
    private final Cam c = new Cam();
    private final Reader r = new Reader();
    public GUI()
    {
        setName("SmartFood");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 1));
        setMinimumSize(new Dimension(480, 320));
        initComponents();
        pack();
        setVisible(true);
    }
    
    /**
     * Initiates the GUI components
     */
    private void initComponents()
    {
        tabbedPane = new JTabbedPane();
        //add by webcam photo or text
        addPanel = makePanel();
        //webcam read start and display
        JButton readBarcode_button = new JButton("Read barcode");
        JLabel image_label = new JLabel(new ImageIcon());
        readBarcode_button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                Thread t = new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            String barcode = r.readImage(c.takePicture());
                            while (barcode.equals(""))
                            {
                                barcode = r.readImage(c.takePicture());
                            }
                            Logger.getLogger(GUI.class.getName()).info("Barcode: " + barcode);
                        } catch (IOException ex)
                        {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                t.start();
            }
            
        });
        addPanel.add(readBarcode_button);
        final WebcamPanel panel = c.getPanel(true, true);
        addPanel.add(panel);
        tabbedPane.add("Add", addPanel);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        
        remPanel = makePanel();
        tabbedPane.add("Remove", remPanel);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        
        viePanel = makePanel();
        tabbedPane.add("View products", viePanel);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
        
        add(tabbedPane);
        //allows scrolling tabs
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }
    
    /**
     * Creates a panel with label
     * @return panel
     */
    private JComponent makePanel()
    {
        JPanel panel = new JPanel(false);
        return panel;
    }
}
