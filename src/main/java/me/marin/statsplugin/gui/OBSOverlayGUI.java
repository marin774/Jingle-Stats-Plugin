package me.marin.statsplugin.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.gui.JingleGUI;
import xyz.duncanruns.jingle.util.ExceptionUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static me.marin.statsplugin.StatsPlugin.*;

public class OBSOverlayGUI extends JFrame {

    private boolean isClosed = false;

    private JTextPane folderPathTextPane;
    private JButton copyFilePathButton;
    private JTextArea overlayFormatTextArea;
    private JButton updateOverlayFormatButton;
    private JPanel mainPanel;

    public OBSOverlayGUI() {
        this.setContentPane(mainPanel);
        this.setTitle("OBS Overlay Config - Stats Plugin");
        this.pack();
        this.setVisible(true);
        this.setResizable(false);
        this.setLocation(JingleGUI.get().getLocation());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isClosed = true;
            }
        });

        folderPathTextPane.setText(OBS_OVERLAY_PATH.toAbsolutePath().toString());
        copyFilePathButton.addActionListener(a -> {
            StringSelection stringSelection = new StringSelection(OBS_OVERLAY_PATH.toAbsolutePath().toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        try {
            overlayFormatTextArea.setText(new String(Files.readAllBytes(OBS_OVERLAY_TEMPLATE_PATH), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log(Level.ERROR, "Failed to read obs-overlay-template:\n" + ExceptionUtil.toDetailedString(e));
        }
        updateOverlayFormatButton.addActionListener(a -> {
            String text = overlayFormatTextArea.getText();
            try {
                Files.write(OBS_OVERLAY_TEMPLATE_PATH, text.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                CURRENT_SESSION.updateOverlay();
                JOptionPane.showMessageDialog(null, "Updated overlay template!");
            } catch (IOException e) {
                log(Level.ERROR, "Failed to write to obs-overlay-template:\n" + ExceptionUtil.toDetailedString(e));
            }
        });
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    /**
     * Copies default obs-overlay-template from resources if it doesn't exist.
     */
    public static void createDefaultFile() {
        if (Files.exists(OBS_OVERLAY_TEMPLATE_PATH)) return;

        try {
            Files.copy(Objects.requireNonNull(OBSOverlayGUI.class.getResourceAsStream("/obs-overlay-template")), OBS_OVERLAY_TEMPLATE_PATH);
        } catch (IOException e) {
            log(Level.ERROR, "Failed to create obs-overlay-template:\n" + ExceptionUtil.toDetailedString(e));
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setEnabled(true);
        mainPanel.setMinimumSize(new Dimension(450, 225));
        mainPanel.setPreferredSize(new Dimension(450, 250));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        folderPathTextPane = new JTextPane();
        folderPathTextPane.setEditable(false);
        folderPathTextPane.setEnabled(true);
        folderPathTextPane.setMaximumSize(new Dimension(250, 2147483647));
        folderPathTextPane.setMinimumSize(new Dimension(250, 22));
        folderPathTextPane.setPreferredSize(new Dimension(300, 22));
        folderPathTextPane.setText("folder path");
        panel1.add(folderPathTextPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        copyFilePathButton = new JButton();
        copyFilePathButton.setText("Copy File Path");
        panel1.add(copyFilePathButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("OBS Overlay file:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.add(panel2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Overlay template:");
        panel2.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        overlayFormatTextArea = new JTextArea();
        overlayFormatTextArea.setRows(7);
        overlayFormatTextArea.setText("enters: %enters%\nnph: %nph%\naverage: %average%");
        scrollPane1.setViewportView(overlayFormatTextArea);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.add(panel4, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        updateOverlayFormatButton = new JButton();
        updateOverlayFormatButton.setText("Update overlay format");
        panel4.add(updateOverlayFormatButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
