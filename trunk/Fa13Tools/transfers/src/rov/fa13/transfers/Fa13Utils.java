package rov.fa13.transfers;

import com.fa13.build.model.All;
import com.fa13.build.model.Player;
import com.fa13.build.model.Team;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openide.util.NbBundle;

/**
 *
 * @author MasterOfChaos
 */
public class Fa13Utils {

    private static final Logger LOG = Logger.getLogger(Fa13Utils.class.getName());

    public static void copyPlayerParams(Player srcPlr, Player destPlr) {

        if ((srcPlr == null) || (destPlr == null)) {
            return;
        }
        //copy only abilities
        destPlr.setCross(srcPlr.getCross());
        destPlr.setDribbling(srcPlr.getDribbling());
        destPlr.setHandling(srcPlr.getHandling());
        destPlr.setHeading(srcPlr.getHeading());
        destPlr.setPassing(srcPlr.getPassing());
        destPlr.setReflexes(srcPlr.getReflexes());
        destPlr.setShooting(srcPlr.getShooting());
        destPlr.setSpeed(srcPlr.getSpeed());
        destPlr.setTackling(srcPlr.getTackling());
        destPlr.setStamina(srcPlr.getStamina());

    }
    public static final int CROSSING = 1;
    public static final int DRIBBLING = 2;
    public static final int HANDLING = 3;
    public static final int HEADING = 4;
    public static final int PASSING = 5;
    public static final int REFLEXES = 6;
    public static final int SHOOTING = 7;
    public static final int SPEED = 8;
    public static final int TACKLING = 9;
    public static final int STAMINA = 10;
    final public static Map<Integer, Pattern> abilityPatterns = new HashMap<Integer, Pattern>();
    final private static Pattern digitsPattern = Pattern.compile("\\d+");

    private static void initAbilityPatterns() {
        if (abilityPatterns.size() < 1) {
            abilityPatterns.put(CROSSING, Pattern.compile(getRes("N") + "\\d*"));
            abilityPatterns.put(DRIBBLING, Pattern.compile(getRes("D") + "\\d*"));
            abilityPatterns.put(HANDLING, Pattern.compile(getRes("T") + "\\d*"));
            abilityPatterns.put(HEADING, Pattern.compile(getRes("VG") + "\\d*"));
            abilityPatterns.put(PASSING, Pattern.compile(getRes("P") + "\\d*"));
            abilityPatterns.put(REFLEXES, Pattern.compile(getRes("R") + "\\d*"));
            abilityPatterns.put(SHOOTING, Pattern.compile(getRes("U") + "\\d*"));
            abilityPatterns.put(SPEED, Pattern.compile(getRes("S") + "\\d*"));
            abilityPatterns.put(TACKLING, Pattern.compile(getRes("O") + "\\d*"));
            abilityPatterns.put(STAMINA, Pattern.compile(getRes("F") + "\\d*"));
        }
    }

    private static int extractPlayerAbility(String abilitiesString, int abilityCode) {
        int res = 20;

        Matcher m = abilityPatterns.get(abilityCode).matcher(abilitiesString);
        if (m.find()) {

            m = digitsPattern.matcher(m.group());
            if (m.find()) {
                String ss = m.group();
                res = Integer.valueOf(ss);
            }
        } else {
            res = 40;// f.e. UDS -->U40D40S40
        }
        return res;
    }

    public static void copyAbilitiesToPlayer(String stringAbilities, Player player) {
        initAbilityPatterns();
        player.setCross(extractPlayerAbility(stringAbilities, CROSSING));
        player.setDribbling(extractPlayerAbility(stringAbilities, DRIBBLING));
        player.setHandling(extractPlayerAbility(stringAbilities, HANDLING));
        player.setHeading(extractPlayerAbility(stringAbilities, HEADING));
        player.setPassing(extractPlayerAbility(stringAbilities, PASSING));
        player.setReflexes(extractPlayerAbility(stringAbilities, REFLEXES));
        player.setShooting(extractPlayerAbility(stringAbilities, SHOOTING));
        player.setSpeed(extractPlayerAbility(stringAbilities, SPEED));
        player.setTackling(extractPlayerAbility(stringAbilities, TACKLING));
        player.setStamina(extractPlayerAbility(stringAbilities, STAMINA));
    }

    public static int updateTransferPlayersFromAll(List<MyTransferPlayer> listTransferPlayers, All all) {
        int count = 0;
        for (MyTransferPlayer tp : listTransferPlayers) {
            for (Team t : all.getTeams()) {
                if (t.getName().equalsIgnoreCase(tp.getPreviousTeam())) {
                    for (Player p : t.getPlayers()) {

                        if (p.getId() == tp.getId()) {
                            Fa13Utils.copyPlayerParams(p, tp);
                            count++;
                            break;
                        }
                    }
                }
            }
        }
        return count;
    }

