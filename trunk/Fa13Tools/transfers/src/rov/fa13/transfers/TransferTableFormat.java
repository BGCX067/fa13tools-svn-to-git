package rov.fa13.transfers;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import java.util.Comparator;
import org.openide.util.NbBundle;

/**
 *
 * @author MasterOfChaos
 */
class TransferTableFormat implements AdvancedTableFormat<MyTransferPlayer>,WritableTableFormat<MyTransferPlayer> {

    public TransferTableFormat() {
    }

    @Override
    public int getColumnCount() {
        return 17;
    }
    @Override
    public String getColumnName(int column) {

        String res;
        if (column == 0) {
            res = "id";
        } else if (column == 1) {
            res = getRes("HName");
        } else if (column == 2) {
            res = getRes("HNationality");
        } else if (column == 3) {
            res = getRes("HTeam");
        } else if (column == 4) {
            res = getRes("HPos");
        } else if (column == 5) {
            res = getRes("HAge");
        } else if (column == 6) {
            res = getRes("HBirthTour");
        } else if (column == 7) {
            res = getRes("HTalent");
        } else if (column == 8) {
            res = getRes("HExp");
        } else if (column == 9) {
            res = getRes("HStrength");
        } else if (column == 10) {
            res = getRes("HHealth");
        } else if (column == 11) {
            res = getRes("HFitness");
        } else if (column == 12) {
            res = getRes("HAbilities");
        } else if (column == 13) {
            res = getRes("HSalary");
        } else if (column == 14) {
            res = getRes("HPrice");
        } else if (column == 15) {
            res = getRes("HTickets");
        } else if (column == 16) {
            res = getRes("HSelected");
        } else {
            res = "";
        }
        return res;
    }

    @Override
    public Object getColumnValue(MyTransferPlayer tp, int column) {
        Object res;
        if (column == 0) {
            res = tp.getId();
        } else if (column == 1) {
            res = tp.getName();
        } else if (column == 2) {
            //res = MyTransferPlayer.nationalities.get(tp.getNationalityCode());//TODO: ReMap Nationalities
            res = tp.getNationality();
        } else if (column == 3) {
            res = tp.getPreviousTeam();
        } else if (column == 4) {
            res = tp.getPosition().toString();
        } else if (column == 5) {
            res = tp.getAge();
        } else if (column == 6) {
            res = tp.getBirthtour();
        } else if (column == 7) {
            res = tp.getTalent();
        } else if (column == 8) {
            res = tp.getExperience();
        } else if (column == 9) {
            res = tp.getStrength();
        } else if (column == 10) {
            res = tp.getHealth();
        } else if (column == 11) {
            res = tp.getFitness();
        } else if (column == 12) {
            res = tp.getAbilities();
        } else if (column == 13) {
            res = tp.getSalary();
        } else if (column == 14) {
            res = tp.getPrice();
        } else if (column == 15) {
            res = tp.getTickets();
        } else if (column == 16) {
            res = tp.isSelectedForTicket();
        } else {
            res = "";
        }
        return res;
    }

    @Override
    public Class getColumnClass(int i) {
        Class res;
        switch (i) {
            case 0: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 15: res = Integer.class;
            break;
            case 16: res = Boolean.class;
            break;
            default: return String.class;
        
        }
        return res;
    }

    @Override
    public Comparator getColumnComparator(int i) {
        return GlazedLists.comparableComparator();
    }
    
    public static String getRes(String resKey) {
        return NbBundle.getMessage(TransferTableFormat.class, resKey);
    }

    @Override
    public boolean isEditable(MyTransferPlayer baseObject, int column) {
        return (column == 16);
    }

    @Override
    public MyTransferPlayer setColumnValue(MyTransferPlayer baseObject, Object editedValue, int column) {
        if (column == 16) {
            boolean flag = Boolean.valueOf( editedValue.toString() );
            baseObject.setSelectedForTicket(flag);
        }
        return baseObject;
    }
}
