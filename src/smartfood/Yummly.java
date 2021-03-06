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
import jade.core.behaviours.OneShotBehaviour;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Yummly service communicator.
 * It serves for the SmartFood agent and communicates with Yummly to get data.
 * @author Paulius Šukys
 */
public class Yummly extends Agent
{
    private static final long serialVersionUID = 1L;
    final Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());
    private final YummlyWrapper wrapper = new YummlyWrapper();
    @Override
    protected void setup()
    {
        addBehaviour(new OneShotBehaviour(this)
        {
            private static final long serialVersionUID = 1L;
           @Override
           public void action()
           {
               logger.log(Level.INFO, "I am {0}", getAID().getName());
               String ingredients[] = {"apple", "chocolate", "milk"};
               //List matched_recipes = wrapper.searchRecipe("pie", ingredients);
           }
        });
    }
    
    @Override
    protected void takeDown()
    {
        logger.log(Level.INFO, "Agent {0} terminating.", getAID().getName());
    }
}
