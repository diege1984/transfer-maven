package cn.diege;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "dependencies")
public class MetaList {

	@ElementList(inline = true)
	public List<MavenMeta> list = new ArrayList<>();
	
	public void add(Collection<MavenMeta> collection){
		list.addAll(collection);
	}
	
}
