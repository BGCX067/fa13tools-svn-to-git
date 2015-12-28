package rov.fa13.transfers;

import com.fa13.build.model.Player;
import com.fa13.build.model.TransferPlayer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author MasterOfChaos
 */
public class MyTransferPlayer extends com.fa13.build.model.TransferPlayer {

    private int tickets;
    static private int ageDiff = 2;
    static private int talDiff = 5;
    static private int strDiff = 5;
    static private int expDiff = 20;
    protected boolean selectedForTicket = false;
    //add new counties
//    static {
//        nationalities.put("Сент-Лусия", "slu");
//    }
    /**
     * Get the value of selectedForTicket
     *
     * @return the value of selectedForTicket
     */
    public boolean isSelectedForTicket() {
        return selectedForTicket;
    }

    /**
     * Set the value of selectedForTicket
     *
     * @param selectedForTicket new value of selectedForTicket
     */
    public void setSelectedForTicket(boolean selectedForTicket) {
        this.selectedForTicket = selectedForTicket;
    }

    public static int getAgeDiff() {
        return ageDiff;
    }

    public static void setAgeDiff(int ageDiff) {
        MyTransferPlayer.ageDiff = ageDiff;
    }

    public static int getExpDiff() {
        return expDiff;
    }

    public static void setExpDiff(int expDiff) {
        MyTransferPlayer.expDiff = expDiff;
    }

    public static int getStrDiff() {
        return strDiff;
    }

    public static void setStrDiff(int strDiff) {
        MyTransferPlayer.strDiff = strDiff;
    }

    public static int getTalDiff() {
        return talDiff;
    }

    public static void setTalDiff(int talDiff) {
        MyTransferPlayer.talDiff = talDiff;
    }

    public MyTransferPlayer() {
        super();
    }

    public MyTransferPlayer(TransferPlayer tp) {
        copyAbilitiesFrom(tp);
        setId(tp.getId());
        setName(tp.getName());
        setNationalityCode(tp.getNationalityCode());
        setPreviousTeam(tp.getPreviousTeam());
        setPosition(tp.getPosition());
        setAge(tp.getAge());
        setTalent(tp.getTalent());
        setSalary(tp.getSalary());
        setStrength(tp.getStrength());
        setHealth(tp.getHealth());
        setAbilities(tp.getAbilities());
        setPrice(tp.getPrice());
        setTransferID(tp.getTransferID());
        
        setBirthtour(tp.getBirthtour());

    }

    public int getTickets() {
        return tickets;
    }

    public void setTickets(int tickets) {
        this.tickets = tickets;
    }
//remap nationalities
//so:   key = code
//    value = value
    public static final Map<String, String> nationalities2;

    static {
        Map<String, String> tmp = new TreeMap<String, String>();
        for (Map.Entry<String, String> v : nationalities.entrySet()) {
            tmp.put(v.getValue(), v.getKey());
        }
        //hack
        tmp.put("stl","Сент-Лусия");//Сент-Лусия
        tmp.put("und","Underfined-Check!");
        
        nationalities2 = tmp;
    }

    ;

    /*
    public static String getNationality(String nationalityCode) {
    return nationalities2.get(nationalityCode);
    }
     */
    public String getNationality() {
        String s = getNationalityCode();
        if (s == null) s = "und";
        return nationalities2.get(s);
    }

    public boolean isPlayerSimilarForTransfer(MyTransferPlayer tp) {
        boolean res = (Math.abs(this.getAge() - tp.getAge()) <= ageDiff)
                && (Math.abs(this.getTalent() - tp.getTalent()) <= talDiff)
                && (Math.abs(this.getStrength() - tp.getStrength()) <= strDiff)
                && (Math.abs(this.getExperience() - tp.getExperience()) <= expDiff);
        res = res && tp.getPosition().toString().substring(1).equalsIgnoreCase(this.getPosition().toString().substring(1));
        return res;
    }

    public double getPriceByPlayer(MyTransferPlayer tp) {
        double k = ((double) tp.getHealth() / (double) this.getHealth()) * ((double) tp.getSumAbilitiesCalc() / (double) this.getSumAbilities());
        k = (double) tp.getPrice() * k * (tp.getPosition().equals(this.getPosition()) ? 1.0 : 0.9);
        return k;
    }

