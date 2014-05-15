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
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import smartfood.mobile.GUI;


/**
 * Main agent for Smart Food System server container
 * This class is the intelligent agent which configures it's container
 * And helping classes
 * @author Paulius Šukys
 */
public class SmartFood extends Agent
{
    final Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());
    //Main-Container -> Server container
    private AgentController yummly_agent;
    private AgentController comm_agent;
    private MongoClient mongo;
    private DB db;
    @Override
    /**
     * Includes agent initializations
     */
    protected void setup()
    {
        //initial setup
        addBehaviour(new OneShotBehaviour(this)
        {
            private static final long serialVersionUID = 1L;
            @Override
            public void action()
            {                
                try
                {
                    mongo = new MongoClient("localhost");
                    db = mongo.getDB("SmartFood");
                    logger.info("Database binded");
                    yummly_agent = createAgent("Yummly");
                    logger.info("Created: " + yummly_agent.getName());
                    comm_agent = createAgent("Communicator");
                    logger.info("Created: " + comm_agent.getName());
                    GUI g = new GUI();
                    logger.info("GUI started");
                    
                }catch (StaleProxyException exc)
                {
                    logger.log(Level.SEVERE, "Error creating agents\n");
                    logger.log(Level.SEVERE, exc.getMessage());
                } catch (UnknownHostException ex)
                {
                    logger.log(Level.SEVERE, "Error connecting to mongodb host");
                    logger.log(Level.SEVERE, ex.getMessage());
                }
            }
        });
        
        //general purpose behaviour
        addBehaviour(new CyclicBehaviour(this)
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void action()
            {
                //Yummly implementation
                ACLMessage msg = myAgent.receive();
                if (msg != null)
                {
                    //gets name and removes the platform name
                    String name = msg.getSender().getName();
                    name = name.substring(0, name.indexOf("@"));
                    switch(name)
                    {
                        case "Yummly":
                            String content = msg.getContent();
                            logger.info("Received message from " + 
                                            name + ": " + content);
                            break;
                        case "Communicator":
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
        logger.info("Agent "+ getAID().getName() + " terminating.");
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
    
    public String[] getUsedProducts()
    {
        DBCollection product_collection  = db.getCollection("products");
        DBCursor cursor = product_collection.find();
        int product_count = (int)product_collection.getCount();
        String[] products = new String[product_count];
        for(int i = 0; i < product_count; i++)
        {
            products[i] = cursor.next().toString();
        }
        return products;
    }
}