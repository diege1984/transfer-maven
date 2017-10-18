package cn.diege;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Scan {

	Map<String, List<MavenMeta>> metaMaps = new HashMap<>();
	List<String> noHope = new ArrayList<>();

	public void scan(String... paths) {

		List<String> noPoms = new ArrayList<>();
		List<File> allList = new ArrayList<>();
		for (String path : paths) {
			File file = new File(path);

			allList.addAll(Arrays.asList(file.listFiles()));
		}

		for (File child : allList) {
			String fileName = child.getName();
			if (!fileName.endsWith(".jar")) {
				continue;
			}
			try {
				JarFile jarFile = new JarFile(child.getAbsolutePath());
				Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
				boolean hasPom = false;
				while (jarEntryEnumeration.hasMoreElements()) {
					JarEntry entry = jarEntryEnumeration.nextElement();
					String jarEntryName = entry.getName();
					if (jarEntryName.endsWith("pom.xml")) {
						InputStream input = jarFile.getInputStream(entry);
						transfer(input);
						hasPom = true;
					}
				}
				if (!hasPom) {
					noPoms.add(fileName);
				}
				jarFile.close();
			} catch (Exception e) {
			}
		}
		transferNoPoms(noPoms);
		metaMaps.forEach((key, value) -> {
			if (value.size() > 1) {
				System.out.println("may be conflict");
			}
			value.forEach(meta -> System.out.println(meta));
		});
		System.out.println("----  Can't Analysis list ----");
		noHope.forEach(name -> System.out.println(name));
	}

	public void transferNoPoms(List<String> noPoms) {
		for (String fileName : noPoms) {
			MavenMeta meta = transferMavenMeta(fileName);
			if (meta == null) {
				noHope.add(fileName);
				continue;
			}
			addMavenMeta(meta);
			// try {
			// String pathFmt = "http://mvnrepository.com/artifact/%s/%s";
			// URL url = new URL(String.format(pathFmt, meta.groupId, meta.artifactId));
			// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// conn.setConnectTimeout(5 * 1000);
			// conn.setRequestMethod("GET");
			// conn.connect();
			// int state = conn.getResponseCode();
			// if (state == 200) {
			// addMavenMeta(meta);
			// }
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}
	}

	private MavenMeta transferMavenMeta(String fileName) {
		fileName = fileName.substring(0, fileName.length() - 4);
		String groupId = "";
		String artifactId = "";
		String version = "";
		int index = fileName.lastIndexOf("-");
		if (index < 1) {
			return null;
		}
		version = fileName.substring(index + 1);
		groupId = artifactId = fileName.substring(0, index);
		MavenMeta meta = new MavenMeta(groupId, artifactId, version);
		return meta;
	}

	public void transfer(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dt = db.parse(in);
		Element element = dt.getDocumentElement();
		NodeList rootChilds = element.getChildNodes();
		String parentGroupId = null;
		String parentVersion = null;
		String groupId = null;
		String artifactId = null;
		String version = null;
		for (int i = 0; i < rootChilds.getLength(); i++) {
			Node rootChild = rootChilds.item(i);
			if ("parent".equals(rootChild.getNodeName())) {
				NodeList parentChilds = rootChild.getChildNodes();
				for (int p = 0; p < parentChilds.getLength(); p++) {
					Node parentChild = parentChilds.item(p);
					if ("groupId".equals(parentChild.getNodeName())) {
						parentGroupId = parentChild.getTextContent();
					}
					if ("version".equals(parentChild.getNodeName())) {
						parentVersion = parentChild.getTextContent();
					}
				}
			}

			if ("groupId".equals(rootChild.getNodeName())) {
				groupId = rootChild.getTextContent();
			}
			if ("artifactId".equals(rootChild.getNodeName())) {
				artifactId = rootChild.getTextContent();
			}
			if ("version".equals(rootChild.getNodeName())) {
				version = rootChild.getTextContent();
			}
		}

		if (groupId == null) {
			groupId = parentGroupId;
		}
		if (version == null) {
			version = parentVersion;
		}
		MavenMeta meta = new MavenMeta(groupId, artifactId, version);

		addMavenMeta(meta);

	}

	private void addMavenMeta(MavenMeta meta) {
		String key = meta.groupId + ":" + meta.artifactId;
		List<MavenMeta> metaList = metaMaps.get(key);
		if (metaList == null) {
			metaList = new ArrayList<>();
			metaMaps.put(key, metaList);
		}
		metaList.add(meta);
	}

	public static class MavenMeta {
		public String groupId;
		public String artifactId;
		public String version;

		public MavenMeta(String groupId, String artifactId, String version) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		public String toString() {
			return String.format("<dependency><groupId>%s</groupId><artifactId>%s</artifactId><version>%s</version></dependency>", groupId,
					artifactId, version);
		}
	}

}
