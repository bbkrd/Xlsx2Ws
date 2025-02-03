package de.bundesbank.jdemetra.xlsx2ws.converter;

import java.util.TreeSet;

@lombok.Value
public class PrefixSet<T> {

    private final String prefix;
    private final TreeSet<T> set;

}
