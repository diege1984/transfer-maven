package cn.diege.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.diege.chain.DeployFinder;
import cn.diege.chain.Finder;
import cn.diege.chain.NativeFinder;
import cn.diege.chain.NetFinder;
import cn.diege.util.XmlUtils;

public class Main {

	public static void main(String[] args) {
		scan("E:\\soft\\kettle\\kettle7\\data-integration\\lib");
	}

	public static void scan(String... paths) {
		if (paths == null) {
			throw new IllegalArgumentException("文件夹路径不能为空");
		}
		List<String> jarList = new ArrayList<>();
		
		for (String path : paths) {
			jarList.addAll(Arrays.asList(new File(path).list((File dir, String name) -> name.endsWith(".jar"))).stream()
					.map(name -> path + "\\" + name).filter(name -> !XmlUtils.check(name)).collect(Collectors.toList()));
		}
		Finder first = new NativeFinder();
		first.addNext(new NetFinder());
		first.addNext(new DeployFinder());
		first.find(jarList);
	}
	
	

}
