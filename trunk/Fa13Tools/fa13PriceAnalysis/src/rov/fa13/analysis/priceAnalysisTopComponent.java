package rov.fa13.analysis;

import ca.odell.glazedlists.EventList;
import com.fa13.build.model.All;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import rov.chart.utils.CSerieControl;
import rov.chart.utils.ChartUtils;
import rov.fa13.transfers.MyTransferPlayer;
import rov.rcp.utils.CentralLookup;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//rov.fa13.analysis//priceAnalysis//EN",
autostore = false)
public final class priceAnalysisTopComponent extends TopComponent {

    private static priceAnalysisTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "priceAnalysisTopComponent";
    private ArrayList<CSerieControl> listSeries = new ArrayList<CSerieControl>();
    private JFreeChart chartXY;
    private XYSeriesCollection dataSetXY;
    private MyTransferPlayer transferPlayer;
    private All all;
    private Lookup.Result<MyTransferPlayer> playerLookupResult;
    private Lookup.Result<EventList<MyTransferPlayer>> listPlayersLookupResult;
    private LookupListener listPlayersListener;
    private LookupListener playerListener;
    private int[] age;
    double[] price;
    final private ArrayList<MyTransferPlayer> listCalcPlayers = new ArrayList<MyTransferPlayer>();
    private EventList<MyTransferPlayer> listTransferPlayers;

