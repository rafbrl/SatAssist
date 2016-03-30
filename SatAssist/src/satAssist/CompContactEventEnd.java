package satAssist;

import java.util.Comparator;

public class CompContactEventEnd implements Comparator<ContactEvent> {
    @Override
    public int compare(ContactEvent o1, ContactEvent o2) {
    	if (o1.getEnd().compareTo(o2.getEnd()) == 0) {
    			return o1.getSat().compareTo(o2.getSat());
    	}
    	return o1.getEnd().compareTo(o2.getEnd());
    }
}
