/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.dto;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Value
public class PositionInfo implements Comparable<Object> {

    int position;
    String name;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof PositionInfo) {
            return Integer.compare(position, ((PositionInfo) o).position);
        } else {
            return 0;
        }
    }

}