    public priceAnalysisTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(priceAnalysisTopComponent.class, "CTL_priceAnalysisTopComponent"));
        setToolTipText(NbBundle.getMessage(priceAnalysisTopComponent.class, "HINT_priceAnalysisTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        initCharts();

    }

    private void playerChanged(LookupEvent ev) {
        //Lookup.Result r = (Lookup.Result) ev.getSource();
        //Collection<MyTransferPlayer> coll = r.allInstances();

        Collection<MyTransferPlayer> coll = (Collection<MyTransferPlayer>) playerLookupResult.allInstances();
        if (!coll.isEmpty()) {
            for (MyTransferPlayer v : coll) {
                setTransferPlayer(v);
            }
        } else {
            //jTextField1.setText("[no name]");
            //jTextField2.setText("[no city]");
        }
        transferPlayer = CentralLookup.getDef().lookup(MyTransferPlayer.class);
    }

    private void listPlayersChanged(LookupEvent ev) {
        //if (listTransferPlayers != null) return;
        //Lookup.Result r = (Lookup.Result) ev.getSource();
        //Collection<EventList<MyTransferPlayer>> coll = r.allInstances();
        Collection<EventList<MyTransferPlayer>> coll = (Collection<EventList<MyTransferPlayer>>) listPlayersLookupResult.allInstances();
        if (!coll.isEmpty()) {
            for (EventList<MyTransferPlayer> v : coll) {
                setListTransferPlayers(v);
            }
        } else {
            //jTextField1.setText("[no name]");
            //jTextField2.setText("[no city]");
        }
    }

    public EventList<MyTransferPlayer> getListTransferPlayers() {
        return listTransferPlayers;
    }

    public void setListTransferPlayers(EventList<MyTransferPlayer> listTransferPlayers) {

        Object old = this.listTransferPlayers;
        if (old != listTransferPlayers) {
            this.listTransferPlayers = listTransferPlayers;
            recalcPriceDynamic();
        }

    }

    private void initSettingsFromUI() {
        MyTransferPlayer.setAgeDiff(Integer.valueOf(S_ageDiff.getValue().toString()));
        MyTransferPlayer.setTalDiff(Integer.valueOf(S_talDiff.getValue().toString()));
        MyTransferPlayer.setStrDiff(Integer.valueOf(S_strDiff.getValue().toString()));
        MyTransferPlayer.setExpDiff(Integer.valueOf(S_expDiff.getValue().toString()));
    }

    private void recalcPriceDynamic() {
        if (!isDisplayable()) {
            return;
        }
        if ((listTransferPlayers == null) || (transferPlayer == null)) {
            return;
        }
        initSettingsFromUI();
        listTransferPlayers.getReadWriteLock().readLock().lock();
        int originalAge = transferPlayer.getAge();
        int originalExp = transferPlayer.getExperience();
        int originalStr = transferPlayer.getStrength();
        int max_year = 40;
        int size = max_year - originalAge;
        if (size < 0) {
            max_year = max_year - size + 1;
            size = max_year - originalAge;//second try
        }
        double price1;
        age = new int[size];
        price = new double[size];
        int add_sv = Integer.valueOf(S_svAdd.getValue().toString());
        int add_exp = Integer.valueOf(S_expAdd.getValue().toString());
        int add_str = Integer.valueOf(S_strAdd.getValue().toString());
        transferPlayer.resetSumAbilitiesCalc();//!!! fix = reset previos SV dynamics
        for (int age1 = originalAge; age1 < max_year; age1++) {
            listCalcPlayers.clear();
            transferPlayer.setAge(age1);
            transferPlayer.setSumAbilitiesCalc(transferPlayer.getSumAbilitiesCalc() + add_sv);
            transferPlayer.setExperience(transferPlayer.getExperience() + add_exp);
            //limit strenght with talent!!!
            if (transferPlayer.getStrength() < transferPlayer.getTalent() - 3) {
                transferPlayer.setStrength(transferPlayer.getStrength() + add_str);
            }
            if (age1 == originalAge) {
                price1 = transferPlayer.getPrice();
            } else {
                for (MyTransferPlayer tp : listTransferPlayers) {
                    if ((transferPlayer != tp) && (transferPlayer.isPlayerSimilarForTransfer(tp))) {
                        listCalcPlayers.add(tp);
                    }
                }
                price1 = MyTransferPlayer.getPredictionPrice(transferPlayer, listCalcPlayers);
            }
            age[age1 - originalAge] = age1;
            price[age1 - originalAge] = price1;
        }
        listTransferPlayers.getReadWriteLock().readLock().unlock();
        transferPlayer.setAge(originalAge);
        transferPlayer.setExperience(originalExp);
        transferPlayer.setStrength(originalStr);
        updateSeries();
    }

    private void updateSeries() {
        if (listSeries == null) {
            return;
        }
        chartXY.setTitle(getRes("PLAYER_PRICE_PROGNOZ")
                + " [" + transferPlayer.getName() + " -> " + transferPlayer.getAge()
                + "/" + transferPlayer.getBirthtour()
                + "/" + transferPlayer.getTalent()
                + "/" + transferPlayer.getStrength()
                + "/" + transferPlayer.getAbilities()
                + "]  " + transferPlayer.getSumAbilities());// + getRes("COUNT_ANALOG_PLAYERS") + " = "+listCalcPlayers.size() );
        XYSeries s_price = ChartUtils.getXYSerieByName(listSeries, "AGE_PRICE");
        if (s_price != null) {
            s_price.clear();
            s_price.setNotify(false);
        }

        for (int i = 0; i < age.length; i++) {
            if (s_price != null) {
                if (price[i] > 0.1) {
                    ChartUtils.updateSerie(s_price, age[i], price[i]);
                }
            }
        }
        if (s_price != null) {
            s_price.setNotify(true);
        }

    }

    public void updateGrafSettings(String group) {
        if (listSeries == null) {
            return;
        }
        for (CSerieControl cs : listSeries) {
            if (cs.getGroup().equals(group)) {
                cs.applySettings();
            }
        }
        // somehow bug happens
        // renderer apply setting to exact indexes, that defined earlier
        // so it doesnt matter about changes in list of series(add/remove)
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        P_chart = new javax.swing.JPanel();
        P_diffSetting1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        S_svAdd = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        S_expAdd = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        S_strAdd = new javax.swing.JSpinner();
        P_diffSetting = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        S_ageDiff = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        S_talDiff = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        S_strDiff = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        S_expDiff = new javax.swing.JSpinner();

        setLayout(new java.awt.GridBagLayout());

        P_chart.setBorder(null);
        P_chart.setPreferredSize(new java.awt.Dimension(20, 20));
        P_chart.setLayout(new javax.swing.BoxLayout(P_chart, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(P_chart, gridBagConstraints);

        P_diffSetting1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.P_diffSetting1.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("sansserif", 3, 10))); // NOI18N
        P_diffSetting1.setMaximumSize(new java.awt.Dimension(243, 52));
        P_diffSetting1.setMinimumSize(new java.awt.Dimension(243, 52));
        P_diffSetting1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 1));

        jLabel5.setFont(new java.awt.Font("sansserif", 1, 10));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.jLabel5.text")); // NOI18N
        P_diffSetting1.add(jLabel5);

        S_svAdd.setFont(new java.awt.Font("sansserif", 1, 10)); // NOI18N
        S_svAdd.setModel(new javax.swing.SpinnerNumberModel());
        S_svAdd.setPreferredSize(new java.awt.Dimension(48, 24));
        S_svAdd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_svAddStateChanged(evt);
            }
        });
        P_diffSetting1.add(S_svAdd);

        jLabel6.setFont(new java.awt.Font("sansserif", 1, 10));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.jLabel6.text")); // NOI18N
        P_diffSetting1.add(jLabel6);

        S_expAdd.setFont(new java.awt.Font("sansserif", 1, 10)); // NOI18N
        S_expAdd.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        S_expAdd.setPreferredSize(new java.awt.Dimension(48, 24));
        S_expAdd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_expAddStateChanged(evt);
            }
        });
        P_diffSetting1.add(S_expAdd);

        jLabel7.setFont(new java.awt.Font("sansserif", 1, 10));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.jLabel7.text")); // NOI18N
        P_diffSetting1.add(jLabel7);

        S_strAdd.setFont(new java.awt.Font("sansserif", 1, 10)); // NOI18N
        S_strAdd.setModel(new javax.swing.SpinnerNumberModel());
        S_strAdd.setPreferredSize(new java.awt.Dimension(48, 24));
        S_strAdd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_strAddStateChanged(evt);
            }
        });
        P_diffSetting1.add(S_strAdd);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(P_diffSetting1, gridBagConstraints);

        P_diffSetting.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.P_diffSetting.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("sansserif", 3, 10))); // NOI18N
        P_diffSetting.setMaximumSize(new java.awt.Dimension(356, 52));
        P_diffSetting.setMinimumSize(new java.awt.Dimension(356, 52));
        P_diffSetting.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 1));

        jLabel1.setFont(new java.awt.Font("sansserif", 1, 10));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.jLabel1.text")); // NOI18N
        P_diffSetting.add(jLabel1);

        S_ageDiff.setFont(new java.awt.Font("sansserif", 1, 10));
        S_ageDiff.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(0), null, Integer.valueOf(1)));
        S_ageDiff.setPreferredSize(new java.awt.Dimension(48, 24));
        S_ageDiff.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_ageDiffStateChanged(evt);
            }
        });
        P_diffSetting.add(S_ageDiff);

        jLabel2.setFont(new java.awt.Font("sansserif", 1, 10));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.jLabel2.text")); // NOI18N
        P_diffSetting.add(jLabel2);

        S_talDiff.setFont(new java.awt.Font("sansserif", 1, 10));
        S_talDiff.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), Integer.valueOf(0), null, Integer.valueOf(1)));
        S_talDiff.setPreferredSize(new java.awt.Dimension(48, 24));
        S_talDiff.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_talDiffStateChanged(evt);
            }
        });
        P_diffSetting.add(S_talDiff);

        jLabel3.setFont(new java.awt.Font("sansserif", 1, 10));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.jLabel3.text")); // NOI18N
        P_diffSetting.add(jLabel3);

        S_strDiff.setFont(new java.awt.Font("sansserif", 1, 10));
        S_strDiff.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), Integer.valueOf(0), null, Integer.valueOf(1)));
        S_strDiff.setPreferredSize(new java.awt.Dimension(48, 24));
        S_strDiff.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_strDiffStateChanged(evt);
            }
        });
        P_diffSetting.add(S_strDiff);

        jLabel4.setFont(new java.awt.Font("sansserif", 1, 10));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(priceAnalysisTopComponent.class, "priceAnalysisTopComponent.jLabel4.text")); // NOI18N
        P_diffSetting.add(jLabel4);

        S_expDiff.setFont(new java.awt.Font("sansserif", 1, 10));
        S_expDiff.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(20), Integer.valueOf(0), null, Integer.valueOf(1)));
        S_expDiff.setPreferredSize(new java.awt.Dimension(48, 24));
        S_expDiff.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_expDiffStateChanged(evt);
            }
        });
        P_diffSetting.add(S_expDiff);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(P_diffSetting, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void S_ageDiffStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_ageDiffStateChanged
        recalcPriceDynamic();
    }//GEN-LAST:event_S_ageDiffStateChanged

    private void S_talDiffStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_talDiffStateChanged
        recalcPriceDynamic();
    }//GEN-LAST:event_S_talDiffStateChanged

    private void S_strDiffStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_strDiffStateChanged
        recalcPriceDynamic();
    }//GEN-LAST:event_S_strDiffStateChanged

    private void S_expDiffStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_expDiffStateChanged
        recalcPriceDynamic();
    }//GEN-LAST:event_S_expDiffStateChanged

    private void S_svAddStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_svAddStateChanged
        recalcPriceDynamic();
    }//GEN-LAST:event_S_svAddStateChanged

    private void S_expAddStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_expAddStateChanged
        recalcPriceDynamic();
    }//GEN-LAST:event_S_expAddStateChanged

    private void S_strAddStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_strAddStateChanged
        recalcPriceDynamic();
    }//GEN-LAST:event_S_strAddStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel P_chart;
    private javax.swing.JPanel P_diffSetting;
    private javax.swing.JPanel P_diffSetting1;
    private javax.swing.JSpinner S_ageDiff;
    private javax.swing.JSpinner S_expAdd;
    private javax.swing.JSpinner S_expDiff;
    private javax.swing.JSpinner S_strAdd;
    private javax.swing.JSpinner S_strDiff;
    private javax.swing.JSpinner S_svAdd;
    private javax.swing.JSpinner S_talDiff;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized priceAnalysisTopComponent getDefault() {
        if (instance == null) {
            instance = new priceAnalysisTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the priceAnalysisTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized priceAnalysisTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(priceAnalysisTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof priceAnalysisTopComponent) {
            return (priceAnalysisTopComponent) win;
        }
        Logger.getLogger(priceAnalysisTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        Lookup.Template t1 = new Lookup.Template(MyTransferPlayer.class);
        playerLookupResult = (Lookup.Result<MyTransferPlayer>) CentralLookup.getDefault().lookup(t1);
        playerListener = new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                playerChanged(ev);
            }
        };

        playerLookupResult.addLookupListener(playerListener);

        Lookup.Template t2 = new Lookup.Template(EventList.class);
        listPlayersLookupResult = (Result<EventList<MyTransferPlayer>>) CentralLookup.getDefault().lookup(t2);
        listPlayersListener = new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                listPlayersChanged(ev);
            }
        };
        listPlayersLookupResult.addLookupListener(listPlayersListener);

        playerChanged(null);
        listPlayersChanged(null);
    }

    @Override
    public void componentClosed() {
        playerLookupResult.removeLookupListener(playerListener);
        listPlayersLookupResult.removeLookupListener(listPlayersListener);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private void initCharts() {
        dataSetXY = new XYSeriesCollection();
        XYSeries s1 = new XYSeries(getRes("PLAYER_PRICE_PROGNOZ"), true, false);
        dataSetXY.addSeries(s1);

        chartXY = ChartFactory.createXYLineChart(
                getRes("PLAYER_PRICE_PROGNOZ"), // chart title
                getRes("PLAYER_AGE_AXIS"), // domain axis label
                getRes("PLAYER_PRICE"), // range axis label
                dataSetXY, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true,
                false);
        listSeries.add(new CSerieControl(s1, "AGE_PRICE", "PRICE", chartXY, true, dataSetXY, true, true, Color.RED, Color.RED));

        XYPlot plot = chartXY.getXYPlot();
        NumberAxis domainAxis = new NumberAxis(getRes("PLAYER_AGE_AXIS"));
        NumberAxis rangeAxis = new NumberAxis(getRes("PLAYER_PRICE"));
        domainAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRangeIncludesZero(false);

        TickUnits ticks = new TickUnits();

        //NumberFormat nf1 = NumberFormat.getInstance();
        //nf1.setMaximumFractionDigits(0);
        NumberTickUnit tu = new NumberTickUnit(1);
        //ticks.add(new NumberTickUnit(step,nf1));
        ticks.add(tu);
        domainAxis.setTickUnit(tu);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        chartXY.setBorderVisible(true);
        plot.setOutlinePaint(Color.black);
        ChartPanel chartPanel = new ChartPanel(chartXY);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
        chartPanel.validate();
        P_chart.add(chartPanel);
    }

    public static String getRes(String resKey) {
        return NbBundle.getMessage(priceAnalysisTopComponent.class, resKey);
    }

    private void setTransferPlayer(MyTransferPlayer tp) {
        Object old = this.transferPlayer;
        if (old != tp) {
            this.transferPlayer = tp;
            recalcPriceDynamic();
        }
    }
}
