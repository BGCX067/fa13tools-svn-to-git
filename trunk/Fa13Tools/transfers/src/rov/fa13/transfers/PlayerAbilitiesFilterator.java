package rov.fa13.transfers;

import ca.odell.glazedlists.Filterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author MasterOfChaos
 */
public class PlayerAbilitiesFilterator implements Filterator<Object, MyTransferPlayer> {

    private String abilityName;
    //private int abilityValue = 20;//default in fa13
    private Pattern abilityPattern;

    private static int abilityDefValue=20;

    public PlayerAbilitiesFilterator(String abilityName) {
        setAbilityName(abilityName);
    }
    //    private Matcher pattern;

    public String getAbilityName() {
        return abilityName;
    }
    public void setAbilityName(String name) {
        if ((name==null) || (name.isEmpty())) return;
        
        String old = abilityName;
        if ((old == null ) || (!old.equalsIgnoreCase(name))) {
            this.abilityName = name.toLowerCase();
            abilityPattern = Pattern.compile(abilityName + "\\d*");
        }
       
    }

    @Override
    public void getFilterValues(List<Object> list, MyTransferPlayer e) {
        if (abilityPattern == null) return;
        String val = e.getAbilities().toLowerCase();
        Matcher m = abilityPattern.matcher(val);
        String tmp="";
        System.out.print(abilityName+" : "+e.getId()+" -> "+val);
        if (m.find()) {
            tmp = m.group();
            tmp = tmp.substring(abilityName.length());
            //tmp.replace(abilityName, "");
//in same case than team13 data didn't loaded
//transfer ability "UDP" means that U>40 D>40 P>40 so we can just set them to 41
            if (tmp.isEmpty()) {
                tmp = "41";
            }

        } else { //no ability so set it to 20
            tmp = "20";
        }
        System.out.println(" -> "+abilityName+tmp);
        list.add(Integer.valueOf(tmp).intValue());
    }

}
