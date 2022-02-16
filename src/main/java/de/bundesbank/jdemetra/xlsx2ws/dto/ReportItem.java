/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Getter
public class ReportItem {

    private static final String NEW_LINE = System.getProperty("line.separator");

    public ReportItem(String documentName, String name, List<Message> messages, boolean isRegressor) {
        this.documentName = documentName;
        this.name = name;
        this.isRegressor = isRegressor;
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();

        highestLevel = this.messages.stream()
                .map(x -> x.getType())
                .filter(x -> x != null)
                .mapToInt(x -> x.intValue())
                .max()
                .orElse(Level.FINE.intValue());
    }

    final String documentName;
    final String name;
    final ArrayList<Message> messages;
    final boolean isRegressor;
    int highestLevel;

    @Override
    public String toString() {
        return documentName + " -> " + name;
    }

    public void addMessage(Message message) {
        if (message == null) {
            return;
        }
        messages.add(message);
        Level level = message.getType();
        if (level != null) {
            highestLevel = highestLevel >= level.intValue() ? highestLevel : level.intValue();
        }
    }

    public String generateReport(boolean pretab) {
        StringBuilder sb = new StringBuilder();
        String prefix = pretab ? "\t" : "";
        if (!messages.isEmpty()) {
            for (Message message : messages) {
                sb.append(prefix).append(message.getType()).append('\t').append(message.getText()).append(NEW_LINE);
            }
            sb.append(NEW_LINE);
        }

        if (getHighestLevel() < Level.SEVERE.intValue()) {
            sb.append(prefix).append("Successfully imported!");
        } else {
            sb.append(prefix).append("Import declined because of severe error(s)!");
        }
        return sb.toString();
    }

}
