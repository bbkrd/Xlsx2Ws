/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class X13SpecificationReaderFactoryTest {

    public X13SpecificationReaderFactoryTest() {
    }

    @Test
    public void testGetNewReaderInstance() {
        X13SpecificationFactory instance = new X13SpecificationFactory();
        Assert.assertTrue(instance.getNewReaderInstance() instanceof X13SpecificationReader);
    }

    @Test
    public void testGetNewWriterInstance() {
        X13SpecificationFactory instance = new X13SpecificationFactory();
        Assert.assertTrue(instance.getNewWriterInstance() instanceof X13SpecificationWriter);
    }

    @Test
    public void testGetSpecificationName() {
        X13SpecificationFactory instance = new X13SpecificationFactory();
        String expected = "X13";
        Assert.assertEquals(expected, instance.getSpecificationName());
    }

}
