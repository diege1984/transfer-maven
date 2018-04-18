package cn.diege.chain;

import java.util.List;

public interface Finder {
	
	/**
	 * 查找
	 * @param jarList
	 * @return
	 */
	public void find(List<String> jarList);
	
	/**
	 * 新增下一个元素
	 * @param finder
	 */
	public void addNext(Finder finder);
	
}
