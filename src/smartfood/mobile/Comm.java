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

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 
 */
public class Comm extends Agent
{
    final AID server_comm = new AID("Communicator@SmartFoodSystem", true);
    final Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());
    private ContainerController cc;
    private static final long serialVersionUID = 1L;
    private GUI gui;
    
    @Override
    protected void setup()
    {
        addBehaviour(new OneShotBehaviour(this)
        {
            private static final long serialVersionUID = 1L;
           @Override
           public void action()
           {
               //creates his container
               cc = getContainerController();
                try
                {
                    gui = new GUI((Comm)myAgent);
                } catch (ControllerException ex)
                {
                    Logger.getLogger(Comm.class.getName()).log(Level.SEVERE, null, ex);
                }
           }
        });
        //reading messages
        addBehaviour(new CyclicBehaviour()
        {
            private static final long serialVersionUID = 1L;
            @Override
            public void action()
            {
                String name, content;
                ACLMessage msg = myAgent.receive();
                if (msg != null)
                {
                    name = msg.getSender().getName();
                    if (name.equals("Communicator@SmartFoodSystem"))
                    {
                        switch(msg.getPerformative())
                        {
                            case ACLMessage.REQUEST:
                                logger.log(Level.INFO, "Server request received, ID: " + msg.getConversationId());
                                break;
                            case ACLMessage.INFORM:
                                //give info
                                switch(msg.getOntology())
                                {
                                    case "products-request":
                                        gui.setTableData(msg.getContent());
                                        break;
                                    case "notification":
                                        break;
                                }
                                break;
                        }
                    }else
                    {
                        logger.log(Level.WARNING, "A non serverish communicator message received.");
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
    
    protected AgentController createAgent(String agent_name)
    {
        
        AgentController AController = null;
        try
        {
            AController = cc.createNewAgent(agent_name, 
                    this.getClass().getPackage().getName() + "." + agent_name,
                    null);
            AController.start();
        }catch(StaleProxyException exc)
        {
            logger.log(Level.SEVERE, "Problem creating new agent.");
            logger.log(Level.SEVERE, exc.getMessage());
        }
        return AController;
    }
    
    /**
     * Gets data from the main container
     * @param dataName
     * @return 
     * @throws java.lang.InterruptedException 
     */
    public void getData(String data) throws InterruptedException
    {
        
        sendMessage(server_comm.getName(), data, ACLMessage.REQUEST, "products-request");
    }
    
    public void addData(String content) throws InterruptedException
    {
        sendMessage(server_comm.getName(), content, ACLMessage.INFORM, "add-data");
    }
    
    public void removeData(String content) throws InterruptedException
    {
        sendMessage(server_comm.getName(), content, ACLMessage.INFORM, "remove-data");
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
