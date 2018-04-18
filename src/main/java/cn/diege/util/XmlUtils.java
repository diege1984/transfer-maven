package cn.diege.util;

import java.io.File;
import java.util.Collection;

import org.simpleframework.xml.core.Persister;

import cn.diege.MavenMeta;
import cn.diege.MetaList;

public class XmlUtils {
	static final String xmlPath = "p.xml";

	public static boolean check(String jarPath) {
		File f = new File(xmlPath);
		try {
			// 创建一个传输器，执行其read方法，可以直接获取到
			MetaList pList = new Persister().read(MetaList.class, f);
			for (MavenMeta meta : pList.list) {
				if (jarPath.equals(meta.filePath)) {
					return true;
				}
			}
		} catch (Exception e) {

		}
		return false;
	}

	public static void add(MavenMeta meta) {
		File f = new File(xmlPath);
		try {
			// 创建一个传输器，执行其read方法，可以直接获取到
			Persister persister = new Persister();
			MetaList pList = persister.read(MetaList.class, f);
			pList.list.add(meta);
			persister.write(pList, new File(xmlPath));
		} catch (Exception e) {

		}
	}

	public static void add(Collection<MavenMeta> meta) {
		add(meta, xmlPath);
	}

	public static void add(Collection<MavenMeta> meta, String path) {
		File f = new File(path);
		Persister persister = new Persister();
		MetaList pList = null;
		try {
			// 创建一个传输器，执行其read方法，可以直接获取到
			if (f.exists()) {
				pList = persister.read(MetaList.class, f);
			}
		} catch (Exception e) {
			pList = new MetaList();
		}

		try {
			pList.list.addAll(meta);
			persister.write(pList, new File(xmlPath));
		} catch (Exception e) {

		}
	}

}
