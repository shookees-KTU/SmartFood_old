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
                    name = name.substring(0, name.indexOf("@"));
                    if (name.equals("Communicator"))
                    {
                        switch(msg.getPerformative())
                        {
                            case ACLMessage.INFORM:
                                logger.log(Level.INFO, "Server inform received, ID: " + msg.getConversationId());
                                gui.notify(msg.getContent());
                                break;
                            case ACLMessage.REQUEST:
                                logger.log(Level.INFO, "Server request received, ID: " + msg.getConversationId());
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
    public String getData(String dataName) throws InterruptedException
    {
        ACLMessage msg;
        msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(server_comm);
        msg.setContent(dataName);
        send(msg);
        
        msg = receive();
        int waitTime = 10;//seconds
        while(msg == null && waitTime != 0)
        {
            logger.info("Message sent to server communicator, waiting for response...");
            Thread.sleep(1000);//1 second
            waitTime -= 1;
            msg = receive();
        }
        
        if (msg == null)
        {
            logger.log(Level.SEVERE, "Waited for 10 seconds and no response!");
            return "";
        }else
        {
            return msg.getContent();
        }
    }
    
    public void addData(String dataName, String dataValue) throws InterruptedException
    {
        ACLMessage msg;
        msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(server_comm);
        msg.setContent(dataName + ": " + dataValue);
        send(msg);
    }
}
