package rov.fa13.transfers;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.impl.matchers.BeanPropertyMatcher;
import ca.odell.glazedlists.impl.matchers.TrueMatcher;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author MasterOfChaos
 */
public class BooleanMatcherEditor extends AbstractMatcherEditor {

    private JCheckBox editorBoolean;
    final private ChangeListener changeListener;
    private boolean editorValue=false;
    private Filterator filterator;
            
    public BooleanMatcherEditor(Filterator filterator, JCheckBox editorBoolean ) {
        this.filterator = filterator;
        this.editorBoolean = editorBoolean;
        changeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                editorValueChanged(e);
            }
        };
        editorBoolean.addChangeListener(changeListener);
    }

    private void editorValueChanged(ChangeEvent e) {
        editorValue = ((JCheckBox)e.getSource()).isSelected();
        if (!editorValue) {
            fireMatchAll();
        } else {
            final Matcher newMatcher = new BooleanMatcher(filterator); //put filterator !!!
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fireChanged(newMatcher);
                }
            };

            SwingUtilities.invokeLater(r);
        }
            
            
    }
    
    
}