    public static DefaultTableModel parseHtmlTable(String html, int max_cols, String[] columns) {

        DefaultTableModel mm = new DefaultTableModel();
        Vector rowData = new Vector(max_cols);

        Document doc = Jsoup.parse(html);

        Elements trs = doc.getElementsByTag("tr");
        Elements tds;
        boolean colSet = false;
        for (Element tr : trs) {
            if (!colSet) {
                if (columns == null) {
                    tds = tr.getElementsByTag("th");
                    if ((tds != null) && (!tds.isEmpty())) {
                        rowData = new Vector(max_cols);
                        for (Element td : tds) {
                            rowData.add(td.text());
                        }
                        mm.setColumnIdentifiers(rowData);
                    }
                    colSet = true;
                } else {
                    mm.setColumnIdentifiers(columns);
                    colSet = true;
                }
            }
            tds = tr.getElementsByTag("td");
            if (tds != null) {
                rowData = new Vector(max_cols);
                for (Element td : tds) {
                    rowData.add(td.text());
                }
                mm.addRow(rowData);
            }
        }
        return mm;
    }

//typically its a update from table parsed from HTML page
//update transfer players from tableModel
//tableModel's format
//0 = Number    1 = Name    2 = Pos     3 = Nationality    4 = Age    5 = Talent    6 = Exp    7 = Strenght
//8 = physics   9 = morale   10 = health   11 = TeamWork   12 = Abilities   13 = Salary   14 = Price  15 = BirthTour
    public static int updateTransferPlayersFromTable(List<MyTransferPlayer> listTransferPlayers, TableModel tableModel) {

        if ((listTransferPlayers == null) || (tableModel == null)) {
            return -1;
        }

        int count = 0;

        int size = tableModel.getRowCount();
        Object o = null;
        String pName = null;
        //Set set = new HashSet();
        boolean[] found = new boolean[size];
        for (int i = 0; i < found.length; i++) {
            found[i] = false;
        }

        boolean foundFlag = false;
        for (MyTransferPlayer tp : listTransferPlayers) {
            if (!tp.getPreviousTeam().endsWith("*")) {
                continue;
            }
            foundFlag = false;
            //LOG.info("team13 player: " + tp.getName());
            for (int row = 0; row < size; row = row + 2) {
                if (found[row]) {//(set.contains(row)) {
                    continue;//avoid iterate over proccessed already
                }
                o = tableModel.getValueAt(row, 1);
                pName = (o == null ? null : o.toString());
//                    if (pName!=null && pName.startsWith("Франческо") && tp.getName().startsWith("Франческо")) {
//                       int i = 2 +1 ;
//                    }
//                if (tp.getName().startsWith("Стиг Арильд")) {
//                    int i = 2 + 1;
//                }X1`X`X`
                if (pName!=null) {
                    pName = pName.replaceAll("\\s+", " ");
                    if (pName.endsWith(" "+getRes("MAIN_TEAM13_PNAME_PREFIX"))) {
                        pName = pName.replaceAll(" "+getRes("MAIN_TEAM13_PNAME_PREFIX"), "");
                    }
                }
                        
                        
                
                String ss = tp.getName().replaceAll("\\s+", " ");
                if (pName != null && !pName.isEmpty()
                        && (pName.equalsIgnoreCase(ss)))   {
                    //&& (pName.substring(0,
                    //pName.length() < ss.length() ? pName.length() : ss.length()).equalsIgnoreCase(ss))) {
//                    if (pName!=null && pName.startsWith("Франческо")) {
//                       int i = 2 +1 ;
//                    }
                    //set.add(row);
                    found[row] = true;
                    tp.setAbilities(tableModel.getValueAt(row, 12).toString());
                    copyAbilitiesToPlayer(tp.getAbilities(), tp);
                    tp.setExperience(Integer.valueOf(tableModel.getValueAt(row, 6).toString()));
                    //hack
                    if (tp.getBirthtour() == 0) {
                        tp.setBirthtour(Integer.valueOf(tableModel.getValueAt(row, 15).toString()));
                    }
                    //get fitness
                    if (tp.getFitness() == 0) {
                        tp.setFitness(Integer.valueOf(tableModel.getValueAt(row, 8).toString()));
                    }
                    count++;
                    //LOG.info("..... count=" + count + " " + ss);
                    break; // relation 1:1
                }
            }
        }

        return count;
    }

    public static int updateTransferPlayersTicketsFromTable(List<MyTransferPlayer> listTransferPlayers, TableModel tableModel, int startRow, int ticketsColumnIndex) {

        if ((listTransferPlayers == null) || (tableModel == null)) {
            return -1;
        }

        int count = 0;

        int size = tableModel.getRowCount();

        for (MyTransferPlayer tp : listTransferPlayers) {

            for (int row = startRow; row < size - 3; row++) {
                if (tableModel.getValueAt(row, 1).toString().equalsIgnoreCase(tp.getName())) {
                    tp.setTickets(Integer.valueOf(tableModel.getValueAt(row, ticketsColumnIndex).toString()));
                    count++;
                    break; // relation 1:1
                }
            }
        }

        return count;
    }

    public static String getRes(String resKey) {
        return NbBundle.getMessage(transfersTopComponent.class, resKey);
    }

    public static String asStringList(List list, String separator) {

        StringBuilder sb = new StringBuilder();
        for (Object s : list) {
            sb.append(separator).append(s);
        }
        return sb.length() == 0 ? "" : sb.substring(1);

    }

    public static List<String> asListOfStrings(String listStrings, String separator) {

        if (listStrings == null) {
            return Collections.EMPTY_LIST;
        }

        String ss[] = listStrings.split(separator);
        if (ss == null || ss.length < 1) {
            return Collections.EMPTY_LIST;
        }

        ArrayList<String> resList = new ArrayList<String>();
        resList.addAll(Arrays.asList(ss));
        return resList;

    }
}