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
    private AgentController AController;//used for creating new agents
    
    private AID yummly_agent;
    private AID comm_agent;
    @Override
    /**
     * Includes agent initializations
     */
    protected void setup()
    {
        addBehaviour(new OneShotBehaviour(this)
        {
           @Override
           public void action()
           {
               System.out.println("I am " + getAID().getName());
               createAgent("Yummly");
               yummly_agent = new AID("Yummly", AID.ISLOCALNAME);
               createAgent("Communicator");
               comm_agent = new AID("Communicator", AID.ISLOCALNAME);
           }
        });
    }
    
    @Override
    protected void takeDown()
    {
        System.out.println("Agent "+ getAID().getName() + " terminating.");
    }
    
    protected void createAgent(String agent_name)
    {
        try
        {
            AgentContainer container = (AgentContainer)getContainerController();
            AController = container.createNewAgent(agent_name, 
                    this.getClass().getPackage().getName() + "." + agent_name,
                    null);
            AController.start();
        }catch(StaleProxyException exc)
        {
            throw new RuntimeException("Klaida sukuriant agentą.");
        }
    }
}
