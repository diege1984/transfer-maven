package cn.diege.chain;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFinder implements Finder {
	
	protected Logger logger = LoggerFactory.getLogger(Finder.class);

	Finder next;

	@Override
	public void find(List<String> jarList) {
		findCurrent(jarList);
		if(next != null){
			next.find(jarList);
		}
	}
	
	public abstract void findCurrent(List<String> jarList);

	@Override
	public void addNext(Finder finder) {
		if (next == null) {
			next = finder;
		} else {
			next.addNext(finder);
		}
	}

}
