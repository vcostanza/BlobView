package software.blob.ui.view.layout;

import software.blob.ui.view.AttributeSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Group of radio buttons
 */
public class RadioGroup extends LinearLayout implements ActionListener {

    public RadioGroup() {
        super(VERTICAL);
    }

    public RadioGroup(AttributeSet attrs) {
        super(attrs);
    }

    @Override
    public Component add(Component c) {
        if (c instanceof AbstractButton)
            ((AbstractButton) c).addActionListener(this);
        return super.add(c);
    }

    @Override
    public void add(Component c, Object constraints) {
        super.add(c, constraints);
        if (c instanceof AbstractButton)
            ((AbstractButton) c).addActionListener(this);
    }

    @Override
    public void remove(Component c) {
        super.remove(c);
        if (c instanceof AbstractButton)
            ((AbstractButton) c).removeActionListener(this);
    }

    /**
     * Get the checked radio button
     * @return Radio button or null if non-checked
     */
    public AbstractButton getCheckedButton() {
        for (Component c : getComponents()) {
            if (c instanceof AbstractButton && ((AbstractButton) c).isSelected())
                return (AbstractButton) c;
        }
        return null;
    }

    /**
     * Get the checked radio button name
     * @return Name of the radio button or null if non-checked
     */
    public String getCheckedName() {
        AbstractButton btn = getCheckedButton();
        return btn != null ? btn.getName() : null;
    }

    /**
     * Check a radio button by name
     * @param name Radio button name
     */
    public void check(String name) {
        for (Component c : getComponents()) {
            if (c instanceof AbstractButton) {
                AbstractButton rbBtn = (AbstractButton) c;
                rbBtn.setSelected(name != null && name.equals(c.getName()));
            }
        }
    }

    private static void checkRadioButton(AbstractButton btn) {
        Container parent = btn.getParent();
        for (Component c : parent.getComponents()) {
            if (c instanceof AbstractButton) {
                AbstractButton rbBtn = (AbstractButton) c;
                rbBtn.setSelected(btn == rbBtn);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Component c = (Component) e.getSource();
        if (c instanceof AbstractButton)
            checkRadioButton((AbstractButton) c);
    }
}
