package rov.fa13.transfers;

import java.util.Comparator;

/**
 *
 * @author MasterOfChaos
 */
public class MyTransferPlayerComparator implements Comparator<MyTransferPlayer> {

    @Override
    public int compare(MyTransferPlayer p1, MyTransferPlayer p2) {

        return p1.compareTo(p2);
    }

    
}