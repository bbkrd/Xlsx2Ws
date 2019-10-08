package de.bundesbank.jdemetra.xlsx2ws;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsException;

/**
 *
 * @author Thomas Witthohn
 */
public class DayBuilder {

    private int year = 1970;
    private int month = 1;
    private int day = 1;

    public Day build() {
        try {
            return new Day(year, Month.valueOf(month - 1), day - 1);
        } catch (TsException e) {
            //TODO LOG!!!!!
            return null;
        }
    }

    public DayBuilder year(int year) {
        this.year = year;
        return this;
    }

    public DayBuilder month(int month) {
        this.month = month;
        return this;
    }

    public DayBuilder day(int day) {
        this.day = day;
        return this;
    }

}