    public int getSumAbilities() {
        return this.getCross()
                + this.getDribbling()
                + this.getHandling()
                + this.getHeading()
                + this.getPassing()
                + this.getReflexes()
                + this.getShooting()
                + this.getSpeed()
                + this.getStamina()
                + this.getTackling();
    }
    private int sumAbilitiesCalc = -1;

    public int getSumAbilitiesCalc() {
        if (sumAbilitiesCalc < 0) {
            return getSumAbilities();
        } else {
            return sumAbilitiesCalc;
        }
    }

    public void setSumAbilitiesCalc(int sumAbilities) {
        this.sumAbilitiesCalc = sumAbilities;
    }
    
    public void resetSumAbilitiesCalc() {
        setSumAbilitiesCalc(-1);
    }

    public static double getPredictionPrice(MyTransferPlayer player, ArrayList<MyTransferPlayer> calcList) {
        if ((player == null) || (calcList == null) || (calcList.size() < 1)) {
            return 0;
        }
        double sum1 = 0, sum2 = 0, pos_weight = 1;
        for (MyTransferPlayer tp : calcList) {
            sum1 = sum1 + tp.getPriceByPlayer(player);
            pos_weight = (tp.getPosition().equals(player.getPosition()) ? 1.0 : 0.9);
            sum2 = sum2 + pos_weight;
        }
        if (Math.abs(sum2) < 0.0001) {
            System.out.println(player.getName() + " : " + player.getAbilities());
        }
        return sum1 / sum2;
    }

    @Override
    public final void setAbilities(String abilities) {
        super.setAbilities(abilities);
    }

    public final void copyAbilitiesFrom(Player player) {
        this.setCross(player.getCross());
        this.setDribbling(player.getDribbling());
        this.setHandling(player.getHandling());
        this.setHeading(player.getHeading());
        this.setPassing(player.getPassing());
        this.setReflexes(player.getReflexes());
        this.setShooting(player.getShooting());
        this.setSpeed(player.getSpeed());
        this.setTackling(player.getTackling());
        this.setStamina(player.getStamina());
        checkAbilitiesForLimits();
    }

    public void checkAbilitiesForLimits() {
        int minAbiltyValue = 20;
        if (getCross() < minAbiltyValue) {
            setCross(minAbiltyValue);
        }
        if (getDribbling() < minAbiltyValue) {
            setDribbling(minAbiltyValue);
        }
        if (getHandling() < minAbiltyValue) {
            setHandling(minAbiltyValue);
        }
        if (getHeading() < minAbiltyValue) {
            setHeading(minAbiltyValue);
        }
        if (getPassing() < minAbiltyValue) {
            setPassing(minAbiltyValue);
        }
        if (getReflexes() < minAbiltyValue) {
            setReflexes(minAbiltyValue);
        }
        if (getShooting() < minAbiltyValue) {
            setShooting(minAbiltyValue);
        }
        if (getSpeed() < minAbiltyValue) {
            setSpeed(minAbiltyValue);
        }
        if (getTackling() < minAbiltyValue) {
            setTackling(minAbiltyValue);
        }
        if (getStamina() < minAbiltyValue) {
            setStamina(minAbiltyValue);
        }

        int maxAbiltyValue = 200;
        if (getCross() > maxAbiltyValue) {
            setCross(maxAbiltyValue);
        }
        if (getDribbling() > maxAbiltyValue) {
            setDribbling(maxAbiltyValue);
        }
        if (getHandling() > maxAbiltyValue) {
            setHandling(maxAbiltyValue);
        }
        if (getHeading() > maxAbiltyValue) {
            setHeading(maxAbiltyValue);
        }
        if (getPassing() > maxAbiltyValue) {
            setPassing(maxAbiltyValue);
        }
        if (getReflexes() > maxAbiltyValue) {
            setReflexes(maxAbiltyValue);
        }
        if (getShooting() > maxAbiltyValue) {
            setShooting(maxAbiltyValue);
        }
        if (getSpeed() > maxAbiltyValue) {
            setSpeed(maxAbiltyValue);
        }
        if (getTackling() > maxAbiltyValue) {
            setTackling(maxAbiltyValue);
        }
        if (getStamina() > maxAbiltyValue) {
            setStamina(maxAbiltyValue);
        }
    }
}
