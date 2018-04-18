package cn.diege;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "dependency")
public class MavenMeta implements Comparable<MavenMeta> {
	@Element
	public String groupId;
	@Element
	public String artifactId;
	@Element
	public String version;
	@Element
	public String filePath;

	public MavenMeta() {

	}

	public MavenMeta(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public String key() {
		return this.groupId + ":" + this.artifactId + ":" + this.version;
	}

	public String toString() {
		return String.format(
				"<dependency>\n  <groupId>%s</groupId>\n  <artifactId>%s</artifactId>\n  <version>%s</version>\n</dependency>",
				groupId, artifactId, version);
	}

	@Override
	public int compareTo(MavenMeta o) {
		return this.toString().compareTo(o.toString());
	}
}