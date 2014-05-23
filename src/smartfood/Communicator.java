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

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
//apparently Logger is not such a good package, is it?
import java.util.logging.Logger;

/**
 * Communication agent between mobile and server containers.
 * In a simple context, it currently talks to another packet
 * @author Paulius Šukys
 */
public class Communicator extends Agent
{
    final Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());
    private static final long serialVersionUID = 1L;
    private final AID sf_aid = new AID("SmartFood@SmartFoodSystem", true);
    @Override
    protected void setup()
    {
        //apparently this is bad practice
        /**basic initiation*/
        addBehaviour(new OneShotBehaviour(this)
        {
            private static final long serialVersionUID = 1L;
           @Override
           public void action()
           {
               initMobile();
               logger.log(Level.INFO, "{0} initiated", getAID().getName());
           }
        });
        
        addBehaviour(new CyclicBehaviour(this)
        {

            @Override
            public void action()
            {
                //reading ALL message received
                String sender_name;
                ACLMessage msg = myAgent.receive();
                if (msg != null)
                {
                    sender_name = msg.getSender().getName();
                    switch(msg.getPerformative())
                    {
                        case ACLMessage.REQUEST:
                            //incoming agents requesting some kind of service/data
                            switch(msg.getOntology())
                            {
                                case "products-request":
                                case "current-products-request":
                                    //asking for the list of all products
                                    sendMessage(sf_aid.getName(), msg.getContent(), ACLMessage.REQUEST, msg.getOntology());
                                    
                                    //TODO: add asynchronized version
                                    String return_content = waitForMessage(ACLMessage.INFORM, 
                                            msg.getOntology());
                                    //return back the message
                                    sendMessage(sender_name, return_content, 
                                            ACLMessage.INFORM, msg.getOntology());
                                    break;
                                default:
                                    logger.log(Level.WARNING, "REQUEST: Unknown message ontology: " + msg.getOntology());
                                    break;
                            }
                            break;
                        case ACLMessage.INFORM:
                            //retrieving data from mobile platform
                            switch(msg.getOntology())
                            {  
                                case "add-data":
                                    sendMessage(sf_aid.getName(), msg.getContent(), msg.getPerformative(), msg.getOntology());
                                    break;
                                case "remove-data":
                                    sendMessage(sf_aid.getName(), msg.getContent(), msg.getPerformative(), msg.getOntology());
                                    break;
                                default:
                                    logger.log(Level.WARNING, "INFORM: Unknown message ontology: " + msg.getOntology());
                            }
                            break;
                        default:
                            logger.log(Level.WARNING, "Unknown performative: " + msg.getPerformative());
                            break;
                    }
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
     * A workaround for running two containers in one process
     * It is crucial, that the mobile containers is set up correctly
     */
    private void initMobile()
    {
        try
        {
            jade.core.Runtime rt = jade.core.Runtime.instance();
            jade.core.Profile p = new ProfileImpl();
            p.setParameter(Profile.MAIN_HOST, "");
            p.setParameter(Profile.MAIN_PORT, "");
            p.setParameter(Profile.CONTAINER_NAME, "Mobile");
            AgentContainer cc = rt.createAgentContainer(p);
            AgentController mobile_comm = cc.createNewAgent("Comm", 
                    "smartfood.mobile.Comm", null);
            mobile_comm.start();
        }catch(StaleProxyException exc)
        {
            logger.log(Level.SEVERE, exc.getMessage());
            throw new RuntimeException("Cannot init mobile agent");
        }
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
     * Waits for the message to return of specific performative and ontology
     * 
     * @param performative message's performative
     * @param ontology message's ontology
     * @return String content
     */
    private String waitForMessage(int performative, String ontology)
    {
        ACLMessage msg = receive();
        int waitTime = 10;//seconds
        
        //using the time comparisson method rather than Thread.sleep()
        Calendar current_time = Calendar.getInstance();
        current_time.setTime(new Date());
        Calendar wait_time = Calendar.getInstance();
        wait_time.setTime(new Date());
        wait_time.add(Calendar.SECOND, waitTime);
        
        while(msg == null &&
                current_time.get(Calendar.SECOND) !=
                wait_time.get(Calendar.SECOND))
        {
            msg = receive();
            if (msg != null)
            {
                if (msg.getPerformative() != performative ||
                        !msg.getOntology().equals(ontology)) {
                    msg = null;//not the message we're waiting for
                }
            }
        }
        if (msg != null)
        {
            return msg.getContent();
        }else
        {
            logger.log(Level.SEVERE, "Waited for 10 seconds and no response!");
            return "-";
        }
    }
}
