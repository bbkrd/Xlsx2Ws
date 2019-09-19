/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.dto;

import java.util.Map;

/**
 *
 * @author Thomas Witthohn
 */
public interface IProviderInfo {

    String getProviderName();

    void addProviderInfo(String key, String value);

    void setProviderName(String name);

    Map<String, String> getProviderInfos();

}
