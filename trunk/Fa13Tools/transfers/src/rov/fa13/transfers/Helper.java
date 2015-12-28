package rov.fa13.transfers;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author rachaev_ov
 */
public class Helper {

    public static void infoMessage(String message) {
        message(message,NotifyDescriptor.INFORMATION_MESSAGE);
    }

    public static void errorMessage(String message) {
        message(message,NotifyDescriptor.ERROR_MESSAGE);
    }

    public static void message(String message, int type) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message,type));
    }
    
    public static boolean askYesNo(String message, String title) {

        Object answer = DialogDisplayer.getDefault().notify(new DialogDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION));
        if (NotifyDescriptor.YES_OPTION.equals(answer)) {
            return true;
        } else {
            return false;
        }
    }   
    
}