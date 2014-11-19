package model;

import java.util.HashSet;
import java.util.Iterator;
import java.lang.String;

public class mySet<T> extends HashSet<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	mySet<T> symdiff(mySet<T> b) {
		mySet<T> c = new mySet<T>();
		mySet<T> d = new mySet<T>();
		c.addAll(this);
		d.addAll(b);
		c.removeAll(b);
		d.removeAll(this);
		c.addAll(d);
		return c;

	}

	mySet<String> cartesian(mySet<T> b) {
		mySet<String> ret = new mySet<String>();
		for (Iterator<T> as = this.iterator(); as.hasNext();) {
			T astr = as.next();
			for (Iterator<T> bs = b.iterator(); bs.hasNext();) {
				T bstr = bs.next();
				String f = astr.toString().concat(bstr.toString());

				ret.add(f);
			}
		}
		return ret;
	}



	mySet<T> diff(mySet<T> b) {
		this.removeAll(b);
		return this;
	}
	


}
