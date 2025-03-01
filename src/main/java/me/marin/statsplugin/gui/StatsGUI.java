package me.marin.statsplugin.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lombok.Getter;
import me.marin.statsplugin.util.StatsPluginUtil;
import me.marin.statsplugin.io.StatsPluginSettings;
import me.marin.statsplugin.stats.Session;
import me.marin.statsplugin.util.UpdateUtil;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.util.ExceptionUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

import static me.marin.statsplugin.StatsPlugin.*;

public class StatsGUI extends JPanel {

    @Getter
    private JPanel mainPanel;
    private JCheckBox trackerEnabledCheckbox;
    private JButton setupButton;
    private JButton openSettingsJson;
    private JButton testGoogleSheetsConnectionButton;
    private JButton openGoogleSheetButton;
    private JButton openStatsBrowserButton;
    private JButton reloadSettingsButton;
    private JButton OBSOverlayButton;
    private JButton startANewSessionButton;
    private JPanel setupPanel;
    private JPanel settingsPanel;
    private JCheckBox simulateOfflineCheckbox;
    private JButton checkForUpdatesButton;
    private JPanel checkForUpdatesPanel;

    private OBSOverlayGUI obsOverlayGUI;
    private SetupGUI setupGUI;

    public StatsGUI() {
        $$$setupUI$$$();
        this.setVisible(true);
        this.add(mainPanel);

        updateGUI();

        trackerEnabledCheckbox.addActionListener(e -> {
            toggleTracker();
        });

        setupButton.addActionListener(a -> {
            if (setupGUI == null || setupGUI.isClosed()) {
                setupGUI = new SetupGUI();
            } else {
                setupGUI.requestFocus();
            }
        });

        openSettingsJson.addActionListener(a -> {
            try {
                Desktop.getDesktop().open(STATS_SETTINGS_PATH.toFile());
            } catch (IOException e) {
                log(Level.ERROR, "Failed to open settings.json:\n" + ExceptionUtil.toDetailedString(e));
            }
        });

        openGoogleSheetButton.addActionListener(a -> {
            try {
                String sheetLink = StatsPluginSettings.getInstance().sheetLink;
                if (sheetLink == null) {
                    JOptionPane.showMessageDialog(null, "You haven't set up a Google Sheet link in settings.json!");
                    return;
                }
                Desktop.getDesktop().browse(URI.create(sheetLink));
            } catch (Exception e) {
                log(Level.ERROR, "Failed to open sheets:\n" + ExceptionUtil.toDetailedString(e));
            }
        });

        openStatsBrowserButton.addActionListener(a -> {
            try {
                String sheetLink = StatsPluginSettings.getInstance().sheetLink;
                if (sheetLink == null) {
                    JOptionPane.showMessageDialog(null, "You haven't set up a Google Sheet link in settings.json!");
                    return;
                }
                String sheetsID = StatsPluginUtil.extractGoogleSheetsID(sheetLink);
                Desktop.getDesktop().browse(URI.create("https://reset-analytics.vercel.app/sheet/" + sheetsID));
            } catch (Exception e) {
                log(Level.ERROR, "Failed to open sheets:\n" + ExceptionUtil.toDetailedString(e));
            }
        });

        testGoogleSheetsConnectionButton.addActionListener(a -> {
            testGoogleSheetsConnectionButton.setEnabled(false);
            StatsPluginUtil.runAsync("google-sheets-connection", () -> {
                boolean connected = reloadGoogleSheets();
                if (!connected) {
                    JOptionPane.showMessageDialog(null, "Could not connect to Google Sheets. Check Jingle logs for help.");
                } else {
                    JOptionPane.showMessageDialog(null, "Connection to Google Sheets successful!");
                }
                testGoogleSheetsConnectionButton.setEnabled(true);
            });
        });

        reloadSettingsButton.addActionListener(a -> {
            StatsPluginSettings.load();
            updateGUI();
            log(Level.INFO, "Reloaded settings.");
            JOptionPane.showMessageDialog(null, "Reloaded settings.");
        });

        OBSOverlayButton.addActionListener(a -> {
            if (obsOverlayGUI == null || obsOverlayGUI.isClosed()) {
                obsOverlayGUI = new OBSOverlayGUI();
            } else {
                obsOverlayGUI.requestFocus();
            }
        });

        /* moved to the custom create method
        startANewSessionButton.addActionListener(a -> {
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to start a new session?", "New session", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                CURRENT_SESSION = new Session();
                CURRENT_SESSION.updateOverlay();
                log(Level.INFO, "Started a new session!");
                JOptionPane.showMessageDialog(null, "New session started.");
            }
        });*/

        checkForUpdatesButton.addActionListener(a -> {
            UpdateUtil.checkForUpdatesAndUpdate(false);
        });
    }

