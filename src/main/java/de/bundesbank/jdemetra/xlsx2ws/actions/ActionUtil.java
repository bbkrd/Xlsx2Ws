/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.actions;

import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbPreferences;

/**
 *
 * @author s4504tw
 */
public class ActionUtil {

    public static final String LAST_FOLDER = "lastFolder";

    private static final FileChooser FILE_CHOOSER = new FileChooser();

    static {
        FILE_CHOOSER.getExtensionFilters().add(new FileChooser.ExtensionFilter("Spreadsheet file", "*.xlsx"));
        new JFXPanel();
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

    private static File chooseFile(Function<Window, File> function) {

        Preferences preferences = NbPreferences.forModule(ActionUtil.class);
        File startingDirectory = new File(preferences.get(ActionUtil.LAST_FOLDER, System.getProperty("user.home")));
        try {
            startingDirectory.getCanonicalPath();
        } catch (IOException ex) {
            startingDirectory = new File(System.getProperty("user.home"));
        }
        if (startingDirectory.exists()) {
            FILE_CHOOSER.setInitialDirectory(startingDirectory);
        }
        File file = function.apply(null);
        if (file != null) {
            preferences.put(ActionUtil.LAST_FOLDER, file.getParent());
        }
        return file;
    }

    private static void runFunctionWithProgressBar(String progressMessage, Runnable c) {

        Platform.runLater(() -> {
            ProgressHandle progressHandle = ProgressHandle.createHandle(progressMessage);
            try {
                progressHandle.start();
                c.run();
            } finally {
                progressHandle.finish();
            }
        });
    }

}
