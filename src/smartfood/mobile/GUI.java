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
    private JComponent remove_panel_ctrl;
    
    private JButton add_readBarcode_button;
    private JButton add_inputProduct_button;
    private JButton rem_readBarcode_button;
    private JButton rem_inputProduct_button;
    private JTextField add_product_text;
    private JTextField add_barcode_text;
    private JTextField rem_product_text;
    private JTextField rem_barcode_text;
    private JScrollPane add_scrollPane;
    private TableRowSorter<SFTableModel> add_sorter;
    private JTable add_table;
    private JButton add_submit_button;
    private JDatePickerImpl add_date_picker;
    private JScrollPane rem_scrollPane;
    private TableRowSorter<SFTableModel> rem_sorter;
    private JTable rem_table;
    private JButton rem_submit_button;
    private JDatePickerImpl rem_date_picker;
    private String table_data = "";
    private String table_current_data = "";
    
    
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
                
                add_readBarcode_button = getBarcodeButton(tab);
                add_inputProduct_button = getInputProdButton(tab);
                panel.add(add_readBarcode_button);
                panel.add(add_inputProduct_button);
                break;
            case "remove":
                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                
                rem_readBarcode_button = getBarcodeButton(tab);
                rem_inputProduct_button = getInputProdButton(tab);
                panel.add(rem_readBarcode_button);
                panel.add(rem_inputProduct_button);
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
                            WebcamPanel webcam_panel = c.getPanel(true, true);
                            switch(tab)
                            {
                                case "add":
                                    add_panel.add(webcam_panel);
                                    break;
                                case "remove":
                                    remove_panel.add(webcam_panel);
                            }
                            
                            pack();
                            
                            //try to get the barcode
                            String barcode = retrieveBarcode();
                            JSONObject data = new JSONObject();
                            data.put("barcode", barcode);
                            data.put("expiry", "");
                            data.put("product", "");
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
                            switch(tab)
                            {
                                case "add":
                                    add_panel.remove(webcam_panel);
                                    break;
                                case "remove":
                                    remove_panel.remove(webcam_panel);
                            }
                            enablePanelControls(tab, true);
                            webcam_panel = null;
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
                            waitForDataRetrieval(tab, 10);
                            switch(tab)
                            {
                                case "add":
                                    comm.getData("products");
                                    add_product_text = getTextField("Product name");
                                    add_product_text.getDocument().addDocumentListener(getDataTableDocumentListener(tab, "input"));
                                    add_date_picker = getDatePicker();
                                    add_barcode_text = getTextField("Barcode");
                                    add_barcode_text.getDocument().addDocumentListener(getDataTableDocumentListener(tab, "barcode"));
                                    //submit button for control panel
                                    add_submit_button = getSubmitButton(tab);

                                    //adding to control panel
                                    add_panel_ctrl.add(add_product_text);
                                    add_panel_ctrl.add(add_barcode_text);
                                    add_panel_ctrl.add(add_date_picker);
                                    add_panel_ctrl.add(add_submit_button);
                                    break;
                                case "remove":
                                    comm.getData("current-products");
                                    rem_product_text = getTextField("Product name");
                                    rem_product_text.getDocument().addDocumentListener(getDataTableDocumentListener(tab, "input"));
                                    rem_barcode_text = getTextField("Barcode");
                                    rem_barcode_text.getDocument().addDocumentListener(getDataTableDocumentListener(tab, "barcode"));
                                    //submit button for control panel
                                    rem_submit_button = getSubmitButton(tab);

                                    //adding to control panel
                                    remove_panel_ctrl.add(rem_product_text);
                                    remove_panel_ctrl.add(rem_barcode_text);
                                    remove_panel_ctrl.add(rem_submit_button);
                                    break;
                            }
                            //I hate switches
                            String data = "";
                            switch(tab)
                            {
                                case "add":
                                    data = table_data;
                                    break;
                                case "remove":
                                    data = table_current_data;
                                    break;
                            }
                            if (data != "" && getProducts(data).length != 0)
                            {
                                String[][] products = getProducts(data);
                                switch(tab)
                                {
                                    case "add":
                                        add_scrollPane = getDataTableSP(tab, products);
                                        add_panel.add(add_scrollPane);
                                        break;
                                    case "remove":
                                        rem_scrollPane = getDataTableSP(tab, products);
                                        remove_panel.add(rem_scrollPane);
                                }
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
    private void waitForDataRetrieval(String tab, int waitSeconds)
    {
        Calendar current_time = Calendar.getInstance();
        current_time.setTime(new Date());
        Calendar wait_time = Calendar.getInstance();
        wait_time.setTime(new Date());
        wait_time.add(Calendar.SECOND, waitSeconds);
        switch(tab)
        {
            case "add":
                while(table_data.equals("") &&
                current_time.get(Calendar.SECOND) !=
                wait_time.get(Calendar.SECOND))
                break;
            case "remove":
                while(table_current_data.equals("") &&
                current_time.get(Calendar.SECOND) !=
                wait_time.get(Calendar.SECOND))
                break;
        }
        
            
    }
    
    /**
     * Creates a sortable table model with populated data
     * @param tableData data to populate
     * @return scroll pane  for the table model
     */
    private JScrollPane getDataTableSP(String tab, String[][] tableData)
    {
        SFTableModel model = new SFTableModel();
        model.setData(tableData);
        JScrollPane pane = new JScrollPane();
        switch(tab)
        {
            case "add":
                add_sorter = new TableRowSorter<>(model);
                add_table = new JTable(model);
                add_table.setRowSorter(add_sorter);
                add_table.setFillsViewportHeight(true);
                add_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                pane = new JScrollPane(add_table);
                break;
            case "remove":
                rem_sorter = new TableRowSorter<>(model);
                rem_table = new JTable(model);
                rem_table.setRowSorter(rem_sorter);
                rem_table.setFillsViewportHeight(true);
                rem_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                pane = new JScrollPane(rem_table);
                break;
        }
        
        return pane;
    }
    
    /**
     * Creates a simple data table document listener which updates on any action
     * @return the document listener
     */
    private DocumentListener getDataTableDocumentListener(final String tab, final String target)
    {
        DocumentListener dl = new DocumentListener()
        {

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                filterData(tab, target);
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                filterData(tab, target);
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                filterData(tab, target);
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
                    if (add_scrollPane != null && add_table.getSelectedRow() != -1)
                    {
                        JSONObject data = new JSONObject();
                        switch(tab)
                        {
                            case "add":
                                data.put("product", add_table.getValueAt(add_table.getSelectedRow(), 0).toString().trim());
                                data.put("barcode", add_table.getValueAt(add_table.getSelectedRow(), 1).toString().trim());
                                data.put("expiry", add_table.getValueAt(add_table.getSelectedRow(), 2).toString().trim());
                                break;
                            case "remove":
                                data.put("product", rem_table.getValueAt(rem_table.getSelectedRow(), 0).toString().trim());
                                data.put("barcode", rem_table.getValueAt(rem_table.getSelectedRow(), 1).toString().trim());
                                data.put("expiry", rem_table.getValueAt(rem_table.getSelectedRow(), 2).toString().trim());
                                break;
                        }
                        
                        comm.addData(data.toString());
                        logger.log(Level.INFO, "Adding {0}", 
                                add_table.getValueAt(add_table.getSelectedColumn(), 0).toString());
                    }else
                    {
                        JSONObject data = new JSONObject();
                        data.put("product", add_product_text.getText().trim());
                        data.put("barcode", add_barcode_text.getText().trim());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        if (add_date_picker.getModel().getValue() == null)
                        {
                            data.put("expiry", "");
                        }else
                        {
                            data.put("expiry", sdf.format(add_date_picker.getModel().getValue()));
                        }
                        comm.addData(data.toString().trim());
                        logger.log(Level.INFO, 
                                "Adding {0}", add_product_text.getText());
                    }
                    enablePanelControls(tab, true);
                    //remove controls and scrollpane
                    removeControls(tab);
                    //refresh data
                    switch(tab)
                    {
                        case "add":
                            comm.getData("products");
                            break;
                        case "remove":
                            comm.getData("");
                    }
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
                add_panel_ctrl.remove(add_product_text);
                add_panel_ctrl.remove(add_barcode_text);
                add_panel_ctrl.remove(add_date_picker);
                add_panel_ctrl.remove(add_submit_button);
                if (add_scrollPane != null)
                {
                    add_panel.remove(add_scrollPane);
                }
                break;
            case "remove":
                remove_panel_ctrl.remove(rem_product_text);
                remove_panel_ctrl.remove(rem_barcode_text);
                remove_panel_ctrl.remove(rem_date_picker);
                remove_panel_ctrl.remove(rem_submit_button);
                if (add_scrollPane != null)
                {
                    remove_panel.remove(rem_scrollPane);
                }
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
        remove_panel_ctrl = initControlPanel("remove");
        remove_panel.add(remove_panel_ctrl);
        tabbedPane.add("Remove", remove_panel);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        
        view_panel = makePanel();
        tabbedPane.add("View products", view_panel);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
        
        add(tabbedPane);
        //allows scrolling tabs
    }
    
    private void filterData(String tab, String whatToFilter)
    {
        try
        {
            RowFilter<SFTableModel, Object> rf = null;
            switch(tab)
            {
                case "add":
                    switch(whatToFilter)
                    {
                        case "barcode":
                            rf = RowFilter.regexFilter(add_product_text.getText(), 0);
                            add_sorter.setRowFilter(rf);
                            break;
                        case "input":
                            rf = RowFilter.regexFilter(add_barcode_text.getText(), 0);
                            add_sorter.setRowFilter(rf);
                            break;
                    }
                    break;
                case "remove":
                    switch(whatToFilter)
                    {
                        case "barcode":
                            rf = RowFilter.regexFilter(rem_product_text.getText(), 0);
                            rem_sorter.setRowFilter(rf);
                            break;
                        case "input":
                            rf = RowFilter.regexFilter(rem_barcode_text.getText(), 0);
                            rem_sorter.setRowFilter(rf);
                            break;
                    }
                    break;
            }
            
            
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
        switch(tab)
        {
            case "add":
                add_inputProduct_button.setEnabled(enabled);
                add_readBarcode_button.setEnabled(enabled);
                break;
            case "remove":
                rem_inputProduct_button.setEnabled(enabled);
                rem_readBarcode_button.setEnabled(enabled);
                break;
        }
        
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
    
    public void setCurrentData(String data)
    {
        table_current_data = data;
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