/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.dto;

import java.util.logging.Level;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Value
public class Message {

    private Level type;
    private String text;

}
