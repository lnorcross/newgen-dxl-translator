package com.newgen.spring;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLRecursiveTraversal {
    public static void main(String[] args) {
        try {
            // Load and parse the XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse("example.xml"); // Replace with your XML file path

            // Get the root element
            Element root = document.getDocumentElement();

            // Start recursive traversal
            traverseNodes(root, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recursive method to traverse nodes
    static void traverseNodes(Node node, int depth) {
        // Print the current node's name and value (if any)
        printNodeInfo(node, depth);

        // Process child nodes recursively
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            traverseNodes(childNodes.item(i), depth + 1);
        }
    }

    // Helper method to print node information
    private static void printNodeInfo(Node node, int depth) {
        String indent = "  ".repeat(depth); // Indentation for better readability
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            System.out.println(indent + "Element: " + node.getNodeName());
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            String textContent = node.getTextContent().trim();
            if (!textContent.isEmpty()) {
                System.out.println(indent + "Text: " + textContent);
            }
        }
    }
}
