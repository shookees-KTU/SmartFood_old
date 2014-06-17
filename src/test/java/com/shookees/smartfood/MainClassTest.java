package com.shookees.smartfood;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple Application.
 */
public class MainClassTest 
    extends TestCase
{

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MainClassTest( String testName )
    {
        super( testName );
    }    /**
     * Initial set up for testing
     */
    protected void setUp()
    {
    	
    }
    
    /**
     * Final destructor
     */
    protected void tearDown()
    {
    	
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MainClassTest.class );
    }

    /**
     * Smoke test
     */
    public void testSmoke()
    {
        
    }
}
