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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A recipe object for retrieved Yummly request
 * @author Paulius Šukys
 */
public class Recipe 
{
    final Logger logger = jade.util.Logger.getMyLogger(this.getClass().getName());
    private Map<String, Double> flavors;//flavor - 0..1 percentage
    private String name;
    private String source;
    private int rating;
    private List ingredients;
    private String id;
    public Recipe()
    {
        ingredients = new ArrayList();
        flavors = new HashMap<String, Double>();
        flavors.put("salty", 0.0);
        flavors.put("meaty", 0.0);
        flavors.put("sour", 0.0);
        flavors.put("sweet", 0.0);
        flavors.put("bitter", 0.0);
    }
    public void addFlavor(String flavor, double ratio)
    {
        flavors.put(flavor, ratio);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public void setRating(int rating)
    {
        this.rating = rating;
    }
    
    public void setRating(long rating)
    {
        this.rating = (int) rating;
    }

    public void addIngredient(String ingredient)
    {
        ingredients.add(ingredient);
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public double getFlavor(String flavor)
    {
        return flavors.get(flavor);
    }

    public String getName()
    {
        return name;
    }

    public String getSource()
    {
        return source;
    }

    public int getRating()
    {
        return rating;
    }

    public String getIngredients(int i)
    {
        return (String)ingredients.get(i);
    }

    public String getId()
    {
        return id;
    }
    
    //mainly for debugging
    public String toString()
    {
        String r = "Name: " + name + "\n";
        r += "ID: " + id + "\n";
        r += "Source: " + source + "\n";
        r += "Rating: " + rating + "\n";
        r += "Ingredients: ";
        for (Object ingredient: ingredients)
        {
            r += ingredient + "; ";
        }
        r += "\n";
        r += "Flavors:\n";
        Iterator iterator = flavors.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            r += "\t" + mapEntry.getKey() + ": " + mapEntry.getValue() + "\n";
        }
        return r;
    }
}
