package rov.rcp.utils;

/**
 *
 * @author Rachaev_OV
 */


import java.util.Collection;
import java.util.Iterator;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;

import org.openide.util.lookup.InstanceContent;

/**
 * Class used to house anything one might want to store

 * in a central lookup which can affect anything within

 * the application. It can be thought of as a central context

 * where any application data may be stored and watched.

 *

 * A singleton instance is created using @see getDefault().

 * This class is as thread safe as Lookup. Lookup appears to be safe.

 * @author Wade Chandler

 * @version 1.0

 */
public class CentralLookup extends AbstractLookup {

    private InstanceContent content = null;
    private static CentralLookup def = new CentralLookup();

    public CentralLookup(InstanceContent content) {
        super(content);
        this.content = content;
    }

    public CentralLookup() {
        this(new InstanceContent());
    }

    public void add(Object instance) {
        content.add(instance);
    }
    public void addSingleton(Object singletonInstance) {
        if (singletonInstance==null) return;
        Class cl = singletonInstance.getClass();
        Collection lookupAll = def.lookupAll(cl);
        if ((lookupAll!=null) && (!lookupAll.isEmpty())) {
            Object obj;
            for (Iterator it = lookupAll.iterator(); it.hasNext();) {
                obj = it.next();
                content.remove(obj);
            }
        }
        content.add(singletonInstance);
    }

    public void remove(Object instance) {
        content.remove(instance);
    }

    public static CentralLookup getDef() {
        return def; 
    }

    public static Lookup getDefault() {
        return def; 
    }
    
}
