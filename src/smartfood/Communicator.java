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

import  jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.logging.Level;
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
    @Override
    protected void setup()
    {
        addBehaviour(new OneShotBehaviour(this)
        {
            private static final long serialVersionUID = 1L;
           @Override
           public void action()
           {
               logger.info("I am " + getAID().getName());
               initMobile();
           }
        });
    }
    
    @Override
    protected void takeDown()
    {
        logger.info("Agent "+ getAID().getName() + " terminating.");
    }
    
    private void initMobile()
    {
        //lazy way, since I don't have to run two separata processes
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
            logger.log(Level.SEVERE, "Cannot init mobile agent");
            logger.log(Level.SEVERE, exc.getMessage());
        }
    }
}
