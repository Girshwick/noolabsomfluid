package org.NooLab.utilities.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser; 



public class OpenDialog {

    public static void main(String[] args) {
        OpenDialog odc = new OpenDialog();
        odc.openFolder();
    }

    public void openFolder() {
    	
        final JFileChooser chooser = new JFileChooser("Verzeichnis w�hlen");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        final File file = new File("/home");

        chooser.setCurrentDirectory(file);

        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
                        || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    final File f = (File) e.getNewValue();
                }
            }
        });

        chooser.setVisible(true);
        final int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = chooser.getSelectedFile();
            String inputVerzStr = inputVerzFile.getPath();
            System.out.println("Eingabepfad:" + inputVerzStr);
        }
        System.out.println("Abbruch");
        chooser.setVisible(false);
    }
} 
