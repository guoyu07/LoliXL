package org.to2mbn.lolixl.plugin.impl;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PluginDescriptionResolver {

	private DocumentBuilder documentBuilder;
	private XPath xpath;

	private XPathExpression xexpGroupId;
	private XPathExpression xexpArtifactId;
	private XPathExpression xexpVersion;
	private XPathExpression xexpDependencies;
	private XPathExpression xexpPlugin;

	public PluginDescriptionResolver() {
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			// Compile XPaths
			xpath = XPathFactory.newInstance().newXPath();
			xexpGroupId = xpath.compile("groupId");
			xexpArtifactId = xpath.compile("artifactId");
			xexpVersion = xpath.compile("version");
			xexpDependencies = xpath.compile("dependencies/dependency");
			xexpPlugin = xpath.compile("plugin");
		} catch (XPathExpressionException | ParserConfigurationException e) {
			throw new IllegalStateException("Couldn't compile XPaths", e);
		}
	}

	public synchronized MavenArtifact resolveArtifact(Node node) throws XPathExpressionException {
		return new MavenArtifact(
				xexpGroupId.evaluate(node),
				xexpArtifactId.evaluate(node),
				xexpVersion.evaluate(node));
	}

	public synchronized PluginDescription resolvePlugin(Node node) throws XPathExpressionException {
		MavenArtifact artifact = resolveArtifact(node);

		Set<MavenArtifact> dependencies = new LinkedHashSet<>();
		NodeList dependenciesNodeList = (NodeList) xexpDependencies.evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < dependenciesNodeList.getLength(); i++) {
			dependencies.add(resolveArtifact(dependenciesNodeList.item(i)));
		}


		return new PluginDescriptionImpl(artifact, dependencies);
	}

	public synchronized PluginDescription resolve(InputSource source) throws XPathExpressionException, SAXException, IOException {
		return resolvePlugin((Node) xexpPlugin.evaluate(documentBuilder.parse(source), XPathConstants.NODE));
	}

}
