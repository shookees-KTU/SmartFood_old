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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Main agent for Smart Food System server container
 * This class is the intelligent agent which configures it's container
 * And helping classes
 * @author Paulius Šukys
 */
public class SmartFood extends Agent
{
    private static final long serialVersionUID = 1L;
    final Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());
    //Main-Container -> Server container
    private AgentController yummly_agent;
    private AgentController comm_agent;
    private MongoClient mongo_client;
    private DB mongo_db;
    @Override
    /**
     * Includes agent initializations
     */
    protected void setup()
    {
        /**initial setup:
         * sets up mongo database
         * sets up yummly agent
         * sets up comm agent
         * 
         * The setup is crucial for correct functioning, on exceptions - exit
         */
        addBehaviour(new OneShotBehaviour(this)
        {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void action()
            {                
                try
                {
                    mongo_client = new MongoClient("localhost");
                    mongo_db = mongo_client.getDB("SmartFood");
                    logger.log(Level.INFO, "Binding {0} database", mongo_db.getName());
                    yummly_agent = createAgent("Yummly");
                    logger.log(Level.INFO, "Starting {0} agent", yummly_agent.getName());
                    comm_agent = createAgent("Communicator");
                    logger.log(Level.INFO, "Starting {0} agent", comm_agent.getName());
                }catch (StaleProxyException exc)
                {
                    logger.log(Level.SEVERE, exc.getMessage());
                    throw new RuntimeException("Error adding agent");
                } catch (UnknownHostException ex)
                {
                    logger.log(Level.SEVERE, ex.getMessage());
                    throw new RuntimeException("Error connecting to mongodb host");
                }
            }
        });
        
        /**messaging inbox:
         * retrieves messages from agents
         * return back
         */
        addBehaviour(new CyclicBehaviour(this)
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void action()
            {
                String content;
                ACLMessage msg = myAgent.receive();
                if (msg != null)
                {
                    String sender_name = msg.getSender().getName();
                    switch (sender_name)
                    {
                        case "Yummly@SmartFoodSystem":
                            content = msg.getContent();
                            logger.log(Level.INFO, "Received message from {0}: {1}", new Object[]{sender_name, content});
                            break;
                           
                        case "Communicator@SmartFoodSystem":
                            BasicDBObject doc = new BasicDBObject();
                            JSONParser parser = new JSONParser();
                            switch (msg.getOntology())
                            {
                                case "products-request":
                                    String b64str = stringMatrixToBase64(getUsedProducts());
                                    sendMessage(sender_name, b64str, ACLMessage.INFORM, msg.getOntology());
                                    //requests for the whole list of products
                                case "current-products-request":
                                    b64str = stringMatrixToBase64(getCurrentProducts());
                                    sendMessage(sender_name, b64str, ACLMessage.INFORM, msg.getOntology());
                                    break;
                                case "add-data":
                                    //add data to database
                                    
                                    try
                                    {
                                        JSONObject json = (JSONObject)parser.parse(msg.getContent());
                                        if (json.get("product") != null)
                                        {
                                            doc.put("product", json.get("product"));
                                        }
                                        
                                        if (json.get("barcode") != null)
                                        {
                                            doc.put("barcode", json.get("barcode"));
                                        }
                                        
                                        if (json.get("expiry") != null)
                                        {
                                            doc.put("expiry", json.get("expiry"));
                                        }
                                    } catch (ParseException ex)
                                    {
                                        Logger.getLogger(SmartFood.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    
                                    
                                    DBCursor cursor = mongo_db.getCollection("products").find(doc);
                                    if (cursor.count() == 0)
                                    {
                                        logger.log(Level.INFO, "Adding product: " + msg.getContent());
                                        mongo_db.getCollection("products").insert(doc);
                                    }else
                                    {
                                        logger.log(Level.INFO, "Product already exists: " + msg.getContent());
                                    }
                                    //add to current products collection
                                    mongo_db.getCollection("current_products").insert(doc);
                                    break;
                                case "remove-data":
                                    try
                                    {
                                        JSONObject json = (JSONObject) parser.parse(msg.getContent());
                                        if (json.get("product") != null)
                                        {
                                            doc.put("product", json.get("product"));
                                        }
                                        
                                        if (json.get("barcode") != null)
                                        {
                                            doc.put("barcode", json.get("barcode"));
                                        }
                                        
                                        if (json.get("expiry") != null)
                                        {
                                            doc.put("expiry", json.get("expiry"));
                                        }
                                    } catch (ParseException ex)
                                    {
                                        Logger.getLogger(SmartFood.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    
                                    mongo_db.getCollection("current_products").remove(doc);
                                    break;
                            }
                            break;
                    }
                }else
                {
                    
                }
            }
            
        });
        
        /**database handler:
         * read database for unfilled values, try to add via other agents
         */
        addBehaviour(new CyclicBehaviour(this) 
        {
            @Override
            public void action()
            {
                //if the expiry date is not set, the default time is 1 week
                /*DBCollection p  = mongo_db.getCollection("products");
                DBCollection c  = mongo_db.getCollection("current_products");
                DBCursor prod = p.find();
                while (prod.hasNext())
                {
                    DBObject obj = prod.next();
                    System.out.println("DB-UPDATER: " + obj);
                    //obj.get("expiry")
                }*/
            }
        });
        
        /**expiry viewer:
         * watches for product expiry dates
         * as products are defined 24 hours within, it check only twice per day
         */
        int ticker = 12 * 60 * 60 * 1000;//hours*minutes*seconds*miliseconds
        addBehaviour(new TickerBehaviour(this, ticker) 
        {
            @Override
            protected void onTick()
            {
                    
            }
        });
    }
    
    @Override
    protected void takeDown()
    {
        logger.log(Level.INFO, "Agent {0} terminating.", getAID().getName());
    }
    
    /**
     * Creates a new agent in local container
     * 
     * @param agent_name the name of the agent
     * @return AController AgentController object created in local container
     * @throws StaleProxyException when an attempt to use stale (i.e. outdated) wrapper object is made
     */
    protected AgentController createAgent(String agent_name)
            throws StaleProxyException
    {
        AgentContainer container = getContainerController();
        AgentController AController = container.createNewAgent(agent_name, 
                this.getClass().getPackage().getName() + "." + agent_name,
                null);
        AController.start();
        return AController;
    }
    
    /**
     * Gets all used products
     * 
     * @return String[] array of used products
     */
    public String[][] getUsedProducts()
    {
        DBCollection product_collection  = mongo_db.getCollection("products");
        DBCursor cursor = product_collection.find();
        int product_count = (int)product_collection.getCount();
        String[][] products = new String[product_count][3];
        for(int i = 0; i < product_count; i++)
        {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            products[i][0] = obj.getString("product");
            products[i][1] = obj.getString("barcode");
            products[i][2] = obj.getString("expiry");
        }
        return products;
    }
    
    public String[][] getCurrentProducts()
    {
        DBCollection product_collection  = mongo_db.getCollection("current_products");
        DBCursor cursor = product_collection.find();
        int product_count = (int)product_collection.getCount();
        String[][] products = new String[product_count][3];
        for(int i = 0; i < product_count; i++)
        {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            products[i][0] = obj.getString("product");
            products[i][1] = obj.getString("barcode");
            products[i][2] = obj.getString("expiry");
        }
        return products;
    }
    
    /**
     * Sends a message to agent
     * 
     * @param to agent GUID name
     * @param content content to be sent
     * @param performative performative level
     */
    private void sendMessage(String to, String content, int performative, String ontology)
    {
        ACLMessage msg;
        msg = new ACLMessage(performative);
        msg.addReceiver(new AID(to, true));
        msg.setOntology(ontology);
        msg.setContent(content);
        send(msg);
    }
    
    /**
     * Serializes a string array into base64 single string
     * 
     * @param array the string array to be serialized
     * @return serialized base64 string
     */
    private String stringMatrixToBase64(String[][] array)
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try
        {
            ObjectOutputStream oout;
            oout = new ObjectOutputStream(bout);
            oout.writeObject(array);
            oout.close();
        } catch (IOException ex)
        {
            Logger.getLogger(SmartFood.class.getName()).log(Level.SEVERE, "Error in serialization");
            Logger.getLogger(SmartFood.class.getName()).log(Level.SEVERE, null, ex);
        }
        String b64str = Base64.encode(bout.toByteArray());
        return b64str;
    }
}