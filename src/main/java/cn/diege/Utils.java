package cn.diege;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Utils {
	public static MavenMeta transfer(InputStream in) throws ParserConfigurationException, SAXException, IOException {
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
		
		return meta;
	}
	
	public static List<MavenMeta> parseToMavenMeta(InputStream in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dt = db.parse(in);
			Element element = dt.getDocumentElement();
			NodeList rootChilds = element.getChildNodes();
			List<MavenMeta> list = new ArrayList<>();
			for (int i = 0; i < rootChilds.getLength(); i++) {
				Node node = rootChilds.item(i);

				NodeList metaChilds = node.getChildNodes();
				if(!"dependency".equals(node.getNodeName())) {
					continue;
				}
				MavenMeta meta = new MavenMeta();
				for (int m = 0; m < metaChilds.getLength(); m++) {
					Node metaNode = metaChilds.item(m);
					
					if ("groupId".equals(metaNode.getNodeName())) {
						meta.groupId = metaNode.getTextContent().trim();
					}
					if ("artifactId".equals(metaNode.getNodeName())) {
						meta.artifactId = metaNode.getTextContent().trim();
					}
					if ("version".equals(metaNode.getNodeName())) {
						meta.version = metaNode.getTextContent().trim();
					}
				}
				list.add(meta);
			}
			return list;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
