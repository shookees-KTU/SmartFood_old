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

import com.github.sarxos.webcam.WebcamPanel;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import jade.wrapper.ControllerException;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author 
 */
public class GUI extends JFrame
{
    private static final long serialVersionUID = 1L;
    private final Logger logger = Logger.getLogger(GUI.class.getName());
    private JTabbedPane tabbedPane;
    private JComponent add_panel;
    private JComponent remPanel;
    private JComponent viePanel;
    private JComponent addPanel_controls;
    private JButton readBarcode_button;
    private JButton inputProduct_button;
    private SFTableModel model;
    private JTable table;
    private JTextField text;
    private JScrollPane scrollPane;
    private JButton inputProduct_add;
    private TableRowSorter<SFTableModel> sorter;
    private String table_data = "";
    
    private final Cam c;
    private final Reader r;
    private final Comm comm;
    
    public GUI(Comm comm) throws ControllerException
    {
        c = new Cam();
        r = new Reader();
        this.comm = comm;
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
        add_panel = makePanel();
        addPanel_controls = makePanel();
        addPanel_controls.setLayout(new BoxLayout(addPanel_controls, BoxLayout.PAGE_AXIS));
        
        //webcam read start and display
        readBarcode_button = new JButton("Read barcode");
        JLabel image_label = new JLabel(new ImageIcon());
        readBarcode_button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                Thread t;
                t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            enableAddPanelControls(false);
                            final WebcamPanel webcam_panel = c.getPanel(true, true);
                            
                            add_panel.add(webcam_panel);
                            pack();
                            
                            String barcode = retrieveBarcode();
                            add_panel.remove(webcam_panel);
                            
                            comm.addData("Product barcode", barcode);
                            enableAddPanelControls(true);
                            pack();
                        } catch (InterruptedException ex)
                        {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                };
                t.start();
            }
            
        });
        addPanel_controls.add(readBarcode_button);
        
        //input box and result table
        inputProduct_button = new JButton("Input product");
        inputProduct_button.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ae)
            {
                Thread t;
                t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        enableAddPanelControls(false);
                        //need to retrieve the list of used products
                        //plan - get serialized String[] and unserialize it.
                        //getProducts might be as well changedto getProducts
                        String[] products;
                        try
                        {
                            comm.getData("products");
                            //wait 10 seconds
                            int waitTime = 10;//seconds
        
                            //using the time comparisson method rather than Thread.sleep()
                            Calendar current_time = Calendar.getInstance();
                            current_time.setTime(new Date());
                            Calendar wait_time = Calendar.getInstance();
                            wait_time.setTime(new Date());
                            wait_time.add(Calendar.SECOND, waitTime);
                            while(table_data != "" && 
                                    current_time.get(Calendar.SECOND) !=
                                    wait_time.get(Calendar.SECOND))
                            {
                                //do nothing lol
                                //FIXME: repair the logic...
                            }
                        } catch (InterruptedException ex)
                        {
                            logger.log(Level.SEVERE, null, ex);
                        }
                        
                        //search/add field
                        text = new JTextField();
                        text.setPreferredSize(new Dimension(80, 20));
                        //live search on table (if there are any :) )
                        if (table_data != "" && getProducts(table_data).length != 0)
                        {
                            products = getProducts(table_data);
                            model = new SFTableModel();

                            sorter = new TableRowSorter<>(model);
                            table = new JTable(model);
                            table.setRowSorter(sorter);
                            table.setFillsViewportHeight(true);
                            //single selector
                            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            scrollPane = new JScrollPane(table);
                            //adding searcher for the table
                            text.getDocument().addDocumentListener(
                                    new DocumentListener()
                                    {

                                        @Override
                                        public void insertUpdate(DocumentEvent de)
                                        {
                                            filterData();
                                        }

                                        @Override
                                        public void removeUpdate(DocumentEvent de)
                                        {
                                            filterData();
                                        }

                                        @Override
                                        public void changedUpdate(DocumentEvent de)
                                        {
                                            filterData();
                                        }
                                    });
                        }
                        
                        //Add button
                        inputProduct_add = new JButton("Add");
                        inputProduct_add.addActionListener(new ActionListener()
                        {

                            @Override
                            public void actionPerformed(ActionEvent ae)
                            {
                                //should be some kind of definition binding to send data to Comm
                                if (scrollPane != null && table.getSelectedRow() != -1)
                                {
                                    try 
                                    {
                                        comm.addData("product",
                                          table.getValueAt(table.getSelectedColumn(), 0).toString());
                                        logger.log(
                                          Level.INFO, "Adding {0}", table.getValueAt(table.getSelectedColumn(), 0).toString());
                                    } catch (InterruptedException ex) 
                                    {
                                        logger.log(Level.SEVERE, null, ex);
                                    }
                                }else
                                {
                                    try
                                    {
                                        comm.addData("product", text.getText());
                                        logger.log(
                                          Level.INFO, "Adding {0}", text.getText());
                                    } catch (InterruptedException ex)
                                    {
                                        logger.log(Level.SEVERE, null, ex);
                                    }
                                }
                                enableAddPanelControls(true);
                                addPanel_controls.remove(text);
                                addPanel_controls.remove(inputProduct_add);
                                if (scrollPane != null)
                                {
                                    add_panel.remove(scrollPane);
                                }
                                pack();
                            }
                            
                        });
                        addPanel_controls.add(text);
                        addPanel_controls.add(inputProduct_add);
                        if (scrollPane != null)
                        {
                            add_panel.add(scrollPane);
                        }
                        //need to redefine the rightful choosing of an element
                        pack();
                    }
                };
                t.start();
            }
            
        });
        addPanel_controls.add(inputProduct_button);
        add_panel.add(addPanel_controls);
        tabbedPane.add("Add", add_panel);
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
    
    private void filterData()
    {
        try
        {
            RowFilter<SFTableModel, Object> rf = null;
            rf = RowFilter.regexFilter(text.getText(), 0);
            sorter.setRowFilter(rf);
        }catch(PatternSyntaxException ex)
        {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
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

    /**
     * Notifies the user with a notification about something
     * @param content notification text
     */
    public void notify(String content)
    {
        
    }
    
    /**
     * Retrieves serialized base64 string and decodes and 
     * de-serializes it into String array
     * @param serString
     * @return array of products as in strings
     */
    private String[] getProducts(String serString)
    {
        try
        {
            if ("".equals(serString))
            {
                return new String[0];
            }
            ByteArrayInputStream in;
            in = new ByteArrayInputStream(Base64.decode(serString));
            String[] ret = (String[]) new ObjectInputStream(in).readObject();
            return ret;
        } catch (IOException | ClassNotFoundException ex)
        {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private void enableAddPanelControls(boolean enabled)
    {
        inputProduct_button.setEnabled(enabled);
        readBarcode_button.setEnabled(enabled);
    }
    
    private String retrieveBarcode()
    {
        try
        {
            String barcode = r.readImage(c.takePicture());
            while (barcode.equals(""))
            {
                barcode = r.readImage(c.takePicture());
            }
            return barcode;
        } catch (IOException ex)
        {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Error while trying to take a picture");
        }
    }
    
    public void setTableData(String data)
    {
        table_data = data;
    }
}

class SFTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;
    private final String[] columNames = {"Product name"};

    private String[] data;
    @Override
    public int getRowCount()
    {
        return data.length;
    }

    @Override
    public int getColumnCount()
    {
        return columNames.length;
    }
    
    @Override
    public String getColumnName(int col) 
    {
        return columNames[col];
    }
    @Override
    public Object getValueAt(int i, int i1)
    {
        return data[i];
    }
    
    @Override
    public Class getColumnClass(int c)
    {
        return getValueAt(0, c).getClass();
    }
    
    public void setData(String[] data)
    {
        this.data = data;
    }
    
    public String[] getData()
    {
        return data;
    }
}