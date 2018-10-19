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
    public void testGetNewInstance() {
        X13SpecificationReaderFactory instance = new X13SpecificationReaderFactory();
        Assert.assertTrue(instance.getNewInstance() instanceof X13SpecificationReader);
    }

    @Test
    public void testGetSpecificationName() {
        X13SpecificationReaderFactory instance = new X13SpecificationReaderFactory();
        String expected = "X13";
        Assert.assertEquals(expected, instance.getSpecificationName());
    }

}
