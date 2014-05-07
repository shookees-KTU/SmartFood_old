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

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;


/**
 * Main agent for Smart Food System server container
 * This class is the intelligent agent which configures it's container
 * And helping classes
 * @author Paulius Šukys
 */
public class SmartFood extends Agent
{
    //Main-Container -> Server container
    private AgentController yummly_agent;
    private AgentController comm_agent;
    
    @Override
    /**
     * Includes agent initializations
     */
    protected void setup()
    {
        //initial setup
        addBehaviour(new OneShotBehaviour(this)
        {
            @Override
            public void action()
            {
                System.out.println("I am " + getAID().getName());
                try
                {
                    yummly_agent = createAgent("Yummly");
                    System.out.println("Created: " + yummly_agent.getName());
                    comm_agent = createAgent("Communicator");
                    System.out.println("Created: " + comm_agent.getName());
                }catch (StaleProxyException exc)
                {
                    System.out.println("Error creating agents\n");
                    System.out.println(exc.getMessage());
                }
            }
        });
        
        //general purpose behaviour
        addBehaviour(new CyclicBehaviour(this)
        {

            @Override
            public void action()
            {
                //Yummly implementation
                jade.lang.acl.ACLMessage msg = myAgent.receive();
                if (msg != null)
                {
                    //gets name and removes the platform name
                    String name = msg.getSender().getName();
                    name = name.substring(0, name.indexOf("@"));
                    switch(name)
                    {
                        case "Yummly":
                            String content = msg.getContent();
                            System.out.println("Received message from " + 
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
        System.out.println("Agent "+ getAID().getName() + " terminating.");
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
}
