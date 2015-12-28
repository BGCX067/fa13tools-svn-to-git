package rov.fa13.transfers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.TransactionList;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.RangeMatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.fa13.build.controller.io.AllReader;
import com.fa13.build.controller.io.ReaderException;
import com.fa13.build.controller.io.TransferListReader;
import com.fa13.build.model.All;
import com.fa13.build.model.Player;
import com.fa13.build.model.Team;
import com.fa13.build.model.TransferList;
import com.fa13.build.model.TransferPlayer;
import java.awt.Color;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import rov.CU;
import rov.rcp.utils.CentralLookup;
import sun.net.www.protocol.file.FileURLConnection;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//rov.fa13.transfers//transfers//EN",
        autostore = false)
public final class transfersTopComponent extends TopComponent {

    private static final Logger LOG = Logger.getLogger(transfersTopComponent.class.getName());
    private String UI_PREFIX = "transfers";
    private JFileChooser fileChooser = new JFileChooser();
    private File file;
    private String tlist_date = "", alt_date = "", all_date = "";
    //private FilteredTableModel m;
    private static transfersTopComponent instance;
    //final private ArrayList<Ticket> ticketList = new ArrayList<Ticket>();
    private StringBuilder pageTeam13;
    private String pageTeam13CharSet= "windows-1251";
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "transfersTopComponent";
    private String localTeam13 = "team13.htm";
    private String urlTeam13 = "http://www.fa13.info/team.html?team=13";
    //private String urlTeam13 = "http://fa13.info/team.html?filter=&team=13"; 1.1.2
    private String ticket13Url = "http://www.fa13.info/trans-release.html";
    private String urlTlist = "http://www.fa13.info/build/Tlist13.b13";
    private String urlAltlist = "http://www.fa13.info/build/alltl.zip";
    private String urlAll = "http://www.fa13.info/build/all13.zip";
    //files
    private String fileTlist = "Tlist13.b13";
    private String fileAltlist = "alltl.zip";
    private String fileAll = "all13.zip";
    private InputOutput io_log;
    private int statusTlist = NONE, statusAltlist = NONE, statusAll = NONE, statusTeam13 = NONE, statusTickets = NONE;
    private static int NONE = 0;
    private static int OK = 1;
    private static int ERR = -1;
    private All alt_data, all_data;
    final private DateFormat df = DateFormat.getDateInstance();
    final private EventList<MyTransferPlayer> listTransferPlayers = new BasicEventList<MyTransferPlayer>();
    final private TransactionList<MyTransferPlayer> txListTransferPlayers = new TransactionList<MyTransferPlayer>(listTransferPlayers);
    private TransferList transferList;
    private TransferTableFormat tableFormat = new TransferTableFormat();
    private EventTableModel tableModel;// = new EventTableModel(listTransferPlayers, tableFormat);
    private EventList<MatcherEditor> matcherEditorsList = new BasicEventList<MatcherEditor>();
//matchers used for filtering
    private TextComponentMatcherEditor meName, meNationality, mePos;
    private RangeMatcherEditor meAge, meSalary, meHealth, meTickets, mePrice, meStrenght, meTalent, meExp;
    private BooleanMatcherEditor meMyChoose;
    private PlayerAbilitiesMatcherEditor meSpeed, meStamina, mePassing, meCrossing, meTackling, meHeading, meShooting, meDribbling, meHandling, meReflexes;
    private EventSelectionModel esm;
    private ArrayList<JComponent> listSvUI = new ArrayList<JComponent>();
    private List<String> choosedPlayers = new ArrayList<String>();
    private boolean firstListReloadDone = false;

    public transfersTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(transfersTopComponent.class, "CTL_transfersTopComponent"));
        setToolTipText(NbBundle.getMessage(transfersTopComponent.class, "HINT_transfersTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        //reloadData();

        Frame f = WindowManager.getDefault().getMainWindow();
        f.setTitle("Fa13Tools 0.7");
        initTable();
        S_s1.putClientProperty("enabler", CHB_s);
        S_s2.putClientProperty("enabler", CHB_s);
        S_f1.putClientProperty("enabler", CHB_f);
        S_f2.putClientProperty("enabler", CHB_f);
        S_p1.putClientProperty("enabler", CHB_p);
        S_p2.putClientProperty("enabler", CHB_p);
        S_n1.putClientProperty("enabler", CHB_n);
        S_n2.putClientProperty("enabler", CHB_n);
        S_o1.putClientProperty("enabler", CHB_o);
        S_o2.putClientProperty("enabler", CHB_o);
        S_vg1.putClientProperty("enabler", CHB_vg);
        S_vg2.putClientProperty("enabler", CHB_vg);
        S_u1.putClientProperty("enabler", CHB_u);
        S_u2.putClientProperty("enabler", CHB_u);
        S_d1.putClientProperty("enabler", CHB_d);
        S_d2.putClientProperty("enabler", CHB_d);
        S_t1.putClientProperty("enabler", CHB_t);
        S_t2.putClientProperty("enabler", CHB_t);
        S_r1.putClientProperty("enabler", CHB_r);
        S_r2.putClientProperty("enabler", CHB_r);

        listSvUI.add(S_s1);
        listSvUI.add(S_s2);
        listSvUI.add(S_f1);
        listSvUI.add(S_f2);
        listSvUI.add(S_p1);
        listSvUI.add(S_p2);
        listSvUI.add(S_n1);
        listSvUI.add(S_n2);
        listSvUI.add(S_o1);
        listSvUI.add(S_o2);
        listSvUI.add(S_vg1);
        listSvUI.add(S_vg2);
        listSvUI.add(S_u1);
        listSvUI.add(S_u2);
        listSvUI.add(S_d1);
        listSvUI.add(S_d2);
        listSvUI.add(S_t1);
        listSvUI.add(S_t2);
        listSvUI.add(S_r1);
        listSvUI.add(S_r2);
    }

    private void initTable() {
        meName = new TextComponentMatcherEditor(TF_name, GlazedLists.textFilterator("name"));
        TextFilterator nationalityFilterator = new TextFilterator() {
            @Override
            public void getFilterStrings(List list, Object e) {
                MyTransferPlayer tp = (MyTransferPlayer) e;
                list.add(tp.getNationality());
            }
        };
        meNationality = new TextComponentMatcherEditor(TF_country, nationalityFilterator);
        mePos = new TextComponentMatcherEditor(TF_pos, GlazedLists.textFilterator("position"));
        meAge = new RangeMatcherEditor(GlazedLists.filterator("age"));
        meSalary = new RangeMatcherEditor(GlazedLists.filterator("salary"));
        meHealth = new RangeMatcherEditor(GlazedLists.filterator("health"));
        meTickets = new RangeMatcherEditor(GlazedLists.filterator("tickets"));
        mePrice = new RangeMatcherEditor(GlazedLists.filterator("price"));
        meStrenght = new RangeMatcherEditor(GlazedLists.filterator("strength"));//strength
        meTalent = new RangeMatcherEditor(GlazedLists.filterator("talent"));
        meExp = new RangeMatcherEditor(GlazedLists.filterator("experience"));//experience
//init abilities
        meSpeed = new PlayerAbilitiesMatcherEditor(getRes("S"), S_s1, S_s2);
        meStamina = new PlayerAbilitiesMatcherEditor(getRes("F"), S_f1, S_f2);
        mePassing = new PlayerAbilitiesMatcherEditor(getRes("P"), S_p1, S_p2);
        meCrossing = new PlayerAbilitiesMatcherEditor(getRes("N"), S_n1, S_n2);
        meTackling = new PlayerAbilitiesMatcherEditor(getRes("O"), S_o1, S_o2);
        meHeading = new PlayerAbilitiesMatcherEditor(getRes("VG"), S_vg1, S_vg2);
        meShooting = new PlayerAbilitiesMatcherEditor(getRes("U"), S_u1, S_u2);
        meDribbling = new PlayerAbilitiesMatcherEditor(getRes("D"), S_d1, S_d2);
        meHandling = new PlayerAbilitiesMatcherEditor(getRes("T"), S_t1, S_t2);
        meReflexes = new PlayerAbilitiesMatcherEditor(getRes("R"), S_r1, S_r2);
        meMyChoose = new BooleanMatcherEditor(GlazedLists.filterator("selectedForTicket"), CHB_myChoose);
        toggleFilter(false);
        toggleAbilitiesFilter(false);

        CompositeMatcherEditor cme = new CompositeMatcherEditor(matcherEditorsList);

        FilterList fl = new FilterList(listTransferPlayers, cme);
        SortedList sl = new SortedList(fl, new MyTransferPlayerComparator());
        tableModel = new EventTableModel(sl, tableFormat);
        T_tlist.setModel(tableModel);

        //all magic done in constructor!!!
        TableComparatorChooser tcc = new TableComparatorChooser(T_tlist, sl, true);
//set selection model
        esm = new EventSelectionModel(sl);
        T_tlist.setSelectionModel(esm);
        esm.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                tlistSelectionChanged(e);
            }
        });

    }

    private List getMyChoosedPlayers() {
        //StringBuilder sb = new StringBuilder();
        choosedPlayers.clear();
        for (MyTransferPlayer myTransferPlayer : txListTransferPlayers) {
            if (myTransferPlayer.isSelectedForTicket()) {
                choosedPlayers.add(String.valueOf(myTransferPlayer.getId()));
            }
        }
        return choosedPlayers;
    }

    private void setMyChoosedPlayers(List<String> listPlayerId) {
        if (listPlayerId == null || listPlayerId.isEmpty()) {
            return;
        }
        txListTransferPlayers.beginEvent();
        Integer num;
        for (String id : listPlayerId) {
            if (id instanceof String) {
                for (int i = 0; i < txListTransferPlayers.size(); i++) {
                    MyTransferPlayer tp = txListTransferPlayers.get(i);
                    try {
                        num = Integer.valueOf(id);
                        if (tp.getId() == num.intValue()) {
                            tp.setSelectedForTicket(true);
                            txListTransferPlayers.set(i, tp);//hack to update table view
                        }
                    } catch (Exception e) {
                        //System.out.println(e.printStackTrace();
                    }
                }
            }
        }
        txListTransferPlayers.commitEvent();
    }

    private void tlistSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            //if (e.getFirstIndex() == e.getLastIndex())
            //int selRow = T_tlist.getSelectedRow();
            //int selRow = e.getFirstIndex();
