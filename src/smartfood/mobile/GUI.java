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
import java.util.regex.PatternSyntaxException;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

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
    private JTable table;
    private JTextField text;
    private TableRowSorter<SFTableModel> sorter;
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
                            final WebcamPanel panel = c.getPanel(true, true);
                            addPanel.add(panel);
                            pack();
                            String barcode = r.readImage(c.takePicture());
                            while (barcode.equals(""))
                            {
                                barcode = r.readImage(c.takePicture());
                            }
                            addPanel.remove(panel);
                            pack();
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
        
        //input box and result table
        JButton inputProduct_button = new JButton("Input product");
        inputProduct_button.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ae)
            {
                Thread t = new Thread()
                {
                    public void run()
                    {
                        SFTableModel model = new SFTableModel();
                        sorter = new TableRowSorter<SFTableModel>(model);
                        table = new JTable(model);
                        table.setRowSorter(sorter);
                        table.setFillsViewportHeight(true);
                        
                        //single selector
                        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        
                        //when selection changes provide user with row numbers  for both view and model
                        table.getSelectionModel().addListSelectionListener(
                            new ListSelectionListener()
                            {

                                @Override
                                public void valueChanged(ListSelectionEvent lse)
                                {
                                    int viewRow = table.getSelectedRow();
                                    if (viewRow < 0)
                                    {
                                        Logger.getLogger(GUI.class.getName()).info("viewRow < 0");
                                    }else
                                    {
                                        int modelRow = 
                                          table.convertRowIndexToModel(viewRow);
                                        Logger.getLogger(GUI.class.getName()).info("Selected row in view: " + viewRow + "; Selected row in model: " + modelRow);
                                    }
                                }
                            });
                        
                        JScrollPane scrollPane = new JScrollPane(table);
                        
                        
                        text = new JTextField();
                        text.setPreferredSize(new Dimension(80, 20));
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
                        addPanel.add(text);
                        addPanel.add(scrollPane);
                        pack();
                    }
                };
                t.start();
            }
            
        });
        addPanel.add(inputProduct_button);
        
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
    
    private void filterData()
    {
        RowFilter<SFTableModel, Object> rf = null;
        try
        {
            rf = RowFilter.regexFilter(text.getText(), 0);
        }catch(PatternSyntaxException e)
        {
            return;
        }
        sorter.setRowFilter(rf);
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

class SFTableModel extends AbstractTableModel
{
    private String[] columNames = {"Product name"};

    private String[] data = {"Vardas", "Pavarde", "Dar", "Du zodziai", "lietuvių"};
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
    
    public String getColumnName(int col) 
    {
        return columNames[col];
    }
    @Override
    public Object getValueAt(int i, int i1)
    {
        return data[i];
    }
    
    public Class getColumnClass(int c)
    {
        return getValueAt(0, c).getClass();
    }
}