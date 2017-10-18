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

public class DeployCommandGenerator {

	public Map<String, MavenMeta> scanMaven(String... paths) {
		List<File> allList = new ArrayList<>();
		for (String path : paths) {
			File file = new File(path);

			allList.addAll(Arrays.asList(file.listFiles()));
		}
		Map<String, MavenMeta> map = new HashMap<>();
		for (File child : allList) {
			String fileName = child.getName();
			if (!fileName.endsWith(".jar")) {
				continue;
			}
			try {
				JarFile jarFile = new JarFile(child.getAbsolutePath());
				Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
				while (jarEntryEnumeration.hasMoreElements()) {
					JarEntry entry = jarEntryEnumeration.nextElement();
					String jarEntryName = entry.getName();
					if (jarEntryName.endsWith("pom.xml")) {
						InputStream input = jarFile.getInputStream(entry);
						MavenMeta meta = Utils.transfer(input);
						meta.filePath = child.getAbsolutePath();
						map.put(meta.key(), meta);
					}
				}
				jarFile.close();
			} catch (Exception e) {
			}
		}
		return map;
	}

	public void buildCommand() {
		InputStream in = DeployCommandGenerator.class.getClassLoader().getResourceAsStream("list.xml");
		List<MavenMeta> list = Utils.parseToMavenMeta(in);

		Map<String, MavenMeta> map = scanMaven("E:\\third_jars\\111");
		for (MavenMeta meta : list) {

			MavenMeta fileMeta = map.get(meta.key());
			String deloyCommadFmt = "call mvn deploy:deploy-file -DgroupId=%s -DartifactId=%s -Dversion=%s -Dpackaging=jar -Dfile=%s -Durl=http://nexus.worldunion.com.cn:9005/nexus/content/repositories/releases -DrepositoryId=releases";
		
			String str = String.format(deloyCommadFmt, fileMeta.groupId,fileMeta.artifactId,fileMeta.version,fileMeta.filePath);
			System.out.println(str);
		}
		
		System.out.println("pause");
	}
	
	public static void main(String[] args) {
		DeployCommandGenerator generator = new DeployCommandGenerator();
		generator.buildCommand();
	}

}
