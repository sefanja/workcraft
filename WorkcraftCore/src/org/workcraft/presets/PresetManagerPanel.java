package org.workcraft.presets;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

@SuppressWarnings("serial")
public class PresetManagerPanel<T> extends JPanel {
    private JComboBox presetCombo;

    private final PresetManager<T> presetManager;
    private final SettingsToControlsMapper<T> guiMapper;
    private JButton updatePresetButton;
    private final Window dialogOwner;

    @SuppressWarnings("unchecked")
    public PresetManagerPanel(PresetManager<T> presetManager, List<Preset<T>> builtIn, SettingsToControlsMapper<T> guiMapper, Window dialogOwner) {
        super();

        this.guiMapper = guiMapper;
        this.presetManager = presetManager;
        this.dialogOwner = dialogOwner;

        for (Preset<T> p : builtIn) {
            presetManager.add(p);
        }
        initialise();
    }

    private void initialise() {
        //presetManager.sort();
        presetCombo = new JComboBox();
        for (Preset<T> p : presetManager.list()) {
            presetCombo.addItem(p);
        }
        presetCombo.addActionListener(event -> {
            Preset<T> p = (Preset<T>) presetCombo.getSelectedItem();
            if (p == null) {
                return;
            }
            if (p.isBuiltIn()) {
                updatePresetButton.setEnabled(false);
                updatePresetButton.setToolTipText("Cannot make changes to a built-in preset");
            } else {
                updatePresetButton.setEnabled(true);
                updatePresetButton.setToolTipText("Save these settings to the currently selected preset");
            }

            T settings = p.getSettings();
            guiMapper.applySettingsToControls(settings);
        });

        JButton manageButton = new JButton("Manage...");
        manageButton.addActionListener(event -> {
            boolean haveCustomPresets = false;
            for (Preset<T> p : presetManager.list()) {
                if (!p.isBuiltIn()) {
                    haveCustomPresets = true;
                    break;
                }
            }
            if (haveCustomPresets) {
                managePresets();
            } else {
                JOptionPane.showMessageDialog(this, "There are no custom presets to manage.");
            }
        });

        updatePresetButton = new JButton("Update");
        updatePresetButton.addActionListener(event -> {
            Preset<T> selected = (Preset<T>) presetCombo.getSelectedItem();
            presetManager.update(selected, guiMapper.getSettingsFromControls());
        });

        JButton saveAsButton = new JButton("Save as...");
        saveAsButton.addActionListener(event -> createPreset());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(updatePresetButton);
        buttonsPanel.add(saveAsButton);
        buttonsPanel.add(manageButton);

        setBorder(SizeHelper.getTitledBorder("Presets"));
        setLayout(new BorderLayout(SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        add(presetCombo, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    @SuppressWarnings("unchecked")
    public void managePresets() {
        Preset<T> selected = (Preset<T>) presetCombo.getSelectedItem();

        PresetManagerDialog<T> dlg = new PresetManagerDialog<>(dialogOwner, presetManager);
        dlg.setModalityType(ModalityType.APPLICATION_MODAL);
        GuiUtils.centerAndSizeToParent(dlg, dialogOwner);
        dlg.setVisible(true);

        presetCombo.removeAllItems();
        List<Preset<T>> presets = presetManager.list();

        boolean haveOldSelection = false;

        for (Preset<T> p : presets) {
            presetCombo.addItem(p);
            if (p == selected) {
                haveOldSelection = true;
            }
        }

        if (haveOldSelection) {
            presetCombo.setSelectedItem(selected);
        } else {
            presetCombo.setSelectedIndex(0);
        }
    }

    public void createPreset() {
        String desc = DialogUtils.showInput("Please enter the description of the new preset:", "");
        if (!(desc == null || desc.isEmpty())) {
            T settings = guiMapper.getSettingsFromControls();
            Preset<T> preset = presetManager.save(settings, desc);
            presetCombo.addItem(preset);
            presetCombo.setSelectedItem(preset);
        }
    }

    public void selectFirst() {
        if (presetCombo.getItemCount() > 0) {
            presetCombo.setSelectedIndex(0);
        }
    }
}
