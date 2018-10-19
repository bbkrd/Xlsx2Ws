package de.bundesbank.jdemetra.xlsx2ws;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;

/**
 *
 * @author Thomas Witthohn
 */
public class DayBuilder {

    private int year = 1970;
    private int month = 0;
    private int day = 0;

    public Day build() {
        return new Day(year, Month.valueOf(month), day);
    }

    public DayBuilder year(int year) {
        this.year = year;
        return this;
    }

    public DayBuilder month(int month) {
        this.month = month - 1;
        return this;
    }

    public DayBuilder day(int day) {
        this.day = day - 1;
        return this;
    }

}
