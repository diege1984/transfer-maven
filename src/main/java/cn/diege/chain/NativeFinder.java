package cn.diege.chain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cn.diege.MavenMeta;
import cn.diege.Utils;
import cn.diege.util.XmlUtils;

public class NativeFinder extends AbstractFinder {

	@Override
	public void findCurrent(List<String> jarList) {
		Set<MavenMeta> metaSet = new TreeSet<>();
		Iterator<String> it = jarList.iterator();
		while (it.hasNext()) {
			String jarPath = it.next();
			try (JarFile jarFile = new JarFile(jarPath)) {
				Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
				while (jarEntryEnumeration.hasMoreElements()) {
					JarEntry entry = jarEntryEnumeration.nextElement();
					String jarEntryName = entry.getName();
					if (jarEntryName.endsWith("pom.xml")) {
						InputStream input = jarFile.getInputStream(entry);
						MavenMeta meta = Utils.transfer(input);
						meta.filePath = jarPath;
						metaSet.add(meta);
						it.remove();
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		metaSet.forEach(System.out::print);
		XmlUtils.add(metaSet);
	}

}
