package satAssist;

import java.util.Comparator;

public class CompContactEventBegin implements Comparator<ContactEvent> {
    @Override
    public int compare(ContactEvent o1, ContactEvent o2) {
    	if (o1.getBegin().compareTo(o2.getBegin()) == 0) {
    		return o1.getSat().compareTo(o2.getSat());
    	}
        return o1.getBegin().compareTo(o2.getBegin());
    }
}
