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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A wrapper for Yummly
 * @author Paulius Šukys
 */
public class YummlyWrapper 
{
    //singleton since only one connection is needed
    
    private static final String API_URL = "https://api.yummly.com/v1/api/";
    private static final String APP_ID = "1cf18976";
    private static final String APP_KEY = "59bc08e9d8e8d840454478fbca8ae959";
    
    YummlyWrapper()
    {
        //exists to defeat instantiation
    }
    
    public static List searchRecipe(String recipe, String[] allowedIngredients)
    {
        String query = "recipes?q=" + recipe;
        List<Recipe> recipes = new ArrayList();
        try
        {
            for (String ingredient: allowedIngredients)
            {
                query += "&allowedIngredient[]="; 
                query += URLEncoder.encode(ingredient, "UTF-8");
            }
            String response = getResponse(API_URL + query);
            //since response is in json - parse it!
            JSONParser jsonparser = new JSONParser();
            JSONObject obj = (JSONObject)jsonparser.parse(response);
            long matchCount = (long)obj.get("totalMatchCount");
            //adding all matches to array
            JSONArray matches = (JSONArray)obj.get("matches");
            Iterator<JSONObject> iterator = matches.iterator();
            
            while (iterator.hasNext())
            {
                JSONObject match = iterator.next();
                //setting up a Recipe object and appending to arraylist
                Recipe r = new Recipe();
                JSONObject flavors = (JSONObject)match.get("flavors");
                if (flavors != null)
                {
                    r.addFlavor("salty", (double)flavors.get("salty"));
                    r.addFlavor("meaty", (double)flavors.get("meaty"));
                    r.addFlavor("sour", (double)flavors.get("sour"));
                    r.addFlavor("sweet", (double)flavors.get("sweet"));
                    r.addFlavor("bitter", (double)flavors.get("bitter"));
                }
                r.setRating((long)match.get("rating"));
                r.setName((String)match.get("recipeName"));
                r.setSource((String)match.get("sourceDisplayName"));
                JSONArray ing = (JSONArray)match.get("ingredients");
                Iterator<String> ii = ing.iterator();
                while(ii.hasNext())
                {
                    r.addIngredient((String)ii.next());
                }
                r.setId((String)match.get("id"));
                recipes.add(r);
            }
            
        }catch(UnsupportedEncodingException exc)
        {
            System.out.println("Encoding error");
            System.out.println(exc.getMessage());
        }catch(ParseException exc)
        {
            System.out.println("Parsing error");
            System.out.println(exc.getMessage());
        }
        return recipes;
    }
    
    private static String getResponse(String query)
    {
        StringBuilder response = new StringBuilder();
        try
        {
            URL url = new URL(query);
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Yummly-App-ID", APP_ID);
            conn.setRequestProperty("X-Yummly-App-Key", APP_KEY);

            String res = checkResponse(conn.getResponseCode());
            if(!res.isEmpty())
            {
                System.out.println(res);  
            }else
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                                                        conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    response.append(inputLine);
                }
                in.close();
            }
            conn.disconnect();
        }catch(MalformedURLException exc)
        {
            System.out.println("Malformed URL:");
            System.out.println(exc.getMessage());
        }catch(IOException exc)
        {
            System.out.println("IO error");
            System.out.println(exc.getMessage());
        }
        return response.toString();
    }
    
    private static String checkResponse(int response)
    {
        String msg = "";
        switch(response)
        {
            case 400:
                msg = "Bad request";
                break;
            case 409:
                msg = "API Rate Limit Exceeded";
                break;
            case 500:
                msg = "Internal Server Error";
                break;
        }
        return msg;
    }
}
