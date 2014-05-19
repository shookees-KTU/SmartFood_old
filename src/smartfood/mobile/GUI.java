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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;
import org.json.simple.JSONObject;

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
    private JComponent remove_panel;
    private JComponent view_panel;
    private JComponent add_panel_ctrl;
    private JButton readBarcode_button;
    private JButton inputProduct_button;
    private JTextField product_text;
    private JTextField barcode_text;
    private JScrollPane scrollPane;
    private TableRowSorter<SFTableModel> sorter;
    private JTable table;
    private JButton submit_button;
    private JDatePickerImpl date_picker;
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
    
    private JComponent initControlPanel(String tab)
    {
        JComponent panel = makePanel();
        switch(tab)
        {
            case "add":
                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                
                readBarcode_button = getBarcodeButton(tab);
                inputProduct_button = getInputProdButton(tab);
                panel.add(readBarcode_button);
                panel.add(inputProduct_button);
                break;
            case "remove":
                
                break;
            case "view":
                
                break;
        }
        return panel;
    }
    
    /**
     * General method for getting active barcode reader button which inhibits
     * webcam panel, reads the barcode, removes the panel and sends the data to server
     * @param tab - tab name in which the button will be added
     * @return "Read barcode" button
     */
    private JButton getBarcodeButton(final String tab)
    {
        JButton button = new JButton("Read barcode");
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               Thread t = new Thread()
               {
                   @Override
                   public void run()
                   {
                       try
                       {
                            enablePanelControls(tab, false);
                            final WebcamPanel webcam_panel = c.getPanel(true, true);
                            
                            add_panel.add(webcam_panel);
                            pack();
                            
                            //try to get the barcode
                            String barcode = retrieveBarcode();
                            JSONObject data = new JSONObject();
                            data.put("barcode", barcode);
                            switch(tab)
                            {
                                case "add":
                                    comm.addData(data.toString());
                                    break;
                                case "remove":
                                    comm.removeData(data.toString());
                                    break;
                                default:
                                    logger.log(Level.WARNING, "Unknown tab name!");
                            }
                            //get pack to previous state
                            add_panel.remove(webcam_panel);
                            enablePanelControls(tab, true);
                            pack();
                       }catch (InterruptedException exc)
                       {
                           logger.log(Level.WARNING, "Thread has been interrupted!");
                       }
                   }
               };
               t.start();
            }
        });
        return button;
    }
    
    /**
     * Return input product button
     * @param tab
     * @return 
     */
    private JButton getInputProdButton(final String tab)
    {
        JButton button = new JButton("Input product");
        button.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            enablePanelControls(tab, false);
                            /*ask for asynchronous data retrieval. When retrieved
                             *the class global "table_data" will be changed*/
                            comm.getData("products");
                            
                            waitForDataRetrieval(10);
                            
                            product_text = getTextField("Product name");
                            product_text.getDocument().addDocumentListener(getDataTableDocumentListener());
                            date_picker = getDatePicker();
                            barcode_text = getTextField("Barcode");
                            barcode_text.getDocument().addDocumentListener(getDataTableDocumentListener());
                            //submit button for control panel
                            submit_button = getSubmitButton(tab);
                            
                            //adding to control panel
                            add_panel_ctrl.add(product_text);
                            add_panel_ctrl.add(barcode_text);
                            add_panel_ctrl.add(date_picker);
                            add_panel_ctrl.add(submit_button);
                            
                            
                            if (table_data != "" && getProducts(table_data).length != 0)
                            {
                                String[][] products = getProducts(table_data);
                                scrollPane = getDataTableSP(products);
                                add_panel.add(scrollPane);
                            }
                            pack();
                        }catch(InterruptedException exc)
                        {
                            logger.log(Level.WARNING, "Thread has been interrupted");
                        }
                    }
                };
                t.start();
            }
            
        });
        return button;
    }
    
    /**
     * Gets a text field with a placholder
     * @param placeholder text to be put in placholder
     * @return text field object
     */
    private JTextField getTextField(String placeholder)
    {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(80, 20));
        TextPrompt tp = new TextPrompt(placeholder, tf);
        return tf;
    }
    
    /**
     * Gets the date picker implementation
     * @return date picker
     */
    private JDatePickerImpl getDatePicker()
    {
        return new JDatePickerImpl(new JDatePanelImpl(new UtilDateModel()));
    }
    
    /**
     * Waits for table_data variable to be populated
     * @param waitSeconds how much seconds to wait
     */
    private void waitForDataRetrieval(int waitSeconds)
    {
        Calendar current_time = Calendar.getInstance();
        current_time.setTime(new Date());
        Calendar wait_time = Calendar.getInstance();
        wait_time.setTime(new Date());
        wait_time.add(Calendar.SECOND, waitSeconds);
        while(table_data.equals("") &&
                current_time.get(Calendar.SECOND) !=
                wait_time.get(Calendar.SECOND))
        {}
            
    }
    
    /**
     * Creates a sortable table model with populated data
     * @param tableData data to populate
     * @return scroll pane  for the table model
     */
    private JScrollPane getDataTableSP(String[][] tableData)
    {
        SFTableModel model = new SFTableModel();
        model.setData(tableData);
        //might need to remove this global state, since several tables with sorters will be available
        sorter = new TableRowSorter<>(model);
        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane pane = new JScrollPane(table);
        return pane;
    }
    
    /**
     * Creates a simple data table document listener which updates on any action
     * @return the document listener
     */
    private DocumentListener getDataTableDocumentListener()
    {
        DocumentListener dl = new DocumentListener()
        {

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                filterData();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                filterData();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                filterData();
            }   
        };
        return dl;
    }
    
    /**
     * Returns a tab specific submit button
     * @param tab tab name
     * @return button for submission
     */
    private JButton getSubmitButton(final String tab)
    {
        JButton button = new JButton("Submit");
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    if (scrollPane != null && table.getSelectedRow() != -1)
                    {
                        JSONObject data = new JSONObject();
                        data.put("product", table.getValueAt(table.getSelectedRow(), 0).toString().trim());
                        data.put("barcode", table.getValueAt(table.getSelectedRow(), 1).toString().trim());
                        data.put("expiry", table.getValueAt(table.getSelectedRow(), 2).toString().trim());
                        comm.addData(data.toString());
                        logger.log(Level.INFO, "Adding {0}", 
                                table.getValueAt(table.getSelectedColumn(), 0).toString());
                    }else
                    {
                        JSONObject data = new JSONObject();
                        data.put("product", product_text.getText().trim());
                        data.put("barcode", barcode_text.getText().trim());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        data.put("expiry", sdf.format(date_picker.getModel().getValue()));
                        comm.addData(data.toString().trim());
                        logger.log(Level.INFO, 
                                "Adding {0}", product_text.getText());
                    }
                    enablePanelControls(tab, true);
                    //remove controls and scrollpane
                    removeControls(tab);
                    //refresh data
                    comm.getData("products");
                }catch(InterruptedException exc)
                {
                    logger.log(Level.WARNING, "Thread has been interrupted");
                }
            }
        });
        return button;
    }
    
    private void removeControls(String tab)
    {
        switch(tab)
        {
            case "add":
                add_panel_ctrl.remove(product_text);
                add_panel_ctrl.remove(barcode_text);
                add_panel_ctrl.remove(date_picker);
                add_panel_ctrl.remove(submit_button);
                if (scrollPane != null)
                {
                    add_panel.remove(scrollPane);
                }
                break;
            case "remove":
                break;
        }
        pack();
    }
    
    private void initComponents()
    {
        tabbedPane = new JTabbedPane();
        
        add_panel = makePanel();
        add_panel_ctrl = initControlPanel("add");
        add_panel.add(add_panel_ctrl);
        tabbedPane.add("Add", add_panel);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        
        remove_panel = makePanel();
        tabbedPane.add("Remove", remove_panel);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        
        view_panel = makePanel();
        tabbedPane.add("View products", view_panel);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
        
        add(tabbedPane);
        //allows scrolling tabs
    }
    
    private void filterData()
    {
        try
        {
            RowFilter<SFTableModel, Object> rf = null;
            rf = RowFilter.regexFilter(product_text.getText(), 0);
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
    private String[][] getProducts(String serString)
    {
        try
        {
            System.out.println(serString);
            if (serString.equals(""))
            {
                return new String[0][0];
            }
            ByteArrayInputStream in;
            in = new ByteArrayInputStream(Base64.decode(serString));
            String[][] ret = (String[][]) new ObjectInputStream(in).readObject();
            return ret;
        } catch (IOException | ClassNotFoundException ex)
        {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    //should differ which panel
    private void enablePanelControls(String tab, boolean enabled)
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
    private final String[] columNames = {"Product name", "Barcode", "Expiry date"};

    private String[][] data;
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
        return data[i][i1];
    }
    
    @Override
    public Class getColumnClass(int c)
    {
        return getValueAt(0, c).getClass();
    }
    
    public void setData(String[][] data)
    {
        this.data = data;
    }
    
    public String[][] getData()
    {
        return data;
    }
}