    public void updateGUI() {
        trackerEnabledCheckbox.setSelected(StatsPluginSettings.getInstance().trackerEnabled);
        if (toggleTrackerButton != null) {
            toggleTrackerButton.setText(StatsPluginSettings.getInstance().trackerEnabled ? DISABLE_TRACKER : ENABLE_TRACKER);
        }

        settingsPanel.setVisible(StatsPluginSettings.getInstance().completedSetup);
        checkForUpdatesPanel.setVisible(StatsPluginSettings.getInstance().completedSetup);
        setupPanel.setVisible(!StatsPluginSettings.getInstance().completedSetup);

        openStatsBrowserButton.setVisible(StatsPluginSettings.getInstance().useSheets);
        openGoogleSheetButton.setVisible(StatsPluginSettings.getInstance().useSheets);
        testGoogleSheetsConnectionButton.setVisible(StatsPluginSettings.getInstance().useSheets);
    }

    public JButton createStartNewSessionButton() {
        JButton button = new JButton("Start new session");
        button.addActionListener(a -> {
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to start a new session?", "Stats Plugin - new session", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                CURRENT_SESSION = new Session();
                CURRENT_SESSION.updateOverlay();
                log(Level.INFO, "Started a new session.");
                JOptionPane.showMessageDialog(null, "New session started.");
            }
        });
        return button;
    }

    private static final String ENABLE_TRACKER = "Enable Stats";
    private static final String DISABLE_TRACKER = "Disable Stats";
    private static JButton toggleTrackerButton;

    public JButton createToggleTrackerButton() {
        toggleTrackerButton = new JButton(StatsPluginSettings.getInstance().trackerEnabled ? DISABLE_TRACKER : ENABLE_TRACKER);
        toggleTrackerButton.addActionListener(a -> {
            toggleTracker();
        });
        return toggleTrackerButton;
    }

    private void toggleTracker() {
        StatsPluginSettings settings = StatsPluginSettings.getInstance();
        settings.trackerEnabled = !settings.trackerEnabled;
        StatsPluginSettings.save();
        updateGUI();
        log(Level.INFO, settings.trackerEnabled ? "Now tracking stats." : "No longer tracking stats.");
    }

    // Run in IntelliJ to force it to update GUI code
    public static void main(String[] args) {
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.setAlignmentX(0.5f);
        mainPanel.setAutoscrolls(false);
        mainPanel.setEnabled(true);
        mainPanel.setPreferredSize(new Dimension(500, 250));
        mainPanel.setRequestFocusEnabled(true);
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        setupPanel = new JPanel();
        setupPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(setupPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        setupButton = new JButton();
        setupButton.setText("Setup Tracker");
        setupPanel.add(setupButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Complete the quick Setup so you can start using the tracker.");
        setupPanel.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkForUpdatesPanel = new JPanel();
        checkForUpdatesPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(checkForUpdatesPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkForUpdatesButton = new JButton();
        checkForUpdatesButton.setText("Check for updates");
        checkForUpdatesPanel.add(checkForUpdatesButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        settingsPanel.setVisible(true);
        mainPanel.add(settingsPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 3, new Insets(5, 0, 5, 0), -1, -1));
        settingsPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        OBSOverlayButton = new JButton();
        OBSOverlayButton.setText("Configure OBS overlay");
        panel1.add(OBSOverlayButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Utility:");
        panel1.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openStatsBrowserButton = new JButton();
        openStatsBrowserButton.setText("View stats");
        openStatsBrowserButton.setVisible(true);
        panel1.add(openStatsBrowserButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openGoogleSheetButton = new JButton();
        openGoogleSheetButton.setText("Open Google Sheet");
        openGoogleSheetButton.setVisible(true);
        panel1.add(openGoogleSheetButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startANewSessionButton.setText("Start a new session");
        panel1.add(startANewSessionButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        settingsPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testGoogleSheetsConnectionButton = new JButton();
        testGoogleSheetsConnectionButton.setText("Test Google Sheets connection");
        panel2.add(testGoogleSheetsConnectionButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(5, 0, 5, 0), -1, -1));
        settingsPanel.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Settings:");
        panel3.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        openSettingsJson = new JButton();
        openSettingsJson.setActionCommand("Open settings.json");
        openSettingsJson.setLabel("Edit file manually");
        openSettingsJson.setText("Edit file manually");
        panel4.add(openSettingsJson, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        trackerEnabledCheckbox = new JCheckBox();
        trackerEnabledCheckbox.setText("Enable tracker?");
        trackerEnabledCheckbox.setVisible(true);
        panel4.add(trackerEnabledCheckbox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        simulateOfflineCheckbox = new JCheckBox();
        simulateOfflineCheckbox.setText("Simulate offline?");
        simulateOfflineCheckbox.setVisible(false);
        panel4.add(simulateOfflineCheckbox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        reloadSettingsButton = new JButton();
        reloadSettingsButton.setText("Reload settings");
        panel4.add(reloadSettingsButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private void createUIComponents() {
        startANewSessionButton = createStartNewSessionButton();
    }
}
