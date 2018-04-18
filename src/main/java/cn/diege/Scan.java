package cn.diege;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Scan {

	Map<String, List<MavenMeta>> metaMaps = new HashMap<>();
	Map<String, List<MavenMeta>> metaMaps2 = new HashMap<>();
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
						MavenMeta meta = Utils.transfer(input);
						meta.filePath = child.getAbsolutePath();
						addMavenMeta(meta);
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
		System.out.println("----  maybe correct ----");
		metaMaps2.forEach((key, value) -> {
			if (value.size() > 1) {
				System.out.println("may be conflict");
			}
			value.forEach(meta -> System.out.println(meta));
		});
		System.out.println("----  maybe correct ----");
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
			addMavenMeta2(meta);
			// try {
			// String pathFmt = "http://mvnrepository.com/artifact/%s/%s";
			// URL url = new URL(String.format(pathFmt, meta.groupId,
			// meta.artifactId));
			// HttpURLConnection conn = (HttpURLConnection)
			// url.openConnection();
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

	private void addMavenMeta(MavenMeta meta) {
		String key = meta.groupId + ":" + meta.artifactId;
		List<MavenMeta> metaList = metaMaps.get(key);
		if (metaList == null) {
			metaList = new ArrayList<>();
			metaMaps.put(key, metaList);
		}
		metaList.add(meta);
	}

	private void addMavenMeta2(MavenMeta meta) {
		String key = meta.groupId + ":" + meta.artifactId;
		List<MavenMeta> metaList = metaMaps2.get(key);
		if (metaList == null) {
			metaList = new ArrayList<>();
			metaMaps2.put(key, metaList);
		}
		metaList.add(meta);
	}

}
