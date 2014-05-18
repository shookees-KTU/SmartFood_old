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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
                    logger.log(Level.SEVERE, "Error creating agents\n");
                    logger.log(Level.SEVERE, exc.getMessage());
                    System.exit(1);
                } catch (UnknownHostException ex)
                {
                    logger.log(Level.SEVERE, "Error connecting to mongodb host");
                    logger.log(Level.SEVERE, ex.getMessage());
                    System.exit(1);
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
                            content = msg.getContent();
                            switch (content)
                            {
                                case "products all":
                                    //requests for the whole list of products
                                    String b64str = stringArrayToBase64(getUsedProducts());
                                    sendMessage(sender_name, b64str, ACLMessage.INFORM, msg.getOntology());
                                    break;
                            }
                            break;
                    }
                }else
                {
                    
                }
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
    public String[] getUsedProducts()
    {
        DBCollection product_collection  = mongo_db.getCollection("products");
        DBCursor cursor = product_collection.find();
        int product_count = (int)product_collection.getCount();
        String[] products = new String[product_count];
        for(int i = 0; i < product_count; i++)
        {
            products[i] = cursor.next().toString();
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
    private String stringArrayToBase64(String[] array)
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