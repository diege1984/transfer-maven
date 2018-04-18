package cn.diege.chain;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.diege.MavenMeta;
import cn.diege.util.XmlUtils;

public class DeployFinder extends AbstractFinder {

	@Override
	public void findCurrent(List<String> jarList) {
		Iterator<String> it = jarList.iterator();
		Set<MavenMeta> set = new TreeSet<>();
		while (it.hasNext()) {
			String jarPath = it.next();
			MavenMeta meta = find(jarPath);
			if (meta != null) {
				it.remove();

				String deloyCommadFmt = "call mvn deploy:deploy-file -DgroupId=%s -DartifactId=%s -Dversion=%s -Dpackaging=jar -Dfile=%s -Durl=http://nexus.worldunion.com.cn:9005/nexus/content/repositories/releases -DrepositoryId=releases";
				String str = String.format(deloyCommadFmt, meta.groupId, meta.artifactId, meta.version, meta.filePath);
				System.out.println(str);
				set.add(meta);
			}
		}
		XmlUtils.add(set, "d.xml");
	}

	private MavenMeta find(String jarPath) {
		File file = new File(jarPath);
		if (!file.exists()) {
			return null;
		}
		String fileName = file.getName().replaceAll(".jar", "");
		String version = matchVersion(fileName);

		if (version == null) {
			version = "1.0";
		}
		String[] strs = fileName.replaceAll(version, "").split("-");

		String groupId = "";
		String artifactId = "";
		if (strs.length == 1) {
			groupId = artifactId = strs[0];
		}

		if (strs.length == 2) {
			groupId = strs[0];
			artifactId = "".equals(strs[1]) ? strs[0] : strs[1];
		}

		if (strs.length == 3) {
			groupId = strs[0];
			artifactId = strs[1];
		}

		MavenMeta meta = new MavenMeta(groupId, artifactId, version);
		return meta;
	}

	private String matchVersion(String fileName) {
		Pattern p = Pattern.compile("[0-9]+((\\.|-)[0-9a-zA-Z]+)*");
		Matcher m = p.matcher(fileName);
		String text = null;
		while (m.find()) {
			text = m.group();
		}
		return text;
	}

}