//            if (selRow > -1 && selRow < listTransferPlayers.size()) {
//                CentralLookup.getDef().addSingleton(listTransferPlayers.get(selRow));
//            }
            if (esm.getSelected() != null && !esm.getSelected().isEmpty()) {
                MyTransferPlayer tp = (MyTransferPlayer) esm.getSelected().get(0);
                CentralLookup.getDef().addSingleton(tp);
            }
        }
    }

    private void toggleFilter(boolean flag) {
        if (flag) {
            matcherEditorsList.add(meName);
            matcherEditorsList.add(meAge);
            matcherEditorsList.add(meExp);
            matcherEditorsList.add(meHealth);
            matcherEditorsList.add(meNationality);
            matcherEditorsList.add(mePos);
            matcherEditorsList.add(mePrice);
            matcherEditorsList.add(meSalary);
            matcherEditorsList.add(meStrenght);
            matcherEditorsList.add(meTalent);
            matcherEditorsList.add(meTickets);
            matcherEditorsList.add(meMyChoose);

        } else {
            matcherEditorsList.remove(meName);
            matcherEditorsList.remove(meAge);
            matcherEditorsList.remove(meExp);
            matcherEditorsList.remove(meHealth);
            matcherEditorsList.remove(meNationality);
            matcherEditorsList.remove(mePos);
            matcherEditorsList.remove(mePrice);
            matcherEditorsList.remove(meSalary);
            matcherEditorsList.remove(meStrenght);
            matcherEditorsList.remove(meTalent);
            matcherEditorsList.remove(meTickets);
            matcherEditorsList.remove(meMyChoose);
        }
        CU.setEnabled(P_f1, flag);
    }

    private void toggleAbilitiesFilter(boolean flag) {
        if (flag) {

            toggleSvFilter(CHB_s, meSpeed);
            toggleSvFilter(CHB_f, meStamina);
            toggleSvFilter(CHB_p, mePassing);
            toggleSvFilter(CHB_n, meCrossing);
            toggleSvFilter(CHB_o, meTackling);
            toggleSvFilter(CHB_vg, meHeading);
            toggleSvFilter(CHB_u, meShooting);
            toggleSvFilter(CHB_d, meDribbling);
            toggleSvFilter(CHB_t, meHandling);
            toggleSvFilter(CHB_r, meReflexes);

        } else {
            matcherEditorsList.remove(meSpeed);
            matcherEditorsList.remove(meStamina);
            matcherEditorsList.remove(mePassing);
            matcherEditorsList.remove(meCrossing);
            matcherEditorsList.remove(meTackling);
            matcherEditorsList.remove(meHeading);
            matcherEditorsList.remove(meShooting);
            matcherEditorsList.remove(meDribbling);
            matcherEditorsList.remove(meHandling);
            matcherEditorsList.remove(meReflexes);
        }
        //CU.setEnabled(P_f2, flag);
        CHB_s.setEnabled(flag);
        CHB_f.setEnabled(flag);
        CHB_n.setEnabled(flag);
        CHB_p.setEnabled(flag);
        CHB_o.setEnabled(flag);
        CHB_vg.setEnabled(flag);
        CHB_u.setEnabled(flag);
        CHB_d.setEnabled(flag);
        CHB_t.setEnabled(flag);
        CHB_r.setEnabled(flag);

        if (!flag) {
            //disable
            for (JComponent ui : listSvUI) {
                ui.setEnabled(false);
            }
        } else {
            for (JComponent ui : listSvUI) {
                JCheckBox cb = (JCheckBox) ui.getClientProperty("enabler");
                if (cb != null) {
                    ui.setEnabled(cb.isSelected());
                }
            }
        }


    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        SS_bot = new javax.swing.JSplitPane();
        SP_tlist = new javax.swing.JScrollPane();
        T_tlist = new javax.swing.JTable();
        P_up = new javax.swing.JPanel();
        B_openTList = new javax.swing.JButton();
        B_openAList = new javax.swing.JButton();
        B_openAll = new javax.swing.JButton();
        B_updateTeam13 = new javax.swing.JButton();
        L44 = new javax.swing.JLabel();
        B_reload = new javax.swing.JButton();
        B_updateTickets = new javax.swing.JButton();
        P_filter = new javax.swing.JPanel();
        CHB_useFilter = new javax.swing.JCheckBox();
        B_resetMainFilter = new javax.swing.JButton();
        CHB_svFilter = new javax.swing.JCheckBox();
        B_resetSvFilter = new javax.swing.JButton();
        P_f1 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        S_age1 = new javax.swing.JSpinner();
        S_age2 = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        S_str1 = new javax.swing.JSpinner();
        S_str2 = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        S_sal1 = new javax.swing.JSpinner();
        S_sal2 = new javax.swing.JSpinner();
        jLabel18 = new javax.swing.JLabel();
        S_tal1 = new javax.swing.JSpinner();
        S_tal2 = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        S_h1 = new javax.swing.JSpinner();
        S_h2 = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        S_exp1 = new javax.swing.JSpinner();
        S_exp2 = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        S_tick1 = new javax.swing.JSpinner();
        S_tick2 = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        TF_pos = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        S_pr1 = new javax.swing.JSpinner();
        S_pr2 = new javax.swing.JSpinner();
        CHB_myChoose = new javax.swing.JCheckBox();
        jLabel17 = new javax.swing.JLabel();
        TF_name = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        TF_country = new javax.swing.JTextField();
        P_f2 = new javax.swing.JPanel();
        CHB_s = new javax.swing.JCheckBox();
        S_s1 = new javax.swing.JSpinner();
        S_s2 = new javax.swing.JSpinner();
        CHB_vg = new javax.swing.JCheckBox();
        S_vg1 = new javax.swing.JSpinner();
        S_vg2 = new javax.swing.JSpinner();
        CHB_f = new javax.swing.JCheckBox();
        S_f1 = new javax.swing.JSpinner();
        S_f2 = new javax.swing.JSpinner();
        CHB_u = new javax.swing.JCheckBox();
        S_u1 = new javax.swing.JSpinner();
        S_u2 = new javax.swing.JSpinner();
        CHB_p = new javax.swing.JCheckBox();
        S_p1 = new javax.swing.JSpinner();
        S_p2 = new javax.swing.JSpinner();
        CHB_d = new javax.swing.JCheckBox();
        S_d1 = new javax.swing.JSpinner();
        S_d2 = new javax.swing.JSpinner();
        CHB_n = new javax.swing.JCheckBox();
        S_n1 = new javax.swing.JSpinner();
        S_n2 = new javax.swing.JSpinner();
        CHB_t = new javax.swing.JCheckBox();
        S_t1 = new javax.swing.JSpinner();
        S_t2 = new javax.swing.JSpinner();
        CHB_o = new javax.swing.JCheckBox();
        S_o1 = new javax.swing.JSpinner();
        S_o2 = new javax.swing.JSpinner();
        CHB_r = new javax.swing.JCheckBox();
        S_r1 = new javax.swing.JSpinner();
        S_r2 = new javax.swing.JSpinner();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel3.text")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(90, 26));

        setDisplayName(org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.displayName")); // NOI18N
        setDoubleBuffered(true);
        setMinimumSize(new java.awt.Dimension(700, 200));
        setPreferredSize(new java.awt.Dimension(1010, 500));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        SS_bot.setDividerLocation(290);
        SS_bot.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        SS_bot.setMinimumSize(new java.awt.Dimension(1010, 300));
        SS_bot.setPreferredSize(new java.awt.Dimension(1010, 600));

        SP_tlist.setDoubleBuffered(true);

        T_tlist.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        T_tlist.setDoubleBuffered(true);
        SP_tlist.setViewportView(T_tlist);
        T_tlist.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        SS_bot.setBottomComponent(SP_tlist);

        P_up.setPreferredSize(new java.awt.Dimension(1000, 270));

        B_openTList.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(B_openTList, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.B_openTList.text")); // NOI18N
        B_openTList.setPreferredSize(new java.awt.Dimension(144, 24));
        B_openTList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_openTListActionPerformed(evt);
            }
        });
        P_up.add(B_openTList);

        B_openAList.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(B_openAList, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.B_openAList.text")); // NOI18N
        B_openAList.setPreferredSize(new java.awt.Dimension(188, 24));
        B_openAList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_openAListActionPerformed(evt);
            }
        });
        P_up.add(B_openAList);

        B_openAll.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(B_openAll, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.B_openAll.text")); // NOI18N
        B_openAll.setPreferredSize(new java.awt.Dimension(60, 24));
        B_openAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_openAllActionPerformed(evt);
            }
        });
        P_up.add(B_openAll);

        B_updateTeam13.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(B_updateTeam13, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.B_updateTeam13.text")); // NOI18N
        B_updateTeam13.setPreferredSize(new java.awt.Dimension(120, 24));
        B_updateTeam13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_updateTeam13ActionPerformed(evt);
            }
        });
        P_up.add(B_updateTeam13);

        L44.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        L44.setForeground(java.awt.Color.blue);
        org.openide.awt.Mnemonics.setLocalizedText(L44, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.L44.text")); // NOI18N
        L44.setPreferredSize(new java.awt.Dimension(70, 20));
        P_up.add(L44);

        B_reload.setBackground(java.awt.Color.yellow);
        B_reload.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(B_reload, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.B_reload.text")); // NOI18N
        B_reload.setDoubleBuffered(true);
        B_reload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_reloadActionPerformed(evt);
            }
        });
        P_up.add(B_reload);

        B_updateTickets.setBackground(java.awt.Color.yellow);
        B_updateTickets.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(B_updateTickets, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.B_updateTickets.text")); // NOI18N
        B_updateTickets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_updateTicketsActionPerformed(evt);
            }
        });
        P_up.add(B_updateTickets);

        P_filter.setOpaque(false);

        CHB_useFilter.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_useFilter, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_useFilter.text")); // NOI18N
        CHB_useFilter.setPreferredSize(new java.awt.Dimension(240, 18));
        CHB_useFilter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_useFilterItemStateChanged(evt);
            }
        });

        B_resetMainFilter.setBackground(java.awt.Color.yellow);
        B_resetMainFilter.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(B_resetMainFilter, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.B_resetMainFilter.text")); // NOI18N
        B_resetMainFilter.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        B_resetMainFilter.setPreferredSize(new java.awt.Dimension(130, 20));
        B_resetMainFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_resetMainFilterActionPerformed(evt);
            }
        });

        CHB_svFilter.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_svFilter, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_svFilter.text")); // NOI18N
        CHB_svFilter.setPreferredSize(new java.awt.Dimension(320, 18));
        CHB_svFilter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_svFilterItemStateChanged(evt);
            }
        });

        B_resetSvFilter.setBackground(java.awt.Color.yellow);
        B_resetSvFilter.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(B_resetSvFilter, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.B_resetSvFilter.text")); // NOI18N
        B_resetSvFilter.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        B_resetSvFilter.setPreferredSize(new java.awt.Dimension(130, 20));
        B_resetSvFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                B_resetSvFilterActionPerformed(evt);
            }
        });

        P_f1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        P_f1.setOpaque(false);

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel13.text")); // NOI18N
        jLabel13.setMaximumSize(new java.awt.Dimension(64, 15));
        jLabel13.setMinimumSize(new java.awt.Dimension(64, 15));
        jLabel13.setName(""); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(64, 15));

        S_age1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_age1.setModel(new javax.swing.SpinnerNumberModel(10, 1, 99, 1));
        S_age1.setMinimumSize(new java.awt.Dimension(64, 26));
        S_age1.setName(""); // NOI18N
        S_age1.setPreferredSize(new java.awt.Dimension(40, 20));
        S_age1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_age1StateChanged(evt);
            }
        });

        S_age2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_age2.setModel(new javax.swing.SpinnerNumberModel(99, 1, 100, 1));
        S_age2.setName(""); // NOI18N
        S_age2.setPreferredSize(new java.awt.Dimension(40, 20));
        S_age2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_age2StateChanged(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel11.text")); // NOI18N
        jLabel11.setName(""); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(55, 20));

        S_str1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_str1.setModel(new javax.swing.SpinnerNumberModel(1, 1, 199, 1));
        S_str1.setName(""); // NOI18N
        S_str1.setPreferredSize(new java.awt.Dimension(40, 20));
        S_str1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_str1StateChanged(evt);
            }
        });

        S_str2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_str2.setModel(new javax.swing.SpinnerNumberModel(199, 1, 199, 1));
        S_str2.setName(""); // NOI18N
        S_str2.setPreferredSize(new java.awt.Dimension(40, 20));
        S_str2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_str2StateChanged(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel12.text")); // NOI18N
        jLabel12.setName(""); // NOI18N

        S_sal1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_sal1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));
        S_sal1.setName(""); // NOI18N
        S_sal1.setPreferredSize(new java.awt.Dimension(40, 20));
        S_sal1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_sal1StateChanged(evt);
            }
        });

        S_sal2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_sal2.setModel(new javax.swing.SpinnerNumberModel(999, 0, 999, 1));
        S_sal2.setName(""); // NOI18N
        S_sal2.setPreferredSize(new java.awt.Dimension(40, 20));
        S_sal2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_sal2StateChanged(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel18.text")); // NOI18N
        jLabel18.setName(""); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(55, 20));

        S_tal1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_tal1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_tal1.setName(""); // NOI18N
        S_tal1.setPreferredSize(new java.awt.Dimension(40, 20));
        S_tal1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_tal1StateChanged(evt);
            }
        });

        S_tal2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_tal2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_tal2.setName(""); // NOI18N
        S_tal2.setPreferredSize(new java.awt.Dimension(40, 20));
        S_tal2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_tal2StateChanged(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel19.text")); // NOI18N
        jLabel19.setMaximumSize(new java.awt.Dimension(64, 15));
        jLabel19.setMinimumSize(new java.awt.Dimension(64, 15));
        jLabel19.setName(""); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(64, 15));

        S_h1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_h1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_h1.setName(""); // NOI18N
        S_h1.setPreferredSize(new java.awt.Dimension(40, 20));
        S_h1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_h1StateChanged(evt);
            }
        });

        S_h2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_h2.setModel(new javax.swing.SpinnerNumberModel(100, 20, 100, 1));
        S_h2.setName(""); // NOI18N
        S_h2.setPreferredSize(new java.awt.Dimension(40, 20));
        S_h2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_h2StateChanged(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel21, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel21.text")); // NOI18N
        jLabel21.setName(""); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(55, 20));

        S_exp1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_exp1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 199, 1));
        S_exp1.setName(""); // NOI18N
        S_exp1.setPreferredSize(new java.awt.Dimension(40, 20));
        S_exp1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_exp1StateChanged(evt);
            }
        });

        S_exp2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_exp2.setModel(new javax.swing.SpinnerNumberModel(199, 0, 199, 1));
        S_exp2.setName(""); // NOI18N
        S_exp2.setPreferredSize(new java.awt.Dimension(40, 20));
        S_exp2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_exp2StateChanged(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel20, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel20.text")); // NOI18N
        jLabel20.setMaximumSize(new java.awt.Dimension(64, 15));
        jLabel20.setMinimumSize(new java.awt.Dimension(64, 15));
        jLabel20.setName(""); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(64, 15));

        S_tick1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_tick1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));
        S_tick1.setName(""); // NOI18N
        S_tick1.setPreferredSize(new java.awt.Dimension(40, 20));
        S_tick1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_tick1StateChanged(evt);
            }
        });

        S_tick2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_tick2.setModel(new javax.swing.SpinnerNumberModel(199, 0, 199, 1));
        S_tick2.setName(""); // NOI18N
        S_tick2.setPreferredSize(new java.awt.Dimension(40, 20));
        S_tick2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_tick2StateChanged(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel15.text")); // NOI18N
        jLabel15.setName(""); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(55, 20));

        TF_pos.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TF_pos.setName(""); // NOI18N
        TF_pos.setPreferredSize(new java.awt.Dimension(80, 20));

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel14.text")); // NOI18N
        jLabel14.setMaximumSize(new java.awt.Dimension(64, 15));
        jLabel14.setMinimumSize(new java.awt.Dimension(64, 15));
        jLabel14.setName(""); // NOI18N
        jLabel14.setPreferredSize(new java.awt.Dimension(64, 15));

        S_pr1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_pr1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 99999, 100));
        S_pr1.setName(""); // NOI18N
        S_pr1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_pr1StateChanged(evt);
            }
        });

        S_pr2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_pr2.setModel(new javax.swing.SpinnerNumberModel(99999, 0, 99999, 100));
        S_pr2.setName(""); // NOI18N
        S_pr2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                S_pr2StateChanged(evt);
            }
        });

        CHB_myChoose.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_myChoose, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_myChoose.text")); // NOI18N
        CHB_myChoose.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        CHB_myChoose.setName(""); // NOI18N
        CHB_myChoose.setPreferredSize(new java.awt.Dimension(120, 20));

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel17.text")); // NOI18N
        jLabel17.setName(""); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(35, 20));

        TF_name.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TF_name.setName(""); // NOI18N
        TF_name.setPreferredSize(new java.awt.Dimension(135, 20));

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.jLabel16.text")); // NOI18N
        jLabel16.setName(""); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(45, 20));

        TF_country.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TF_country.setName(""); // NOI18N
        TF_country.setPreferredSize(new java.awt.Dimension(135, 20));

        javax.swing.GroupLayout P_f1Layout = new javax.swing.GroupLayout(P_f1);
        P_f1.setLayout(P_f1Layout);
        P_f1Layout.setHorizontalGroup(
            P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(P_f1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(P_f1Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(TF_name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TF_country, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, P_f1Layout.createSequentialGroup()
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f1Layout.createSequentialGroup()
                                .addComponent(S_tick1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(S_tick2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(P_f1Layout.createSequentialGroup()
                                .addComponent(S_pr1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(S_pr2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, P_f1Layout.createSequentialGroup()
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(P_f1Layout.createSequentialGroup()
                                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(S_h1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(S_h2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(P_f1Layout.createSequentialGroup()
                                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, P_f1Layout.createSequentialGroup()
                                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(S_age1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(P_f1Layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(S_sal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(S_sal2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(S_age2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(65, 65, 65)
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f1Layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(P_f1Layout.createSequentialGroup()
                                        .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(S_tal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(S_tal2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(P_f1Layout.createSequentialGroup()
                                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(S_str1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(S_str2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, P_f1Layout.createSequentialGroup()
                                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(P_f1Layout.createSequentialGroup()
                                        .addComponent(S_exp1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(S_exp2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(TF_pos, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, P_f1Layout.createSequentialGroup()
                                .addComponent(CHB_myChoose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(49, 49, 49)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        P_f1Layout.setVerticalGroup(
            P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(P_f1Layout.createSequentialGroup()
                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(P_f1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_age1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_age2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_sal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel12))
                            .addComponent(S_sal2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_h1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_h2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(P_f1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_str1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_str2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_tal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_tal2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_exp1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_exp2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(S_tick1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(S_tick2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TF_pos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(S_pr1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(S_pr2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(CHB_myChoose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TF_name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(P_f1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(TF_country, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel12.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.AccessibleContext.accessibleName")); // NOI18N

        P_f2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        P_f2.setMinimumSize(new java.awt.Dimension(108, 40));
        P_f2.setOpaque(false);
        P_f2.setPreferredSize(new java.awt.Dimension(420, 190));

        CHB_s.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_s, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_s.text")); // NOI18N
        CHB_s.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_s.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_sItemStateChanged(evt);
            }
        });

        S_s1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_s1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_s1.setEnabled(false);
        S_s1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_s2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_s2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_s2.setEnabled(false);
        S_s2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_vg.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_vg, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_vg.text")); // NOI18N
        CHB_vg.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_vg.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_vgItemStateChanged(evt);
            }
        });

        S_vg1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_vg1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_vg1.setEnabled(false);
        S_vg1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_vg2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_vg2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_vg2.setEnabled(false);
        S_vg2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_f.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_f, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_f.text")); // NOI18N
        CHB_f.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_f.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_fItemStateChanged(evt);
            }
        });

        S_f1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_f1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_f1.setEnabled(false);
        S_f1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_f2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_f2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_f2.setEnabled(false);
        S_f2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_u.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_u, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_u.text")); // NOI18N
        CHB_u.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_u.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_uItemStateChanged(evt);
            }
        });

        S_u1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_u1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_u1.setEnabled(false);
        S_u1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_u2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_u2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_u2.setEnabled(false);
        S_u2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_p.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_p, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_p.text")); // NOI18N
        CHB_p.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_p.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_pItemStateChanged(evt);
            }
        });

        S_p1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_p1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_p1.setEnabled(false);
        S_p1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_p2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_p2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_p2.setEnabled(false);
        S_p2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_d.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_d, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_d.text")); // NOI18N
        CHB_d.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_d.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_dItemStateChanged(evt);
            }
        });

        S_d1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_d1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_d1.setEnabled(false);
        S_d1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_d2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_d2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_d2.setEnabled(false);
        S_d2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_n.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_n, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_n.text")); // NOI18N
        CHB_n.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_n.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_nItemStateChanged(evt);
            }
        });

        S_n1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_n1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_n1.setEnabled(false);
        S_n1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_n2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_n2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_n2.setEnabled(false);
        S_n2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_t.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_t, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_t.text")); // NOI18N
        CHB_t.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_t.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_tItemStateChanged(evt);
            }
        });

        S_t1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_t1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_t1.setEnabled(false);
        S_t1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_t2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_t2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_t2.setEnabled(false);
        S_t2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_o.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_o, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_o.text")); // NOI18N
        CHB_o.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_o.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_oItemStateChanged(evt);
            }
        });

        S_o1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_o1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_o1.setEnabled(false);
        S_o1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_o2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_o2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_o2.setEnabled(false);
        S_o2.setPreferredSize(new java.awt.Dimension(50, 20));

        CHB_r.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(CHB_r, org.openide.util.NbBundle.getMessage(transfersTopComponent.class, "transfersTopComponent.CHB_r.text")); // NOI18N
        CHB_r.setPreferredSize(new java.awt.Dimension(105, 20));
        CHB_r.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CHB_rItemStateChanged(evt);
            }
        });

        S_r1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_r1.setModel(new javax.swing.SpinnerNumberModel(20, 20, 199, 1));
        S_r1.setEnabled(false);
        S_r1.setPreferredSize(new java.awt.Dimension(50, 20));

        S_r2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        S_r2.setModel(new javax.swing.SpinnerNumberModel(199, 20, 199, 1));
        S_r2.setEnabled(false);
        S_r2.setPreferredSize(new java.awt.Dimension(50, 20));

        javax.swing.GroupLayout P_f2Layout = new javax.swing.GroupLayout(P_f2);
        P_f2.setLayout(P_f2Layout);
        P_f2Layout.setHorizontalGroup(
            P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(P_f2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(P_f2Layout.createSequentialGroup()
                        .addComponent(CHB_s, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(S_s1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(S_s2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(P_f2Layout.createSequentialGroup()
                        .addComponent(CHB_p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(S_p1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(S_p2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(P_f2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(CHB_f, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(S_f1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(S_f2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(P_f2Layout.createSequentialGroup()
                            .addComponent(CHB_o, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(S_o1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(4, 4, 4)
                            .addComponent(S_o2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, P_f2Layout.createSequentialGroup()
                            .addComponent(CHB_n, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(S_n1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(5, 5, 5)
                            .addComponent(S_n2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(P_f2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(CHB_t, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(CHB_r, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f2Layout.createSequentialGroup()
                                .addComponent(S_r1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(S_r2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_t1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, P_f2Layout.createSequentialGroup()
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(S_t2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(P_f2Layout.createSequentialGroup()
                                .addComponent(CHB_d, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(S_d1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(S_d2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(P_f2Layout.createSequentialGroup()
                                .addComponent(CHB_vg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(S_vg1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(S_vg2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(P_f2Layout.createSequentialGroup()
                                .addComponent(CHB_u, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(S_u1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(S_u2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(1, 1, 1)))
                .addGap(4, 4, 4))
        );
        P_f2Layout.setVerticalGroup(
            P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(P_f2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(P_f2Layout.createSequentialGroup()
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_vg1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(CHB_vg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_vg2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CHB_u, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_u1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_u2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_d1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(CHB_d, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_d2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CHB_t, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_t1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_t2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_r1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(CHB_r, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_r2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(P_f2Layout.createSequentialGroup()
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(S_s1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(CHB_s, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_s2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_f1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(CHB_f, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_f2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CHB_p, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_p1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_p2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(S_n1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(CHB_n, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(S_n2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(P_f2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CHB_o, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_o1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(S_o2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(79, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout P_filterLayout = new javax.swing.GroupLayout(P_filter);
        P_filter.setLayout(P_filterLayout);
        P_filterLayout.setHorizontalGroup(
            P_filterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(P_filterLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(P_filterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(P_f1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(P_filterLayout.createSequentialGroup()
                        .addComponent(CHB_useFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(B_resetMainFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(P_filterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(P_filterLayout.createSequentialGroup()
                        .addComponent(CHB_svFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(B_resetSvFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(P_filterLayout.createSequentialGroup()
                        .addComponent(P_f2, javax.swing.GroupLayout.PREFERRED_SIZE, 470, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        P_filterLayout.setVerticalGroup(
            P_filterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(P_filterLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(P_filterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(B_resetMainFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CHB_useFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CHB_svFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(B_resetSvFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(P_filterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(P_f2, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                    .addComponent(P_f1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        P_up.add(P_filter);

        SS_bot.setTopComponent(P_up);

        add(SS_bot);
    }// </editor-fold>//GEN-END:initComponents
    private FileFilter fa13FilesFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            boolean res = false;
            if ((pathname.getName().toUpperCase().endsWith("B13")) || ((pathname.getName().toUpperCase().endsWith("ZIP"))) || (pathname.isDirectory())) {
                res = true;
            }
            return res;
        }

        @Override
        public String getDescription() {
            return "fa13 files(*.b13 or *.zip)";
        }
    };

    private void B_openTListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_openTListActionPerformed
        //file dialog options
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(fa13FilesFilter);
        file = new File(fileTlist);
        fileChooser.setCurrentDirectory(file);
        fileChooser.setSelectedFile(file);
        //fileChooser.setFileFilter( schFilter );
        fileChooser.setVisible(true);
        if (this.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            fileTlist = file.getPath();
            B_openTList.setToolTipText(fileTlist);
            //doLoadTList(false);
            //doLoadAll(true);
            //doLoadAlt(true);
            //updateTeam13();
            //m.fireTableDataChanged();
        }
    }//GEN-LAST:event_B_openTListActionPerformed

    private void reloadData() {
        //no asks!!! fo simplify usage
        if (firstListReloadDone) {
            //save current chooosed players
            //NotifyDescriptor nd = new DialogDescriptor.Confirmation(getRes("ASK_SAVE_LIST_CHOOSED_PLAYERS"));
            //DialogDisplayer.getDefault().notify(nd);
            //if (NotifyDescriptor.YES_OPTION.equals(nd.getValue())) {
            choosedPlayers = getMyChoosedPlayers();
            //}

        }

        doLoadTList(true);

        //check if user wants to load last choosen players


        doLoadAll(true);
        doLoadAlt(true);
        updateTeam13();
        //here we must put updated list to Lookup

        //only first load we ask?
        if (!firstListReloadDone) {

//            NotifyDescriptor nd = new DialogDescriptor.Confirmation(getRes("ASK_LOAD_LIST_CHOOSED_PLAYERS"));
//            DialogDisplayer.getDefault().notify(nd);
//            if (NotifyDescriptor.YES_OPTION.equals(nd.getValue())) {
//                this.setMyChoosedPlayers(choosedPlayers);
//            }
            firstListReloadDone = true;
        }

        this.setMyChoosedPlayers(choosedPlayers);

        CentralLookup.getDef().addSingleton(txListTransferPlayers);
    }

    private void B_openAListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_openAListActionPerformed
        //file dialog options

        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(fa13FilesFilter);

        file = new File(fileAltlist);
        fileChooser.setCurrentDirectory(file);
        fileChooser.setSelectedFile(file);
        //fileChooser.setFileFilter( schFilter );
        fileChooser.setVisible(true);
        if (this.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            fileAltlist = file.getPath();
            B_openAList.setToolTipText(fileAltlist);
            //doLoadAlt(false);
        }
    }//GEN-LAST:event_B_openAListActionPerformed

    private void B_updateTicketsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_updateTicketsActionPerformed
        updateTickets();
    }//GEN-LAST:event_B_updateTicketsActionPerformed

    private void updateTickets() {
        /*
         if ((m == null) || (m.getRowCount() == 0)) {
         return;
         }
         */
        if ((txListTransferPlayers == null) || (txListTransferPlayers.size() < 1)) {
            return;
        }

        Runnable task = new Runnable() {
            @Override
            public void run() {
                String lh = "tickets: ";
                try {
                    //"file:///D:/BACKUP/tickets.html"
                    //"http://www.fa13.com/trans-release.html"
                    BufferedReader br;
                    URL url = new URL(ticket13Url);
                    Object obj = url.openConnection();
                    log(lh + "loading player's tickets from [" + ticket13Url + "] ...", Color.black);
                    if (obj instanceof HttpURLConnection) {
                        HttpURLConnection con = (HttpURLConnection) obj;
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    } else {
                        FileURLConnection con = (FileURLConnection) obj;
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    }
                    StringBuilder sb = new StringBuilder();
                    //BufferedReader br = new BufferedReader( new FileReader(fileName) );
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    DefaultTableModel mm = Fa13Utils.parseHtmlTable(sb.toString(), 5, null);

                    mm.removeRow(mm.getRowCount() - 1);
                    mm.removeRow(mm.getRowCount() - 1);
                    mm.removeRow(mm.getRowCount() - 1);

                    int size2 = mm.getRowCount();
                    Object updTime = CU.isNull(mm.getValueAt(size2 - 1, 0), "");

                    String s = updTime.toString();//update time
                    log(lh + "datestamp is [" + s + "]", Color.blue);
                    log(lh + "loaded " + size2 + " player's tickets", Color.black);

                    B_updateTickets.setToolTipText("update time: " + s);
                    //mm.removeRow(size-1);
                    //mm.removeRow(size-2);
                    //mm.removeRow(size-3);
/*
                     int size = m.getRowCount();
                     for (int i = 1; i < size2 - 4; i++) {
                    
                     for (int row = 0; row < size; row++) {
                     if ((m.getValueAt(row, 1).equals(mm.getValueAt(i, 1)))
                     && (m.getValueAt(row, 3).equals(mm.getValueAt(i, 2)))
                     && (m.getValueAt(row, 4).equals(mm.getValueAt(i, 0)))) {
                    
                     m.setValueAt(mm.getValueAt(i, 3), row, 14);//tickets
                     break;// relation 1:1
                     }
                    
                     }
                     }
                     */
                    txListTransferPlayers.getReadWriteLock().writeLock().lock();
                    txListTransferPlayers.beginEvent();
                    try {
                        Fa13Utils.updateTransferPlayersTicketsFromTable(txListTransferPlayers, mm, 1, 3);
                    } finally {
                        txListTransferPlayers.commitEvent();
                        txListTransferPlayers.getReadWriteLock().writeLock().unlock();
                    }

                } catch (MalformedURLException ex) {
                    log(lh + "Bad tickets url", Color.red);
                    B_updateTickets.setToolTipText("update failed: URL incorrect");
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    log(lh + "connection error", Color.red);
                    B_updateTickets.setToolTipText("update failed: Network error");
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        ProgressUtils.showProgressDialogAndRun(task, getRes("UPDATING_TICKETS"));
        int v = CU.getInt(S_tick1, 0);
        meTickets.setRange(v + 1, CU.getInt(S_tick2, 100));
        meTickets.setRange(v, CU.getInt(S_tick2, 100));
        //tableModel.fireTableDataChanged();//to apply filters
    }

    public int writeFile(String fileName, StringBuilder sb) {
        int res = -1;
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(fileName));

            String line = null;
            bw.write(sb.toString());
            res = 1;
        } catch (Exception ex) {
            LOG.severe(ex.getLocalizedMessage());
        }
        return res;
    }

    private void updateTeam13() {
        //if ((m == null) || (m.getRowCount() == 0)) {
        if ((txListTransferPlayers == null) || (txListTransferPlayers.size() < 1)) {
            statusTeam13 = NONE;
            B_updateTeam13.setBackground(Color.red);
            return;
        }

        Runnable task = new Runnable() {
            @Override
            public void run() {
                String lh = "team13: ";
                try {
                    //check if already loaded

                    if (pageTeam13 == null) {
                        log(lh + "loading team13's data from [" + urlTeam13 + "] ...", Color.black);
                        //"file:///D:/BACKUP/team13.html"
                        //"http://fa13.com/team.html?filter=&team=13"
                        BufferedReader br;
                        URL url = new URL(urlTeam13);

                        Object obj = url.openConnection();
                        if (obj instanceof HttpURLConnection) {
                            HttpURLConnection con = (HttpURLConnection) obj;
                            br = new BufferedReader(new InputStreamReader(con.getInputStream(),pageTeam13CharSet));
                            log(lh + "content-length = " + con.getContentLength(), Color.black);
                            log(lh + "content-type = " + con.getContentType(), Color.black);
                            log(lh + "content-encoding = " + con.getContentEncoding(), Color.black);
                        } else {
                            FileURLConnection con = (FileURLConnection) obj;
                            br = new BufferedReader(new InputStreamReader(con.getInputStream(),pageTeam13CharSet));
                            log(lh + "content-length = " + con.getContentLength(), Color.black);
                            log(lh + "content-type = " + con.getContentType(), Color.black);
                            log(lh + "content-encoding = " + con.getContentEncoding(), Color.black);
                        }
                        StringBuilder sb = new StringBuilder();
                        //BufferedReader br = new BufferedReader( new FileReader(fileName) );
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        pageTeam13 = sb;
                        if (br != null) {
                            br.close();
                        }
                        log(lh + "loaded " + sb.length() + " symbols", Color.blue);
                        writeFile(localTeam13, sb);
                    } else {
                        log(lh + "loading from local cache...", Color.blue);
                    }

                    processTeam13Data(lh);
                    statusTeam13 = OK;
                } catch (MalformedURLException ex) {
                    log(lh + "Bad team13 url", Color.red);
                    statusTeam13 = ERR;
                    Exceptions.printStackTrace(ex);
                    loadLocalTeam13();
                } catch (IOException ex) {
                    log(lh + "connection error", Color.red);
                    statusTeam13 = ERR;
                    Exceptions.printStackTrace(ex);
                    loadLocalTeam13();
                }
                if (statusTeam13 == OK) {
                    B_updateTeam13.setBackground(Color.green);
                } else {
                    B_updateTeam13.setBackground(Color.red);
                }
                //B_updateTeam13.setToolTipText(team);

            }
        };
        ProgressUtils.showProgressDialogAndRun(task, getRes("UPDATE_PLAYERS_FROM_13"));//"Updating players from Bank13...");

    }

    private void processTeam13Data(String loghead) {
        int begin = pageTeam13.indexOf(getRes("TEAM13_PARSE_FROM"));
        if (begin == -1) {
            return;
        }
        String[] cols = new String[16];

        DefaultTableModel mm = Fa13Utils.parseHtmlTable(pageTeam13.toString().substring(begin), 16, cols);
        mm.removeRow(0);
        int size2 = (mm.getRowCount() - 1) / 2;
        //String date = mm.getValueAt(size2 - 1, 0).toString(); //update timeS
        //log(loghead + "datestamp is [" + date + "]", Color.blue);

        log(loghead + "found " + size2 + " players in team13", Color.blue);
        B_updateTeam13.setToolTipText(size2 + " players updated from local");
        //L4.setText(s);
        //mm.removeRow(size-1);
        //mm.removeRow(size-2);
        //mm.removeRow(size-3);
/*
         m.setEventsEnabled(false);
         int size = m.getRowCount();
         for (int i = 1; i < size2; i++) {
         if (mm.getValueAt(i, 1) == null) {
         continue;
         }
         for (int row = 0; row < size; row++) {
         if ((mm.getValueAt(i, 1).toString().matches(m.getValueAt(row, 1).toString() + ".*")) && (m.getValueAt(row, 4).equals(mm.getValueAt(i, 2)))) {
         m.setValueAt(mm.getValueAt(i, 12), row, 12); //abilities2
         m.setValueAt(mm.getValueAt(i, 6), row, 13); //exp
         m.setValueAt(mm.getValueAt(i, 15), row, 15); //tur
         break; // relation 1:1
         }
         }
         }
         m.setEventsEnabled(true);
         */
        txListTransferPlayers.getReadWriteLock().writeLock().lock();
        txListTransferPlayers.beginEvent();
        try {
            int updated = Fa13Utils.updateTransferPlayersFromTable(txListTransferPlayers, mm);
        } finally {
            txListTransferPlayers.commitEvent();
            txListTransferPlayers.getReadWriteLock().writeLock().unlock();
        }
    }

    private void loadLocalTeam13() {
        //into pageTeam13
        pageTeam13 = CU.readFile(localTeam13);
        processTeam13Data("team13 local: ");
    }

    private void CHB_useFilterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_useFilterItemStateChanged
        toggleFilter(CHB_useFilter.isSelected());
    }//GEN-LAST:event_CHB_useFilterItemStateChanged

    private void toggleSvFilter(JCheckBox chb, MatcherEditor me) {
        if (CHB_svFilter.isSelected()) {
            if (chb.isSelected()) {
                matcherEditorsList.add(me);
            } else {
                matcherEditorsList.remove(me);
            }
        }
    }

    private void CHB_pItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_pItemStateChanged
        toggleSvFilter(CHB_p, mePassing);
        boolean flag = CHB_p.isSelected();
        S_p1.setEnabled(flag);
        S_p2.setEnabled(flag);
    }//GEN-LAST:event_CHB_pItemStateChanged

    private void CHB_uItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_uItemStateChanged
        toggleSvFilter(CHB_u, meShooting);
        boolean flag = CHB_u.isSelected();
        S_u1.setEnabled(flag);
        S_u2.setEnabled(flag);
    }//GEN-LAST:event_CHB_uItemStateChanged

    private void CHB_nItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_nItemStateChanged
        toggleSvFilter(CHB_n, meCrossing);
        boolean flag = CHB_n.isSelected();
        S_n1.setEnabled(flag);
        S_n2.setEnabled(flag);
    }//GEN-LAST:event_CHB_nItemStateChanged

    private void CHB_dItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_dItemStateChanged
        toggleSvFilter(CHB_d, meDribbling);
        boolean flag = CHB_d.isSelected();
        S_d1.setEnabled(flag);
        S_d2.setEnabled(flag);
    }//GEN-LAST:event_CHB_dItemStateChanged

    private void CHB_oItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_oItemStateChanged
        toggleSvFilter(CHB_o, meTackling);
        boolean flag = CHB_o.isSelected();
        S_o1.setEnabled(flag);
        S_o2.setEnabled(flag);
    }//GEN-LAST:event_CHB_oItemStateChanged

    private void CHB_vgItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_vgItemStateChanged
        toggleSvFilter(CHB_vg, meHeading);
        boolean flag = CHB_vg.isSelected();
        S_vg1.setEnabled(flag);
        S_vg2.setEnabled(flag);
    }//GEN-LAST:event_CHB_vgItemStateChanged

    private void CHB_sItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_sItemStateChanged
        toggleSvFilter(CHB_s, meSpeed);
        boolean flag = CHB_s.isSelected();
        S_s1.setEnabled(flag);
        S_s2.setEnabled(flag);
    }//GEN-LAST:event_CHB_sItemStateChanged

    private void CHB_fItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_fItemStateChanged
        toggleSvFilter(CHB_f, meStamina);
        boolean flag = CHB_f.isSelected();
        S_f1.setEnabled(flag);
        S_f2.setEnabled(flag);
    }//GEN-LAST:event_CHB_fItemStateChanged

    private void CHB_rItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_rItemStateChanged
        toggleSvFilter(CHB_r, meReflexes);
        boolean flag = CHB_r.isSelected();
        S_r1.setEnabled(flag);
        S_r2.setEnabled(flag);
    }//GEN-LAST:event_CHB_rItemStateChanged

    private void CHB_tItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_tItemStateChanged
        toggleSvFilter(CHB_t, meHandling);
        boolean flag = CHB_t.isSelected();
        S_t1.setEnabled(flag);
        S_t2.setEnabled(flag);
    }//GEN-LAST:event_CHB_tItemStateChanged

    private void CHB_svFilterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CHB_svFilterItemStateChanged
        toggleAbilitiesFilter(CHB_svFilter.isSelected());
    }//GEN-LAST:event_CHB_svFilterItemStateChanged

    private void B_reloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_reloadActionPerformed
        reloadData();
    }//GEN-LAST:event_B_reloadActionPerformed

    private void B_openAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_openAllActionPerformed
        //file dialog options
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(fa13FilesFilter);

        file = new File(fileAll);
        fileChooser.setCurrentDirectory(file);
        fileChooser.setSelectedFile(file);
        //fileChooser.setFileFilter( schFilter );
        fileChooser.setVisible(true);
        if (this.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            fileAll = file.getPath();
            B_openAll.setToolTipText(fileAll);
            //doLoadAll(false);
            //m.fireTableDataChanged();
        }
    }//GEN-LAST:event_B_openAllActionPerformed

    private void B_updateTeam13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_updateTeam13ActionPerformed
        updateTeam13();
    }//GEN-LAST:event_B_updateTeam13ActionPerformed

    private void B_resetMainFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_resetMainFilterActionPerformed
        CHB_useFilter.setSelected(false);
        S_age1.setValue(10);
        S_age2.setValue(99);

        S_str1.setValue(1);
        S_str2.setValue(199);

        S_sal1.setValue(0);
        S_sal2.setValue(999);

        S_tal1.setValue(20);
        S_tal2.setValue(199);

        S_h1.setValue(20);
        S_h2.setValue(100);

        S_exp1.setValue(0);
        S_exp2.setValue(199);

        S_tick1.setValue(0);
        S_tick2.setValue(199);

        S_pr1.setValue(0);
        S_pr2.setValue(99999);

        TF_pos.setText("");
        TF_name.setText("");
        TF_country.setText("");

    }//GEN-LAST:event_B_resetMainFilterActionPerformed

    private void B_resetSvFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_B_resetSvFilterActionPerformed
        CHB_svFilter.setSelected(false);
        S_s1.setValue(20);
        S_s2.setValue(199);

        S_f1.setValue(20);
        S_f2.setValue(199);

        S_p1.setValue(20);
        S_p2.setValue(199);

        S_n1.setValue(20);
        S_n2.setValue(199);

        S_o1.setValue(20);
        S_o2.setValue(199);

        S_vg1.setValue(20);
        S_vg2.setValue(199);

        S_u1.setValue(20);
        S_u2.setValue(199);

        S_d1.setValue(20);
        S_d2.setValue(199);

        S_t1.setValue(20);
        S_t2.setValue(199);

        S_r1.setValue(20);
        S_r2.setValue(199);

    }//GEN-LAST:event_B_resetSvFilterActionPerformed

    private void S_pr2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_pr2StateChanged
        mePrice.setRange(CU.getInt(S_pr1, 0), CU.getInt(S_pr2, 99999));
    }//GEN-LAST:event_S_pr2StateChanged

    private void S_pr1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_pr1StateChanged
        mePrice.setRange(CU.getInt(S_pr1, 0), CU.getInt(S_pr2, 99999));
    }//GEN-LAST:event_S_pr1StateChanged

    private void S_tick2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_tick2StateChanged
        meTickets.setRange(CU.getInt(S_tick1, 0), CU.getInt(S_tick2, 100));
    }//GEN-LAST:event_S_tick2StateChanged

    private void S_tick1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_tick1StateChanged
        meTickets.setRange(CU.getInt(S_tick1, 0), CU.getInt(S_tick2, 100));
    }//GEN-LAST:event_S_tick1StateChanged

    private void S_exp2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_exp2StateChanged
        meExp.setRange(CU.getInt(S_exp1, 1), CU.getInt(S_exp2, 100));
    }//GEN-LAST:event_S_exp2StateChanged

    private void S_exp1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_exp1StateChanged
        meExp.setRange(CU.getInt(S_exp1, 1), CU.getInt(S_exp2, 100));
    }//GEN-LAST:event_S_exp1StateChanged

    private void S_h2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_h2StateChanged
        meHealth.setRange(CU.getInt(S_h1, 1), CU.getInt(S_h2, 100));
    }//GEN-LAST:event_S_h2StateChanged

    private void S_h1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_h1StateChanged
        meHealth.setRange(CU.getInt(S_h1, 1), CU.getInt(S_h2, 100));
    }//GEN-LAST:event_S_h1StateChanged

    private void S_tal2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_tal2StateChanged
        meTalent.setRange(CU.getInt(S_tal1, 1), CU.getInt(S_tal2, 100));
    }//GEN-LAST:event_S_tal2StateChanged

    private void S_tal1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_tal1StateChanged
        meTalent.setRange(CU.getInt(S_tal1, 1), CU.getInt(S_tal2, 100));
    }//GEN-LAST:event_S_tal1StateChanged

    private void S_sal2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_sal2StateChanged
        meSalary.setRange(CU.getInt(S_sal1, 1), CU.getInt(S_sal2, 499));
    }//GEN-LAST:event_S_sal2StateChanged

    private void S_sal1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_sal1StateChanged
        meSalary.setRange(CU.getInt(S_sal1, 1), CU.getInt(S_sal2, 499));
    }//GEN-LAST:event_S_sal1StateChanged

    private void S_str2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_str2StateChanged
        meStrenght.setRange(CU.getInt(S_str1, 1), CU.getInt(S_str2, 100));
    }//GEN-LAST:event_S_str2StateChanged

    private void S_str1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_str1StateChanged
        meStrenght.setRange(CU.getInt(S_str1, 1), CU.getInt(S_str2, 100));
    }//GEN-LAST:event_S_str1StateChanged

    private void S_age2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_age2StateChanged
        meAge.setRange(CU.getInt(S_age1, 1), CU.getInt(S_age2, 99));
        //applyFilter();
    }//GEN-LAST:event_S_age2StateChanged

    private void S_age1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_S_age1StateChanged
        meAge.setRange(CU.getInt(S_age1, 1), CU.getInt(S_age2, 99));
        //applyFilter();
    }//GEN-LAST:event_S_age1StateChanged

    private void applyComplexFilter() {
        if (!CHB_svFilter.isSelected()) {
            return;
        }

//        m.setComplexColumnFilter(12, getRes("U"), CU.getFloat(S_u1, 20), CU.getFloat(S_u2, 20), CHB_u.isSelected());
//        m.setComplexColumnFilter(12, getRes("P"), CU.getFloat(S_p1, 20), CU.getFloat(S_p2, 20), CHB_p.isSelected());
//        m.setComplexColumnFilter(12, getRes("N"), CU.getFloat(S_n1, 20), CU.getFloat(S_n2, 20), CHB_n.isSelected());
//        m.setComplexColumnFilter(12, getRes("D"), CU.getFloat(S_d1, 20), CU.getFloat(S_d2, 20), CHB_d.isSelected());
//        m.setComplexColumnFilter(12, getRes("O"), CU.getFloat(S_o1, 20), CU.getFloat(S_o2, 20), CHB_o.isSelected());
//        m.setComplexColumnFilter(12, getRes("VG"), CU.getFloat(S_vg1, 20), CU.getFloat(S_vg2, 20), CHB_vg.isSelected());
//        m.setComplexColumnFilter(12, getRes("S"), CU.getFloat(S_s1, 20), CU.getFloat(S_s2, 20), CHB_s.isSelected());
//        m.setComplexColumnFilter(12, getRes("F"), CU.getFloat(S_f1, 20), CU.getFloat(S_f2, 20), CHB_f.isSelected());
//        m.setComplexColumnFilter(12, getRes("R"), CU.getFloat(S_r1, 20), CU.getFloat(S_r2, 20), CHB_r.isSelected());
//        m.setComplexColumnFilter(12, getRes("T"), CU.getFloat(S_t1, 20), CU.getFloat(S_t2, 20), CHB_t.isSelected());
    }

    private void applyFilter() {
        if (!CHB_useFilter.isSelected()) {
            return;
        }
//  0      1        2        3       4      5       6         7          8         9          10         11         12          13     14
// "N", "Name", "Country", "Club", "Pos", "Age", "Talent", "Salary", "Stregth", "Health", "Abilities", "Price", "Abilities2", "Exp", "Tickets"
//        m.setColumnFilter(5, CU.getInt(S_age1, 1), CU.getInt(S_age2, 99));
//        m.setColumnFilter(6, CU.getInt(S_tal1, 1), CU.getInt(S_tal2, 99));
//        m.setColumnFilter(7, CU.getInt(S_sal1, 1), CU.getInt(S_sal2, 99));
//        m.setColumnFilter(8, CU.getInt(S_str1, 1), CU.getInt(S_str2, 99));
//        m.setColumnFilter(9, CU.getInt(S_h1, 1), CU.getInt(S_h2, 99));
//        //m.setColumnFilter(10, CU.getInt(S_age1, 1), CU.getInt(S_age2, 99));
//        m.setColumnFilter(11, CU.getInt(S_pr1, 1), CU.getInt(S_pr2, 99));
//        //m.setColumnFilter(12, CU.getInt(S_age1, 1), CU.getInt(S_age2, 99));
//        m.setColumnFilter(13, CU.getInt(S_exp1, 1), CU.getInt(S_exp2, 99));
//        m.setColumnFilter(14, CU.getInt(S_tick1, 1), CU.getInt(S_tick2, 99));
    }

    private void doLoadAll(boolean checkStatus) {
        if ((checkStatus) && (statusAll == OK)) {
            return;
        }
        //do load
        Runnable r = new Runnable() {
            @Override
            public void run() {

                final int ret = loadAll(fileAll);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ret != -1) {
                            updatePlayersFromAll(all_data);
                            //JOptionPane.showMessageDialog(instance, "TList is loaded");
                            //L1.setForeground(Color.BLACK);
                            //L1.setText("TList is loaded. date: " + tlist_date);
                            log("ALL is loaded. date: " + all_date, Color.blue);
                        } else {
                            //JOptionPane.(instance, "Error");
                            //L1.setForeground(Color.RED);
                            //L1.setText("TList is NOT loaded.");
                            log("ALL is NOT loaded.", Color.RED);
                        }
                    }
                });
            }
        };
        ProgressUtils.showProgressDialogAndRun(r, getRes("RELOAD_ALL"));//"Reloading ALL list...");
    }

    private void doLoadTList(boolean checkStatus) {
        if ((checkStatus) && (statusTlist == OK)) {
            return;
        }
        //do load
        Runnable r = new Runnable() {
            @Override
            public void run() {

                final int ret = loadTlist(fileTlist);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ret != -1) {
                            //JOptionPane.showMessageDialog(instance, "TList is loaded");
                            //L1.setForeground(Color.BLACK);
                            //L1.setText("TList is loaded. date: " + tlist_date);
                            log("TList is loaded. date: " + tlist_date, Color.blue);
                        } else {
                            //JOptionPane.showMessageDialog(instance, "Error");
                            //L1.setForeground(Color.RED);
                            //L1.setText("TList is NOT loaded.");
                            log("TList is NOT loaded.", Color.RED);
                        }
                    }
                });
            }
        };
        ProgressUtils.showProgressDialogAndRun(r, getRes("RELOAD_TLIST"));//"Reloading Transfer list...");

    }

    private void doLoadAlt(boolean checkStatus) {
        Runnable r;
        if ((checkStatus) && (statusAltlist == OK)) {
            //just update
            r = new Runnable() {
                @Override
                public void run() {
                    updatePlayersFromAll(alt_data);
                    return;
                }
            };
        } else {
            //do load
            r = new Runnable() {
                @Override
                public void run() {
                    final int ret = loadAltlist(fileAltlist);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ret != -1) {
                                updatePlayersFromAll(alt_data);
                                log("AltList is loaded. date: " + alt_date, Color.blue);
                            } else {
                                log("AltList is NOT loaded.", Color.RED);
                            }
                        }
                    });
                }
            };
        }
        ProgressUtils.showProgressDialogAndRun(r, getRes("RELOAD_PLAYERS_ON_TRANS"));//"Reloading Players on Transfer list...");
        applyFilter();

        applyComplexFilter();
    }

    private void log(String mes, Color clr) {
        if (io_log == null) {
            io_log = IOProvider.getDefault().getIO("Log", true);
        }
        if (io_log == null) {
            return;
        }
        if (clr == null) {
            clr = Color.BLACK;
        }
        try {
            IOColorLines.println(io_log, mes, clr);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
    /*
     private void addPlayer(FilteredTableModel m, String s) {
     String ss[] = s.split("/");
     m.addRow(ss);
    
     MyTransferPlayer tp = new MyTransferPlayer();
    
     listTransferPlayers.add(getTransferPlayer(ss));
    
     }
     */
    /*
     39676/Уго Галеано/Парагвай/Депортиво (Кв)/ВР/23/90/198/73/100/РТГФ/11448/
     0      1          2    3  4  5  6  7  8  9  10  11    12  13 14 15 16 17 18 19 20 21 22 2324 252627282930 31 32 33 34  35  36       37            38     39 40  41
     /1/Уго Галеано/Парагвай/ВР/23/90/74/90/90/73/100/11448/198/20/20/20/20/20/79/26/40/60/52/0/30/0/0/0/0/0/0/280/278/0/0/10303/0/rg-Депортиво (Кв)/2007-01-15/0/0/39676/
     т52р60вг79  с26ф40
    
     33296/Джим Магилтон/Сев. Ирландия/Нордсьюлланд/ЦП/26/100/143/64/100/УПДОСФ/2489/
     13 14 15 16 17 18 19 20 21 22 2324
     /22/Джим Магилтон/Сев. Ирландия/ЦП/26/100/64/90/90/64/100/2489/143/43/60/21/40/40/20/52/40/20/20/0/30/0/0/0/0/0/0/291/34/0/0/2240/0/rg-Шеффилд Уэнсдей/2006-01-16/0/0/33296/
     у43п60н21д40о40с52ф40
     */
    /*
     private MyTransferPlayer getTransferPlayer(String[] ss) {
     MyTransferPlayer tp = new MyTransferPlayer();
     tp.setId(Integer.valueOf(ss[0]));
     tp.setName(ss[1]);
     tp.setNationalityCode(ss[2]);//??? GER RUS
     tp.setPreviousTeam(ss[3]);
     tp.setPosition(Player.positions.get(ss[4]));
     tp.setAge(Integer.valueOf(ss[5]));
     tp.setTalent(Integer.valueOf(ss[6]));
     tp.setSalary(Integer.valueOf(ss[7]));
     tp.setStrength(Integer.valueOf(ss[8]));
     tp.setHealth(Integer.valueOf(ss[9]));
     tp.setAbilities(ss[10]);
     tp.setPrice(Integer.valueOf(ss[11]));
     return tp;
     }
     */
    /*
     private void updatePlayerFromAlt(FilteredTableModel m, String s) {
     String ss[] = s.substring(1).split("/");
     StringBuilder ab = new StringBuilder();
     int size = m.getRowCount();
     for (int row = 0; row < size; row++) {
     if ((m.getValueAt(row, 1).equals(ss[1]))
     && (m.getValueAt(row, 2).equals(ss[2]))
     && (m.getValueAt(row, 4).equals(ss[3]))) {
     //update
    
     //                if (Integer.valueOf(ss[13]).intValue()>20) ab.append("u").append(ss[13]);
     //                if (Integer.valueOf(ss[14]).intValue()>20) ab.append("p").append(ss[14]);
     //                if (Integer.valueOf(ss[15]).intValue()>20) ab.append("n").append(ss[15]);
     //                if (Integer.valueOf(ss[16]).intValue()>20) ab.append("d").append(ss[16]);
     //                if (Integer.valueOf(ss[17]).intValue()>20) ab.append("o").append(ss[17]);
     //                if (Integer.valueOf(ss[18]).intValue()>20) ab.append("vg").append(ss[18]);
     //                if (Integer.valueOf(ss[19]).intValue()>20) ab.append("s").append(ss[19]);
     //                if (Integer.valueOf(ss[20]).intValue()>20) ab.append("f").append(ss[20]);
     //                if (Integer.valueOf(ss[21]).intValue()>20) ab.append("r").append(ss[21]);
     //                if (Integer.valueOf(ss[22]).intValue()>20) ab.append("t").append(ss[22]);
    
     if (Integer.valueOf(ss[13]).intValue() > 20) {
     ab.append(getRes("U")).append(ss[13]);
     }
     if (Integer.valueOf(ss[14]).intValue() > 20) {
     ab.append(getRes("P")).append(ss[14]);
     }
     if (Integer.valueOf(ss[15]).intValue() > 20) {
     ab.append(getRes("N")).append(ss[15]);
     }
     if (Integer.valueOf(ss[16]).intValue() > 20) {
     ab.append(getRes("D")).append(ss[16]);
     }
     if (Integer.valueOf(ss[17]).intValue() > 20) {
     ab.append(getRes("O")).append(ss[17]);
     }
     if (Integer.valueOf(ss[18]).intValue() > 20) {
     ab.append(getRes("VG")).append(ss[18]);
     }
     if (Integer.valueOf(ss[19]).intValue() > 20) {
     ab.append(getRes("S")).append(ss[19]);
     }
     if (Integer.valueOf(ss[20]).intValue() > 20) {
     ab.append(getRes("F")).append(ss[20]);
     }
     if (Integer.valueOf(ss[21]).intValue() > 20) {
     ab.append(getRes("R")).append(ss[21]);
     }
     if (Integer.valueOf(ss[22]).intValue() > 20) {
     ab.append(getRes("T")).append(ss[22]);
     }
     m.setValueAt(ab.toString(), row, 12);//ability2
     m.setValueAt(ss[6], row, 13);//exp
     ab.setLength(0);
     break;// relation 1:1
     }
    
     }
     }
     */

    private int loadAll(String fileName) {
        if ((fileName == null) || (fileName.length() == 0)) {
            String s = "Bad ALL fileName: ALL is not loaded!";
            log(s, Color.RED);
            B_openAll.setBackground(Color.red);
            statusAll = ERR;
            return -1;
        }
        try {
            all_data = AllReader.readAllFile(fileName);
            //updatePlayersFromAll(all_data);
            //CentralLookup.getDefault().add(all_data);
            statusAll = OK;
        } catch (ReaderException ex) {
            //Exceptions.printStackTrace(ex);
        }

        if (statusAll == OK) {
            B_openAll.setBackground(Color.green);
        } else {
            B_openAll.setBackground(Color.red);
        }
        //Calendar c = Calendar.getInstance();
        //c.setTime(all_data.getDate());
        //String date = "" + c.get(Calendar.DAY_OF_MONTH) + c.get(Calendar.MONTH) + c.get(Calendar.YEAR);
        if (all_data != null) {
            B_openAll.setToolTipText("[" + df.format(all_data.getDate()) + "]  " + fileName);
        }
        return statusAll;
    }

    private static String getPlayerSkills(Player p) {
        StringBuilder ab = new StringBuilder();
        if (p.getShooting() > 20) {
            ab.append(getRes("U")).append(p.getShooting());
        }
        if (p.getPassing() > 20) {
            ab.append(getRes("P")).append(p.getPassing());
        }
        if (p.getCross() > 20) {
            ab.append(getRes("N")).append(p.getCross());
        }
        if (p.getDribbling() > 20) {
            ab.append(getRes("D")).append(p.getDribbling());
        }
        if (p.getTackling() > 20) {
            ab.append(getRes("O")).append(p.getTackling());
        }
        if (p.getHeading() > 20) {
            ab.append(getRes("VG")).append(p.getHeading());
        }
        if (p.getSpeed() > 20) {
            ab.append(getRes("S")).append(p.getSpeed());
        }
        if (p.getStamina() > 20) {
            ab.append(getRes("F")).append(p.getStamina());
        }
        if (p.getReflexes() > 20) {
            ab.append(getRes("R")).append(p.getReflexes());
        }
        if (p.getHandling() > 20) {
            ab.append(getRes("T")).append(p.getHandling());
        }
        return ab.toString();
    }

    /**
     * here All from alt have WRONG bithTour for players = 0 !!!! so hack
     * applied
     *
     * @param all
     */
    private void updatePlayersFromAll(All all) {
        //if (1==1) return;
        //if ((all == null) || (m == null)) {
        if ((all == null) || (txListTransferPlayers == null) || (txListTransferPlayers.size() < 1)) {
            return;
        }
        txListTransferPlayers.getReadWriteLock().writeLock().lock();
        txListTransferPlayers.beginEvent();
        try {
            String ss;
            for (MyTransferPlayer tp : txListTransferPlayers) {
                //ss = m.getValueAt(i, 3).toString();
                ss = tp.getPreviousTeam();
                //remove * from bank13 players team
                if (ss.endsWith("*")) {
                    ss = ss.substring(0, ss.length() - 1);
                }
                for (Team t : all.getTeams()) {
                    if (t.getName().equalsIgnoreCase(ss)) {
                        for (Player p : t.getPlayers()) {
                            if (tp.getId() == p.getId()) {
                                tp.copyAbilitiesFrom(p);
                                tp.setAbilities(getPlayerSkills(p));
                                tp.setExperience(p.getExperience());
                                //hack !!!
                                if (tp.getBirthtour() == 0) {
                                    tp.setBirthtour(p.getBirthtour());//birth tur
                                }
                                //also add fitness !!!
                                if (tp.getFitness() == 0) {
                                    tp.setFitness(p.getFitness());
                                }
                                break;
                            }
                        }
                    }
                }
            }
            Fa13Utils.updateTransferPlayersFromAll(txListTransferPlayers, all);
        } finally {
            txListTransferPlayers.commitEvent();
            txListTransferPlayers.getReadWriteLock().writeLock().unlock();
        }
    }

    /*
     *
     if ((m.getValueAt(row, 1).equals(ss[1]))
     && (m.getValueAt(row, 2).equals(ss[2]))
     && (m.getValueAt(row, 4).equals(ss[3]))) {
     //update
    
     if (Integer.valueOf(ss[13]).intValue() > 20) {
     ab.append(getRes("U")).append(ss[13]);
     }
     if (Integer.valueOf(ss[14]).intValue() > 20) {
     ab.append(getRes("P")).append(ss[14]);
     }
     if (Integer.valueOf(ss[15]).intValue() > 20) {
     ab.append(getRes("N")).append(ss[15]);
     }
     if (Integer.valueOf(ss[16]).intValue() > 20) {
     ab.append(getRes("D")).append(ss[16]);
     }
     if (Integer.valueOf(ss[17]).intValue() > 20) {
     ab.append(getRes("O")).append(ss[17]);
     }
     if (Integer.valueOf(ss[18]).intValue() > 20) {
     ab.append(getRes("VG")).append(ss[18]);
     }
     if (Integer.valueOf(ss[19]).intValue() > 20) {
     ab.append(getRes("S")).append(ss[19]);
     }
     if (Integer.valueOf(ss[20]).intValue() > 20) {
     ab.append(getRes("F")).append(ss[20]);
     }
     if (Integer.valueOf(ss[21]).intValue() > 20) {
     ab.append(getRes("R")).append(ss[21]);
     }
     if (Integer.valueOf(ss[22]).intValue() > 20) {
     ab.append(getRes("T")).append(ss[22]);
     }
     m.setValueAt(ab.toString(), row, 12);//ability2
     m.setValueAt(ss[6], row, 13);//exp
     */
    private int loadAltlist(String fileName) {
        if ((fileName == null) || (fileName.length() == 0)) {
            String s = "Bad altlist fileName: altlist is not loaded!";
            log(s, Color.RED);
            B_openAList.setBackground(Color.red);
            statusAltlist = ERR;
            return -1;
        }
        try {
            alt_data = AllReader.readAllFile(fileName);
            //updatePlayersFromAll(alt_data);
            statusAltlist = OK;
        } catch (ReaderException ex) {
            statusAltlist = ERR;
            Exceptions.printStackTrace(ex);
        }
        /*
         BufferedReader br = null;
        
         ArrayList<String> lines = new ArrayList<String>();
         int res = 1;
         try {
        
         br = new BufferedReader(new FileReader(fileName));
         String line = null;
        
         while ((line = br.readLine()) != null) {
         lines.add(line);
         }
        
         } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
         res = -1;
         } finally {
         try {
         br.close();
         } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
         res = -1;
         }
         }
         if ((res != -1) && (lines.get(0).equals("format=4"))) {
         alt_date = lines.get(1);
         //String pattern = "^(\\d+)(;\\d+)*$";
         String pattern = "^(/[^/]*){42}/$";
        
         ProgressHandle handle = ProgressHandleFactory.createHandle("Updating players from ALT");
        
         int size = lines.size();
         handle.start(size);
         for (int i = 0; i < size; i++) {
         if (lines.get(i).matches(pattern)) {
         updatePlayerFromAlt(m, lines.get(i));
         }
         handle.progress(i);
         }
         handle.finish();
         altlist_status = OK;
         } else {
         altlist_status = ERR;
         }
         */
        if (statusAltlist == OK) {
            B_openAList.setBackground(Color.green);
        } else {
            B_openAList.setBackground(Color.red);
        }
        //Calendar c = Calendar.getInstance();
        //c.setTime(all_data.getDate());
        //String date = "" + c.get(Calendar.DAY_OF_MONTH) + c.get(Calendar.MONTH) + c.get(Calendar.YEAR);
        if (alt_data != null) {
            B_openAList.setToolTipText("[" + df.format(alt_data.getDate()) + "]  " + fileName);
        }
        return statusAltlist;
    }

    private int loadTlist(String fileName) {

        if ((fileName == null) || (fileName.length() == 0)) {
            String s = "Bad tlist fileName: tlist is not loaded!";
            log(s, Color.RED);
            //B_openTList.setToolTipText(s);
            B_openTList.setBackground(Color.red);
            statusTlist = ERR;
            return -1;
        }
        try {
            transferList = TransferListReader.readTransferListFile(fileName);
            tlist_date = df.format(transferList.getDate());
            txListTransferPlayers.getReadWriteLock().writeLock().lock();
            txListTransferPlayers.beginEvent();
            try {
                txListTransferPlayers.clear();
                for (TransferPlayer tp : transferList.getPlayers()) {
//                    if (tp.getName().startsWith("Стиг Ар")) {
//                        int i = 1;
//                    }
                    MyTransferPlayer my_tp = new MyTransferPlayer(tp);
                    txListTransferPlayers.add(my_tp);
                }
            } finally {
                txListTransferPlayers.commitEvent();
                txListTransferPlayers.getReadWriteLock().writeLock().unlock();
            }
            statusTlist = OK;
        } catch (ReaderException ex) {
            Exceptions.printStackTrace(ex);
            statusTlist = ERR;
        }

        if (statusTlist == OK) {
            B_openTList.setBackground(Color.green);
        } else {
            B_openTList.setBackground(Color.red);
        }
        B_openTList.setToolTipText("[" + tlist_date + "]  " + fileName);
        return txListTransferPlayers.size();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton B_openAList;
    private javax.swing.JButton B_openAll;
    private javax.swing.JButton B_openTList;
    private javax.swing.JButton B_reload;
    private javax.swing.JButton B_resetMainFilter;
    private javax.swing.JButton B_resetSvFilter;
    private javax.swing.JButton B_updateTeam13;
    private javax.swing.JButton B_updateTickets;
    private javax.swing.JCheckBox CHB_d;
    private javax.swing.JCheckBox CHB_f;
    private javax.swing.JCheckBox CHB_myChoose;
    private javax.swing.JCheckBox CHB_n;
    private javax.swing.JCheckBox CHB_o;
    private javax.swing.JCheckBox CHB_p;
    private javax.swing.JCheckBox CHB_r;
    private javax.swing.JCheckBox CHB_s;
    private javax.swing.JCheckBox CHB_svFilter;
    private javax.swing.JCheckBox CHB_t;
    private javax.swing.JCheckBox CHB_u;
    private javax.swing.JCheckBox CHB_useFilter;
    private javax.swing.JCheckBox CHB_vg;
    private javax.swing.JLabel L44;
    private javax.swing.JPanel P_f1;
    private javax.swing.JPanel P_f2;
    private javax.swing.JPanel P_filter;
    private javax.swing.JPanel P_up;
    private javax.swing.JScrollPane SP_tlist;
    private javax.swing.JSplitPane SS_bot;
    private javax.swing.JSpinner S_age1;
    private javax.swing.JSpinner S_age2;
    private javax.swing.JSpinner S_d1;
    private javax.swing.JSpinner S_d2;
    private javax.swing.JSpinner S_exp1;
    private javax.swing.JSpinner S_exp2;
    private javax.swing.JSpinner S_f1;
    private javax.swing.JSpinner S_f2;
    private javax.swing.JSpinner S_h1;
    private javax.swing.JSpinner S_h2;
    private javax.swing.JSpinner S_n1;
    private javax.swing.JSpinner S_n2;
    private javax.swing.JSpinner S_o1;
    private javax.swing.JSpinner S_o2;
    private javax.swing.JSpinner S_p1;
    private javax.swing.JSpinner S_p2;
    private javax.swing.JSpinner S_pr1;
    private javax.swing.JSpinner S_pr2;
    private javax.swing.JSpinner S_r1;
    private javax.swing.JSpinner S_r2;
    private javax.swing.JSpinner S_s1;
    private javax.swing.JSpinner S_s2;
    private javax.swing.JSpinner S_sal1;
    private javax.swing.JSpinner S_sal2;
    private javax.swing.JSpinner S_str1;
    private javax.swing.JSpinner S_str2;
    private javax.swing.JSpinner S_t1;
    private javax.swing.JSpinner S_t2;
    private javax.swing.JSpinner S_tal1;
    private javax.swing.JSpinner S_tal2;
    private javax.swing.JSpinner S_tick1;
    private javax.swing.JSpinner S_tick2;
    private javax.swing.JSpinner S_u1;
    private javax.swing.JSpinner S_u2;
    private javax.swing.JSpinner S_vg1;
    private javax.swing.JSpinner S_vg2;
    private javax.swing.JTextField TF_country;
    private javax.swing.JTextField TF_name;
    private javax.swing.JTextField TF_pos;
    private javax.swing.JTable T_tlist;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized transfersTopComponent getDefault() {
        if (instance == null) {
            instance = new transfersTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the transfersTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized transfersTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            LOG.warning("Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");

            return getDefault();
        }
        if (win instanceof transfersTopComponent) {
            return (transfersTopComponent) win;
        }
        LOG.warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
        p.setProperty(UI_PREFIX + ".all", fileAll);
        p.setProperty(UI_PREFIX + ".tlist", fileTlist);
        p.setProperty(UI_PREFIX + ".altlist", fileAltlist);
        p.setProperty(UI_PREFIX + ".myChoosePlayerIds", Fa13Utils.asStringList(getMyChoosedPlayers(), ";"));
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
        fileAll = CU.isNull(p.getProperty(UI_PREFIX + ".all"), "all.zip");
        fileTlist = CU.isNull(p.getProperty(UI_PREFIX + ".tlist"), "tlist13.b13");
        fileAltlist = CU.isNull(p.getProperty(UI_PREFIX + ".altlist"), "alltl.b13");
        B_openTList.setToolTipText(fileTlist);
        B_openAList.setToolTipText(fileAltlist);
        B_openAll.setToolTipText(fileAll);

        choosedPlayers = Fa13Utils.asListOfStrings(p.getProperty(UI_PREFIX + ".myChoosePlayerIds"), ";");
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public static String getRes(String resKey) {
        return NbBundle.getMessage(transfersTopComponent.class, resKey);
    }
}
