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
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
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
    
    //main, consistent components
    private JMenuBar menuBar;
    private JComponent ctrlPanel;
    private JComponent mainPanel;
    
    private TableRowSorter<SFTableModel> sorter;
    private JTable table;
    private JScrollPane scrollPane;
    private JTextField product_text;
    private JDatePickerImpl date_picker;
    private JTextField barcode_text;
    //cache of all products
    private String table_data = "";
    //cache of currently added products
    private String table_current_data = "";
    
    
    private final Cam c;
    private final Reader r;
    private final Comm comm;
    
    public GUI(Comm comm) throws ControllerException
    {
        //helpers
        c = new Cam();
        r = new Reader();
        //communicator
        this.comm = comm;
        //simple data
        setName("SmartFood");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 1));
        setMinimumSize(new Dimension(480, 320));
        
        initComponents();
        pack();
        setVisible(true);
    }
    
    /**
     * Initiates the graphical components
     */
    private void initComponents()
    {
        //add menu
        menuBar = initMenuBar();
        setJMenuBar(menuBar);
        //initial cache
        try
        {
            comm.getData("current-products");
            comm.getData("products");
        }catch (InterruptedException exc)
        {
            logger.log(Level.SEVERE, "Failed to retrieve data");
        }
    }
    
    /**
     * Initiates the menu bar with menus and menu items
     * @return main menu bar
     */
    private JMenuBar initMenuBar()
    {
        JMenuBar mb = new JMenuBar();
        JMenu manage_menu = new JMenu("Manage");
        manage_menu.setMnemonic(KeyEvent.VK_M);
        manage_menu.getAccessibleContext().setAccessibleDescription("Manage products");
        mb.add(manage_menu);
        
        //menu items
        JMenuItem mitem = new JMenuItem("Add product");
        mitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
        mitem.getAccessibleContext().setAccessibleDescription("Register a new product to SmartFood system");
        mitem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                //opens add product
                initPanelState("add");
            }
            
        });
        manage_menu.add(mitem);
        
        mitem = new JMenuItem("Remove product");
        mitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        mitem.getAccessibleContext().setAccessibleDescription("Remove an old/used product from SmartFood system");
        mitem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                //opens remove product
                initPanelState("remove");
            }
            
        });
        manage_menu.add(mitem);
        
        mitem = new JMenuItem("View current products");
        mitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
        mitem.getAccessibleContext().setAccessibleDescription("View currently registered products");
        mitem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                //opens view products list
                initPanelState("view");
            }
            
        });
        manage_menu.add(mitem);
        
        JMenu help_menu = new JMenu("Help");
        help_menu.setMnemonic(KeyEvent.VK_H);
        help_menu.getAccessibleContext().setAccessibleDescription("Get help about this products");
        mb.add(help_menu);
        
        //menu items
        mitem = new JMenuItem("Documentation");
        mitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
        mitem.getAccessibleContext().setAccessibleDescription("Read the documentation of this program");
        mitem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //should open a premade PDF file
                if (Desktop.isDesktopSupported()) 
                {
                    try 
                    {
                        File myFile = new File("documentation.pdf");
                        Desktop.getDesktop().open(myFile);
                    } catch (IOException ex) {
                        // no application registered for PDFs
                        logger.log(Level.SEVERE, "No application registered for PDFs, can't open it");
                    }
                }else
                {
                    logger.log(Level.SEVERE, "Desktop is not supported");
                }
            }
        });
        help_menu.add(mitem);
        
        mitem = new JMenuItem("About");
        mitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.ALT_MASK));
        mitem.getAccessibleContext().setAccessibleDescription("About this program");
        mitem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                initPanelState("about");
            }
        });
        help_menu.add(mitem);
        
        return mb;
                
    }
    
    /**
     * Simply reinitiates the state of main panel and control panel
     * @param state name of state (add, remove, view)
     */
    private void initPanelState(String state)
    {
        if (mainPanel != null)
        {
            remove(mainPanel);
        }
        mainPanel = makePanel();
        ctrlPanel = initControlPanel(state);
        mainPanel.add(ctrlPanel);
        add(mainPanel);
        pack();
    }
    
    /**
     * Initiates the control panel for definable action
     * @param action
     * @return 
     */
    private JComponent initControlPanel(String action)
    {
        JComponent panel = makePanel();
        
        switch(action)
        {
            case "add":
            case "remove":
                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                
                JButton barcode_button = getBarcodeButton(action);
                JButton input_button  = getInputProdButton(action);
                panel.add(barcode_button);
                panel.add(input_button);
                break;
            case "view":
                try
                {
                    //shows current products
                    comm.getData("current-products");
                    waitForDataRetrieval(action, 10);
                    if (!table_current_data.equals("") && 
                            getProducts(table_current_data).length != 0)
                    {
                        String[][] products = getProducts(table_current_data);
                        scrollPane = getDataTableSP(products);
                        panel.add(scrollPane);
                    }
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                break;

            case "about":
                //show a simple about panel
                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                JButton program_button = new JButton("About SmartFood");
                program_button.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        //shows data about the program
                        initPanelState("about");
                        JTextArea textArea = new JTextArea(
                        "SmartFood is a program for managing your "
                                + "currents food products. By saving products, "
                                + "SmartFood is capable of learning about the "
                                + "expiry data of each product, so that it "
                                + "would remind about product's upcoming "
                                + "expiry. This program is an MIT licensed "
                                + "open source project. You can fork it at "
                                + "https://github.com/shookees/SmartFood",15,20);
                        JScrollPane about_scrollPane = new JScrollPane(textArea);
                        textArea.setEditable(false);
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        mainPanel.add(about_scrollPane);
                        pack();
                    }
                });
                JButton developer_button = new JButton("About the developer");
                developer_button.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        //shows data about the developer
                        initPanelState("about");
                        JTextArea textArea = new JTextArea(
                        "The developer, Paulius Šukys, is a 3rd course "
                                + "informatics engineering student at Kaunas "
                                + "University of Technology. This program is "
                                + "a project work for Multi-Agent Systems "
                                + "Fundamentals module.", 15, 20);
                        JScrollPane about_scrollPane = new JScrollPane(textArea);
                        textArea.setEditable(false);
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        mainPanel.add(about_scrollPane);
                        pack();
                    }
                });
                panel.add(program_button);
                panel.add(developer_button);
                break;
            default:
                logger.log(Level.WARNING, 
                        "Unknown action used for initiating ctrl panel");
                break;
        }
        return panel;
    }
    
    /**
     * General method for getting active barcode reader button which inhibits
     * webcam panel, reads the barcode, removes the panel and sends the data to server
     * @param action - 'add' or 'remove' action
     * @return "Read barcode" button
     */
    private JButton getBarcodeButton(final String action)
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
                            enableComponents(ctrlPanel, false);
                            WebcamPanel webcam_panel = c.getPanel(true, true);
                            mainPanel.add(webcam_panel);
                            pack();
                            
                            //try to get the barcode
                            String barcode = retrieveBarcode();
                            JSONObject data = new JSONObject();
                            data.put("barcode", barcode);
                            data.put("expiry", "");
                            data.put("product", "");
                            switch(action)
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
                            mainPanel.remove(webcam_panel);
                            c.stopWebcam();
                            enableComponents(ctrlPanel, true);
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
     * General method for getting active input reader button which inhibits live
     * product name and barcode search, reads the inputand sends the data to server
     * @param action 'add' or 'remove' action
     * @return "Input product" button
     */
        private JButton getInputProdButton(final String action)
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
                            enableComponents(ctrlPanel, false);
                            /*ask for asynchronous data retrieval. When retrieved
                             *the class global "table_data" will be changed*/
                            switch(action)
                            {
                                case "add":
                                    comm.getData("products");
                                    break;
                                case "remove":
                                    comm.getData("current-products");
                                    break;                                
                            }
                            waitForDataRetrieval(action, 10);
                            product_text = getTextField("Product name");
                            product_text.getDocument().addDocumentListener(getDataTableDocumentListener(action, "input"));
                            date_picker = getDatePicker();
                            barcode_text = getTextField("Barcode");
                            barcode_text.getDocument().addDocumentListener(getDataTableDocumentListener(action, "barcode"));
                            JButton submit_button = getSubmitButton(action);
                            ctrlPanel.add(product_text);
                            ctrlPanel.add(barcode_text);
                            ctrlPanel.add(date_picker);
                            ctrlPanel.add(submit_button);
                            
                            //I hate switches
                            String data = "";
                            switch(action)
                            {
                                case "add":
                                    data = table_data;
                                    break;
                                case "remove":
                                    data = table_current_data;
                                    break;
                            }
                            
                            if (!data.equals("") && getProducts(data).length != 0)
                            {
                                String[][] products = getProducts(data);
                                scrollPane = getDataTableSP(products);
                                mainPanel.add(scrollPane);
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
        //FIXME: do not rely on table_data, rather add a flag
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
            case "view":
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
    private JScrollPane getDataTableSP(String[][] tableData)
    {
        SFTableModel model = new SFTableModel();
        model.setData(tableData);
        JScrollPane pane = new JScrollPane();
        sorter = new TableRowSorter<>(model);
        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pane = new JScrollPane(table);
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
                filterData(target);
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                filterData(target);
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                filterData(target);
            }   
        };
        return dl;
    }
    
    /**
     * Returns a tab specific submit button
     * @param tab tab name
     * @return button for submission
     */
    private JButton getSubmitButton(final String action)
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
                        
                        switch(action)
                        {
                            case "add":
                                comm.addData(data.toString());
                                break;
                            case "remove":
                                comm.removeData(data.toString());
                        }
                        
                        logger.log(Level.INFO, "Adding {0}", 
                                table.getValueAt(table.getSelectedColumn(), 0).toString());
                    }else
                    {
                        JSONObject data = new JSONObject();
                        data.put("product", product_text.getText().trim());
                        data.put("barcode", barcode_text.getText().trim());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        if (date_picker.getModel().getValue() == null)
                        {
                            data.put("expiry", "");
                        }else
                        {
                            data.put("expiry", sdf.format(date_picker.getModel().getValue()));
                        }
                        
                        switch(action)
                        {
                            case "add":
                                comm.addData(data.toString());
                                break;
                            case "remove":
                                comm.removeData(data.toString());
                        }
                        logger.log(Level.INFO, 
                                "Adding {0}", product_text.getText());
                    }
                    enableComponents(ctrlPanel, true);
                    initPanelState(action);
                    //refresh data
                    switch(action)
                    {
                        case "add":
                            comm.getData("products");
                            break;
                        case "remove":
                            comm.getData("current-products");
                    }
                }catch(InterruptedException exc)
                {
                    logger.log(Level.WARNING, "Thread has been interrupted");
                }
            }
        });
        return button;
    }
    
    
    private void filterData(String whatToFilter)
    {
        try
        {
            RowFilter<SFTableModel, Object> rf = null;
            switch(whatToFilter)
            {
                case "barcode":
                    rf = RowFilter.regexFilter(product_text.getText(), 0);
                    break;
                case "input":
                    rf = RowFilter.regexFilter(barcode_text.getText(), 0);
                    break;
            }
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
    
    private void enableComponents(JComponent panel, boolean enable)
    {
        Component[] components = panel.getComponents();
        for (Component c : components)
        {
            c.setEnabled(enable);
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