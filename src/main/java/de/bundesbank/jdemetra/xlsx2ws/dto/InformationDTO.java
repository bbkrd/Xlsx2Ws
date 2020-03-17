/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.dto;

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
        if (lowercaseInput.equals("multidoc") || lowercaseInput.equals("variables")) {
            name = lowercaseInput;
            type = InformationType.DOCUMENT_NAME;
        } else if (lowercaseInput.equals("saitem") || lowercaseInput.equals("name")) {
            name = lowercaseInput;
            type = InformationType.ITEM_NAME;
        } else if (lowercaseInput.equals("providername")) {
            name = lowercaseInput;
            type = InformationType.PROVIDER_NAME;
        } else if (lowercaseInput.startsWith("p_") || lowercaseInput.startsWith("prov_")) {
            name = lowercaseInput.substring(2);
            type = InformationType.PROVIDER_INFO;
        } else if (lowercaseInput.equals("specificationname")) {
            name = lowercaseInput;
            type = InformationType.SPECIFICATION_NAME;
        } else if (lowercaseInput.startsWith("s_") || lowercaseInput.startsWith("spec_")) {
            name = lowercaseInput.substring(2);
            type = InformationType.SPECIFICATION_INFO;
        } else if (lowercaseInput.startsWith("m_") || lowercaseInput.startsWith("meta_")) {
            name = lowercaseInput.substring(2);
            type = InformationType.METADATA;
        } else {
            name = lowercaseInput;
            type = InformationType.UNKNOWN;
        }
    }

}
