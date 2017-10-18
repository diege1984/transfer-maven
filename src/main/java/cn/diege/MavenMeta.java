package cn.diege;

public class MavenMeta {
	public String groupId;
	public String artifactId;
	public String version;
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
				"<dependency><groupId>%s</groupId><artifactId>%s</artifactId><version>%s</version></dependency>",
				groupId, artifactId, version);
	}
}