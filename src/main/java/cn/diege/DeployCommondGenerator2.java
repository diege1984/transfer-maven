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

public class DeployCommondGenerator2 {
	
	public Map<String, MavenMeta> scanNoMaven(String... paths) {
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
			boolean isMaven = false;
			try {
				JarFile jarFile = new JarFile(child.getAbsolutePath());
				Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
				while (jarEntryEnumeration.hasMoreElements()) {
					JarEntry entry = jarEntryEnumeration.nextElement();
					String jarEntryName = entry.getName();
					if (jarEntryName.endsWith("pom.xml")) {
						isMaven = true;
					}
				}
				jarFile.close();
			} catch (Exception e) {
			}
			if(!isMaven) {
				MavenMeta meta = transferMavenMeta(fileName);
				if(meta == null) {
					continue;
				}
				meta.filePath = child.getAbsolutePath();
				map.put(meta.key(), meta);
			}
		}
		return map;
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
	
	public void buildCommand() {
		InputStream in = DeployCommandGenerator.class.getClassLoader().getResourceAsStream("list2.xml");
		List<MavenMeta> list = Utils.parseToMavenMeta(in);
		
		Map<String, MavenMeta> map = scanNoMaven("E:\\third_jars\\111");
		for (MavenMeta meta : list) {

			MavenMeta fileMeta = map.get(meta.key());
			String deloyCommadFmt = "call mvn deploy:deploy-file -DgroupId=%s -DartifactId=%s -Dversion=%s -Dpackaging=jar -Dfile=%s -Durl=http://nexus.worldunion.com.cn:9005/nexus/content/repositories/releases -DrepositoryId=releases";
			if(fileMeta == null) {
				System.out.println(meta.key());
				continue;
			}
			String str = String.format(deloyCommadFmt, fileMeta.groupId,fileMeta.artifactId,fileMeta.version,fileMeta.filePath);
			System.out.println(str);
		}
		
		System.out.println("pause");
	}
	
	public static void main(String[] args) {
		DeployCommondGenerator2 generator = new DeployCommondGenerator2();
		generator.buildCommand();
	}
}
