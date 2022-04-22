/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.actions;

import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.jfree.ui.ExtensionFileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Thomas Witthohn
 */
public class ActionUtil {

    public static final String LAST_FOLDER = "lastFolder";

    private static final JFileChooser FILE_CHOOSER = new JFileChooser();

    static {
        FILE_CHOOSER.addChoosableFileFilter(new ExtensionFileFilter("Spreadsheet file", "*.xlsx"));
    }

    public static final void save(String progressMessage, HashMap<String, ISetting> hashMap, BiConsumer<File, HashMap<String, ISetting>> consumer) {
        runFunctionWithProgressBar(progressMessage, () -> {
            File file = chooseFile(FILE_CHOOSER::showSaveDialog);
            if (file != null) {
                consumer.accept(file, hashMap);
            }
        });
    }

    public static final void open(String progressMessage, Consumer<File> consumer) {
        runFunctionWithProgressBar(progressMessage, () -> {
            File file = chooseFile(FILE_CHOOSER::showOpenDialog);
            if (file != null) {
                consumer.accept(file);
            }
        });
    }

    private static File chooseFile(Function<Component, Integer> function) {

        Preferences preferences = NbPreferences.forModule(ActionUtil.class);
        File startingDirectory = new File(preferences.get(ActionUtil.LAST_FOLDER, System.getProperty("user.home")));
        try {
            startingDirectory.getCanonicalPath();
        } catch (IOException ex) {
            startingDirectory = new File(System.getProperty("user.home"));
        }
        if (startingDirectory.exists()) {
            FILE_CHOOSER.setCurrentDirectory(startingDirectory);
        }
        Integer response = function.apply(null);
        File file = null;
        if (response == JFileChooser.APPROVE_OPTION) {
            file = FILE_CHOOSER.getSelectedFile();
            if (file != null) {
                preferences.put(ActionUtil.LAST_FOLDER, file.getParent());
                String path = file.getPath();
                if (!path.endsWith(".xlsx")) {
                    file = new File(path + ".xlsx");
                }
            }
        }
        return file;
    }

    private static void runFunctionWithProgressBar(String progressMessage, Runnable c) {
        ProgressHandle progressHandle = ProgressHandle.createHandle(progressMessage);
        try {
            progressHandle.start();
            c.run();
        } finally {
            progressHandle.finish();
        }
    }

}
