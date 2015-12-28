package rov.fa13.transfers;

import ca.odell.glazedlists.impl.matchers.RangeMatcher;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import rov.CU;

/**
 *
 * @author Oleg Rachaev
 */
public class PlayerAbilitiesMatcherEditor extends AbstractMatcherEditor {

    private String abilityName;
    private JSpinner editorMin, editorMax;
    final private ChangeListener editorMinListener, editorMaxListener;
    private int min = 20, max = 199;
    final private PlayerAbilitiesFilterator filterator;

    public PlayerAbilitiesMatcherEditor(String abilityName, JSpinner editorMin, JSpinner editorMax) {
        editorMinListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                minChanged(e);
            }
        };
        editorMaxListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                maxChanged(e);
            }
        };

        filterator = new PlayerAbilitiesFilterator(abilityName);
        this.abilityName = abilityName;
        this.editorMin = editorMin;
        this.editorMax = editorMax;

        editorMin.addChangeListener(editorMinListener);
        editorMax.addChangeListener(editorMaxListener);
    }

    public JSpinner getEditorMax() {
        return editorMax;
    }

    public void setEditorMax(JSpinner editorMax) {
        this.editorMax = editorMax;
    }

    public JSpinner getEditorMin() {
        return editorMin;
    }

    public void setEditorMin(JSpinner editorMin) {
        this.editorMin = editorMin;
    }

    private void minChanged(ChangeEvent e) {
        min = CU.getInt((JSpinner) e.getSource(), 20);

        if ((min < 21) && (max > 198)) {
            fireMatchAll();
        } else {
            final RangeMatcher newMatcher = new RangeMatcher(min, max, filterator);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fireChanged(newMatcher);
                }
            };

            SwingUtilities.invokeLater(r);
        }
    }

    private void maxChanged(ChangeEvent e) {
        max = CU.getInt((JSpinner) e.getSource(), 20);
        if ((min < 21) && (max > 198)) {
            fireMatchAll();
        } else {
            final RangeMatcher newMatcher = new RangeMatcher(min, max, filterator);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fireChanged(newMatcher);
                }
            };

            SwingUtilities.invokeLater(r);
        }
    }

    /*
    private String abilities;
    //filter
    private JSpinner shootingMin, shootingMax, passingMin, passingMax, dribblingMin, dribblingMax, speedMin, speedMax, staminaMin, staminaMax, crossingMin, crossingMax, headingMin, headingMax, handlingMin, handlingMax, reflexesMin, reflexesMax, tacklingMin, tacklingMax;
    final private ChangeListener shootingMin1, shootingMax1, passingMin1, passingMax1, dribblingMin1, dribblingMax1, speedMin1, speedMax1, staminaMin1, staminaMax1, crossingMin1, crossingMax1, headingMin1, headingMax1, handlingMin1, handlingMax1, reflexesMin1, reflexesMax1, tacklingMin1, tacklingMax1;
    //final private RangeMatcher shootingMin2, shootingMax2, passingMin2, passingMax2, dribblingMin2, dribblingMax2, speedMin2, speedMax2, staminaMin2, staminaMax2, crossingMin2, crossingMax2, headingMin2, headingMax2, handlingMin2, handlingMax2, reflexesMin2, reflexesMax2, tacklingMin2, tacklingMax2;
    private PlayerAbilitiesMatcher currentMatcher;
    final PlayerAbilitiesFilterator shootingFilterator, passingFilterator, dribblingFilterator, speedFilterator, staminaFilterator, crossingFilterator, headingFilterator, handlingFilterator, reflexesFilterator, tacklingFilterator;

    public PlayerAbilitiesMatcher getCurrentMatcher() {
    if (currentMatcher == null) {
    currentMatcher = new PlayerAbilitiesMatcher();
    }
    return currentMatcher;
    }

    public PlayerAbilitiesMatcherEditor() {
    shootingMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    shootingMinChanged(e);
    }
    };

    shootingMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    shootingMaxChanged(e);
    }
    };
    passingMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    passingMinChanged(e);
    }
    };
    passingMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    passingMaxChanged(e);
    }
    };
    dribblingMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    dribblingMinChanged(e);
    }
    };
    dribblingMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    dribblingMaxChanged(e);
    }
    };
    speedMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    speedMinChanged(e);
    }
    };
    speedMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    speedMaxChanged(e);
    }
    };
    staminaMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    staminaMinChanged(e);
    }
    };
    staminaMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    staminaMaxChanged(e);
    }
    };
    crossingMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    crossingMinChanged(e);
    }
    };
    crossingMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    crossingMaxChanged(e);
    }
    };
    headingMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    headingMinChanged(e);
    }
    };
    headingMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    headingMaxChanged(e);
    }
    };
    handlingMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    handlingMinChanged(e);
    }
    };
    handlingMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    handlingMaxChanged(e);
    }
    };
    reflexesMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    reflexesMinChanged(e);
    }
    };
    reflexesMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    reflexesMaxChanged(e);
    }
    };
    tacklingMin1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    tacklingMinChanged(e);
    }
    };
    tacklingMax1 = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
    tacklingMaxChanged(e);
    }
    };
    shootingFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("U"));

    passingFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("P"));
    dribblingFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("D"));
    speedFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("S"));
    staminaFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("F"));
    crossingFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("N"));
    headingFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("VG"));
    handlingFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("T"));
    reflexesFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("R"));
    tacklingFilterator = new PlayerAbilitiesFilterator(Fa13Utils.getRes("O"));

    }

    private void shootingMinChanged(ChangeEvent e) {
    int val = CU.getInt((JSpinner) e.getSource(), 20);
    PlayerAbilitiesMatcher newMatcher = new PlayerAbilitiesMatcher();
    try {
    newMatcher = (PlayerAbilitiesMatcher) getCurrentMatcher().clone();
    //add new
    fireChanged(newMatcher);
    } catch (CloneNotSupportedException ex) {
    Exceptions.printStackTrace(ex);
    }

    }

    private void shootingMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void passingMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void passingMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void crossingMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void crossingMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void dribblingMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void dribblingMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void tacklingMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void tacklingMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void speedMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void speedMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void staminaMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void staminaMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void headingMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void headingMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handlingMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handlingMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void reflexesMinChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    private void reflexesMaxChanged(ChangeEvent e) {
    throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getAbilities() {
    return abilities;
    }

    public void setAbilities(String abilities) {
    this.abilities = abilities;
    }

    public JSpinner getCrossingMax() {
    return crossingMax;
    }

    public void setCrossingMax(JSpinner crossingMax) {
    JSpinner old = this.crossingMax;
    if ((old != null) && (old != crossingMax)) {
    this.crossingMax = crossingMax;
    old.removeChangeListener(crossingMax1);
    crossingMax.addChangeListener(crossingMax1);
    }
    }

    public JSpinner getCrossingMin() {
    return crossingMin;
    }

    public void setCrossingMin(JSpinner crossingMin) {
    JSpinner old = this.crossingMin;
    if ((old != null) && (old != crossingMin)) {
    this.crossingMin = crossingMin;
    old.removeChangeListener(crossingMin1);
    crossingMin.addChangeListener(crossingMin1);
    }
    }

    public JSpinner getDribblingMax() {
    return dribblingMax;
    }

    public void setDribblingMax(JSpinner dribblingMax) {
    JSpinner old = this.dribblingMax;
    if ((old != null) && (old != dribblingMax)) {
    this.dribblingMax = dribblingMax;
    old.removeChangeListener(dribblingMax1);
    dribblingMax.addChangeListener(dribblingMax1);
    }
    }

    public JSpinner getDribblingMin() {
    return dribblingMin;
    }

    public void setDribblingMin(JSpinner dribblingMin) {
    JSpinner old = this.dribblingMin;
    if ((old != null) && (old != dribblingMin)) {
    this.dribblingMin = dribblingMin;
    old.removeChangeListener(dribblingMin1);
    dribblingMin.addChangeListener(dribblingMin1);
    }
    }

    public JSpinner getHandlingMax() {
    return handlingMax;
    }

    public void setHandlingMax(JSpinner handlingMax) {
    JSpinner old = this.handlingMax;
    if ((old != null) && (old != handlingMax)) {
    this.handlingMax = handlingMax;
    old.removeChangeListener(handlingMax1);
    handlingMax.addChangeListener(handlingMax1);
    }
    }

    public JSpinner getHandlingMin() {
    return handlingMin;
    }

    public void setHandlingMin(JSpinner handlingMin) {
    JSpinner old = this.handlingMin;
    if ((old != null) && (old != handlingMin)) {
    this.handlingMin = handlingMin;
    old.removeChangeListener(handlingMin1);
    handlingMin.addChangeListener(handlingMin1);
    }
    }

    public JSpinner getHeadingMax() {
    return headingMax;
    }

    public void setHeadingMax(JSpinner headingMax) {
    JSpinner old = this.headingMax;
    if ((old != null) && (old != headingMax)) {
    this.headingMax = headingMax;
    old.removeChangeListener(headingMax1);
    headingMax.addChangeListener(headingMax1);
    }
    }

    public JSpinner getHeadingMin() {
    return headingMin;
    }

    public void setHeadingMin(JSpinner headingMin) {
    JSpinner old = this.headingMin;
    if ((old != null) && (old != headingMin)) {
    this.headingMin = headingMin;
    old.removeChangeListener(headingMin1);
    headingMin.addChangeListener(headingMin1);
    }
    }

    public JSpinner getPassingMax() {
    return passingMax;
    }

    public void setPassingMax(JSpinner passingMax) {
    JSpinner old = this.passingMax;
    if ((old != null) && (old != passingMax)) {
    this.passingMax = passingMax;
    old.removeChangeListener(passingMax1);
    passingMax.addChangeListener(passingMax1);
    }
    }

    public JSpinner getPassingMin() {
    return passingMin;
    }

    public void setPassingMin(JSpinner passingMin) {
    JSpinner old = this.passingMin;
    if ((old != null) && (old != passingMin)) {
    this.passingMin = passingMin;
    old.removeChangeListener(passingMin1);
    passingMin.addChangeListener(passingMin1);
    }
    }

    public JSpinner getReflexesMax() {
    return reflexesMax;
    }

    public void setReflexesMax(JSpinner reflexesMax) {
    JSpinner old = this.reflexesMax;
    if ((old != null) && (old != crossingMax)) {
    this.reflexesMax = reflexesMax;
    old.removeChangeListener(reflexesMax1);
    reflexesMax.addChangeListener(reflexesMax1);
    }
    }

    public JSpinner getReflexesMin() {
    return reflexesMin;
    }

    public void setReflexesMin(JSpinner reflexesMin) {
    JSpinner old = this.reflexesMin;
    if ((old != null) && (old != reflexesMin)) {
    this.reflexesMin = reflexesMin;
    old.removeChangeListener(reflexesMin1);
    reflexesMin.addChangeListener(reflexesMin1);
    }
    }

    public JSpinner getShootingMax() {
    return shootingMax;
    }

    public void setShootingMax(JSpinner shootingMax) {
    JSpinner old = this.shootingMax;
    if ((old != null) && (old != shootingMax)) {
    this.shootingMax = shootingMax;
    old.removeChangeListener(shootingMax1);
    shootingMax.addChangeListener(shootingMax1);
    }
    }

    public JSpinner getShootingMin() {
    return shootingMin;
    }

    public void setShootingMin(JSpinner shootingMin) {
    JSpinner old = this.shootingMin;
    if ((old != null) && (old != shootingMin)) {
    this.shootingMin = shootingMin;
    old.removeChangeListener(shootingMin1);
    shootingMin.addChangeListener(shootingMin1);
    }
    }

    public JSpinner getSpeedMax() {
    return speedMax;
    }

    public void setSpeedMax(JSpinner speedMax) {
    JSpinner old = this.speedMax;
    if ((old != null) && (old != crossingMax)) {
    this.speedMax = speedMax;
    old.removeChangeListener(speedMax1);
    speedMax.addChangeListener(speedMax1);
    }
    }

    public JSpinner getSpeedMin() {
    return speedMin;
    }

    public void setSpeedMin(JSpinner speedMin) {
    JSpinner old = this.speedMin;
    if ((old != null) && (old != speedMin)) {
    this.speedMin = speedMin;
    old.removeChangeListener(speedMin1);
    speedMin.addChangeListener(speedMin1);
    }
    }

    public JSpinner getStaminaMax() {
    return staminaMax;
    }

    public void setStaminaMax(JSpinner staminaMax) {
    JSpinner old = this.staminaMax;
    if ((old != null) && (old != staminaMax)) {
    this.staminaMax = staminaMax;
    old.removeChangeListener(staminaMax1);
    staminaMax.addChangeListener(staminaMax1);
    }
    }

    public JSpinner getStaminaMin() {
    return staminaMin;
    }

    public void setStaminaMin(JSpinner staminaMin) {
    JSpinner old = this.staminaMin;
    if ((old != null) && (old != staminaMin)) {
    this.staminaMin = staminaMin;
    old.removeChangeListener(staminaMin1);
    staminaMin.addChangeListener(staminaMin1);
    }
    }

    public JSpinner getTacklingMax() {
    return tacklingMax;
    }

    public void setTacklingMax(JSpinner tacklingMax) {
    JSpinner old = this.tacklingMax;
    if ((old != null) && (old != tacklingMax)) {
    this.tacklingMax = tacklingMax;
    old.removeChangeListener(tacklingMax1);
    tacklingMax.addChangeListener(tacklingMax1);
    }
    }

    public JSpinner getTacklingMin() {
    return tacklingMin;
    }

    public void setTacklingMin(JSpinner tacklingMin) {
    JSpinner old = this.tacklingMin;
    if ((old != null) && (old != tacklingMin)) {
    this.tacklingMin = tacklingMin;
    old.removeChangeListener(tacklingMin1);
    tacklingMin.addChangeListener(tacklingMin1);
    }
    }

    @Override
    public Matcher getMatcher() {
    return super.getMatcher();
    }
     */
}
