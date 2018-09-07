/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import java.util.Locale;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Getter
public class InformationDTO {

    private final String name;
    private final InformationType type;

    public InformationDTO(String input) {
        String lowercaseInput = input.toLowerCase(Locale.ENGLISH);
        if (lowercaseInput.equals("multidoc")) {
            name = lowercaseInput;
            type = InformationType.MULTIDOCUMENT_NAME;
        } else if (lowercaseInput.equals("saitem")) {
            name = lowercaseInput;
            type = InformationType.SAITEM_NAME;
        } else if (lowercaseInput.equals("providername")) {
            name = lowercaseInput;
            type = InformationType.PROVIDER_NAME;
        } else if (lowercaseInput.startsWith("p_")) {
            name = lowercaseInput.substring(2);
            type = InformationType.PROVIDER_INFO;
        } else if (lowercaseInput.equals("specificationname")) {
            name = lowercaseInput;
            type = InformationType.SPECIFICATION_NAME;
        } else if (lowercaseInput.startsWith("s_")) {
            name = lowercaseInput.substring(2);
            type = InformationType.SPECIFICATION_INFO;
        } else if (lowercaseInput.startsWith("m_")) {
            name = lowercaseInput.substring(2);
            type = InformationType.METADATA;
        } else {
            name = lowercaseInput;
            type = InformationType.UNKNOWN;
        }
    }

}
