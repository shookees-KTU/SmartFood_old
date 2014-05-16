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

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentContainer;
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
    final Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());
    ContainerController cc;
    private static final long serialVersionUID = 1L;
    
    AgentController cam, reader;
    
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
                    GUI g = new GUI(cc);
                } catch (ControllerException ex)
                {
                    Logger.getLogger(Comm.class.getName()).log(Level.SEVERE, null, ex);
                }
           }
        });
    }
    
    @Override
    protected void takeDown()
    {
        logger.info("Agent "+ getAID().getName() + " terminating.");
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
}
