package com.ami.gui2go.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import com.ami.gui2go.models.ActivityInfo;
import com.ami.gui2go.models.ProjectInfo;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;
import android.widget.Toast;

public class ProjectXMLParser {
    private Context context;
    private String ProjectName;
    private DocumentBuilderFactory dbf;
    private DocumentBuilder db;
    private Document doc;
    private String path;

    public ProjectXMLParser(String projectName, Context context) {
        ProjectName = projectName;
        this.context = context;
    }

    public void StartParser() {
        try {
            path = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + ProjectName + "/"
                            + ProjectName + ".xml";
            File f = new File(path);

            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            doc = db.parse(f);
            doc.getDocumentElement().normalize();

        } catch (Exception e) {
            // Log.d("XML parsing", e.getMessage());
        }
    }

    public ActivityInfo GetActivityInfo(String activityName) {
        try {
            ActivityInfo activity = new ActivityInfo();

            NodeList nodeList = doc.getElementsByTagName("Activity");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap attrs = node.getAttributes();
                String name = attrs.getNamedItem("name").getNodeValue();
                if (name.equals(activityName)) {
                    activity.name = name;
                    activity.screenSize = attrs.getNamedItem("screenSize")
                                    .getNodeValue();
                }
            }
            return activity;

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            // Log.d("GetMainActivityInfo", e.getMessage());
            return null;
        }
    }

    public ArrayList<ActivityInfo> GetActivityList() {
        ArrayList<ActivityInfo> activityList = new ArrayList<ActivityInfo>();

        try {
            NodeList nodeList = doc.getElementsByTagName("Activity");
            for (int i = 0; i < nodeList.getLength(); i++) {
                ActivityInfo activity = new ActivityInfo();
                Node node = nodeList.item(i);
                NamedNodeMap attrs = node.getAttributes();
                String name = attrs.getNamedItem("name").getNodeValue();
                String screenSize = attrs.getNamedItem("screenSize")
                                .getNodeValue();
                activity.name = name;
                activity.screenSize = screenSize;
                activityList.add(activity);
            }
            return activityList;

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            // Log.d("GetActivityList", e.getMessage());
        }
        return activityList;
    }

    public ProjectInfo GetProjectInfo() {
        try {
            ProjectInfo project = new ProjectInfo();

            NodeList nodeList = doc.getElementsByTagName("Project");
            Node node = nodeList.item(0);
            NamedNodeMap attrs = node.getAttributes();

            project.name = attrs.getNamedItem("name").getNodeValue();
            project.targetSDK = attrs.getNamedItem("targetSDK").getNodeValue();
            project.mainActivityName = attrs.getNamedItem("mainActivity")
                            .getNodeValue();
            if (attrs.getNamedItem("author") != null) {
                project.author = attrs.getNamedItem("author").getNodeValue();
            }
            return project;
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            // Log.d("GetProjectInfo", "");
            return null;
        }

    }

    public void CreateNewProjectXML(ProjectInfo project, ActivityInfo activity) {
        XmlSerializer serializer = Xml.newSerializer();
        File newxmlfile;
        FileOutputStream fileos;

        String sdState = android.os.Environment.getExternalStorageState();
        String path;
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            Toast.makeText(context, "No SD card available!",
                            Toast.LENGTH_SHORT).show();
            return;
        }
        
        path += "/Gui2Go/Projects/" + project.name + "/";

        // create the project folder
        try {
            newxmlfile = new File(path);
            newxmlfile.mkdirs(); // make the folder path
            newxmlfile = new File(path + "Images/");
            newxmlfile.mkdirs(); // make the images folder
            path += project.name + ".xml";
            newxmlfile = new File(path);
            newxmlfile.createNewFile(); // make the project XML file
            fileos = new FileOutputStream(newxmlfile);

            serializer.setOutput(fileos, "utf-8");
            serializer.startDocument(null, null);
            serializer.setFeature(
                            "http://xmlpull.org/v1/doc/features.html#indent-output",
                            true);

            serializer.startTag(null, "Project");
            serializer.attribute(null, "name", project.name);
            serializer.attribute(null, "targetSDK", project.targetSDK);
            serializer.attribute(null, "mainActivity", project.mainActivityName);
            if (project.author != null) {
                serializer.attribute(null, "author", project.author);
            }
            serializer.startTag(null, "Activity");
            serializer.attribute(null, "name", activity.name);
            serializer.attribute(null, "screenSize", activity.screenSize);
            serializer.endTag(null, "Activity");
            serializer.endTag(null, "Project");
            serializer.endDocument();
            // write xml data into the FileOutputStream
            serializer.flush();
            // finally we close the file stream
            fileos.close();

            Toast.makeText(context,
                            "Created new project called " + project.name + "!",
                            Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Couldn't create project file!",
                            Toast.LENGTH_SHORT).show();
            // Log.d("CreateNewProjectXML", e.getMessage());
        }
    }

    public void AddNewActivityNode(ActivityInfo activity) {
        NodeList nodeList = doc.getElementsByTagName("Project");
        Node node = nodeList.item(0);

        NodeList actList = doc.getElementsByTagName("Activity");
        Node activityNode = actList.item(0);
        Node newnode = activityNode.cloneNode(true);
        NamedNodeMap attrsList = newnode.getAttributes();
        attrsList.getNamedItem("name").setNodeValue(activity.name);
        attrsList.getNamedItem("screenSize").setNodeValue(activity.screenSize);

        node.appendChild(newnode);

        DumpFile();
    }

    public void renameActivity(String oldName, String newName) {
        NodeList nodeList = doc.getElementsByTagName("Project");
        Node node = nodeList.item(0);
        NamedNodeMap attrs = node.getAttributes();
        if (attrs.getNamedItem("mainActivity").getNodeValue().equals(oldName)) {
            attrs.getNamedItem("mainActivity").setNodeValue(newName);
        }

        NodeList actList = doc.getElementsByTagName("Activity");
        int i = 0;
        Node actNode = actList.item(i);
        while (actNode != null
                        && !actNode.getAttributes().item(0).getNodeValue()
                                        .equals(oldName)) {
            i++;
            actNode = actList.item(i);
        }
        actNode.getAttributes().item(0).setNodeValue(newName);

        DumpFile();

        renameActivityFile(oldName, newName);
    }

    private void renameActivityFile(String oldName, String newName) {
        try {
            String directoryPath = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + ProjectName + "/";
            String pathOldFile = oldName + ".xml";
            String pathNewFile = newName + ".xml";
            File f = new File(directoryPath + pathOldFile);
            File newF = new File(directoryPath + pathNewFile);
            f.renameTo(newF);
        } catch (Exception e) {

        }
    }

    public void renameProject(String oldName, String newName) {
        NodeList actList = doc.getElementsByTagName("Project");

        actList.item(0).getAttributes().item(0).setNodeValue(newName);

        DumpFile();

        renameProjectFile(oldName, newName);
    }

    public void renameClonedProject(String oldName, String newName) {
        NodeList actList = doc.getElementsByTagName("Project");

        actList.item(0).getAttributes().item(0).setNodeValue(newName);

        DumpFile();
    }

    private void renameProjectFile(String oldName, String newName) {
        try {
            String oldDirectoryPath = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + oldName + "/";
            String oldFilePath = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + newName + "/" + oldName
                            + ".xml";

            String newDirectoryPath = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + newName + "/";
            String newFilePath = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + newName + "/" + newName
                            + ".xml";

            File f = new File(oldDirectoryPath);
            File newF = new File(newDirectoryPath);
            f.renameTo(newF);

            f = new File(oldFilePath);
            newF = new File(newFilePath);
            f.renameTo(newF);
        } catch (Exception e) {

        }
    }

    public void DeleteActivityNode(String activityName, ProjectInfo project) {
        try {
            NodeList nodeList = doc.getElementsByTagName("Activity");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap attrs = node.getAttributes();
                String name = attrs.getNamedItem("name").getNodeValue();
                if (activityName.equals(name)) { // found the node to delete
                    node.getParentNode().removeChild(node);
                }
            }
            // if this activity is the main activity, we promote the
            // next one
            if (project.mainActivityName.equals(activityName)) {
                NodeList projNodeList = doc.getElementsByTagName("Project");
                // grab the project node and its attrs
                Node projNode = projNodeList.item(0);
                NamedNodeMap projAttrs = projNode.getAttributes();
                // get a hold of the first activity node and its attrs
                Node node = nodeList.item(0);
                NamedNodeMap attrs = node.getAttributes();

                String name = attrs.getNamedItem("name").getNodeValue();
                projAttrs.getNamedItem("mainActivity").setNodeValue(name);
            }

            DumpFile();

            // now to delete the actual activity file
            String deletePath = Environment.getExternalStorageDirectory()
                            + "/Gui2Go/Projects/" + ProjectName + "/"
                            + activityName + ".xml";
            File f = new File(deletePath);
            if (f.exists()) {
                f.delete();
            }

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            // Log.d("GetActivityList", e.getMessage());
        }
    }

    public void SetActivityAsMain(String activityName) {
        NodeList nodeList = doc.getElementsByTagName("Project");
        Node node = nodeList.item(0);
        NamedNodeMap attrs = node.getAttributes();

        if (attrs.getNamedItem("mainActivity").getNodeValue()
                        .equals(activityName)) {
            Toast.makeText(context,
                            "Activity is already set as the main activity!",
                            Toast.LENGTH_SHORT).show();
        } else {
            attrs.getNamedItem("mainActivity").setNodeValue(activityName);

            DumpFile();
        }
    }

    private void DumpFile() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            Result result = new StreamResult(new File(path));
            Source source = new DOMSource(doc);
            transformer.transform(source, result);
        } catch (Exception e) {
            // Log.d("DumpFile", e.getMessage());
        }
    }

    public void resizeActivity(String oldSize, String newSize) {
        NodeList actList = doc.getElementsByTagName("Activity");
        int i = 0;
        Node actNode = actList.item(i);
        while (actNode != null
                        && !actNode.getAttributes().item(1).getNodeValue()
                                        .equals(oldSize)) {
            i++;
            actNode = actList.item(i);
        }
        actNode.getAttributes().item(1).setNodeValue(newSize);

        DumpFile();
    }

    public void cloneActivity(String toClone, String activityName) {
        Node newnode = null;
        NodeList nodeList = doc.getElementsByTagName("Activity");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            NamedNodeMap attrs = node.getAttributes();
            String name = attrs.getNamedItem("name").getNodeValue();
            if (toClone.equals(name)) { // found the node to clone
                newnode = node.cloneNode(true);
                NamedNodeMap attrsList = newnode.getAttributes();
                attrsList.getNamedItem("name").setNodeValue(activityName);
            }
        }

        nodeList = doc.getElementsByTagName("Project");
        Node node = nodeList.item(0);
        node.appendChild(newnode);
        DumpFile();

        String directoryPath = Environment.getExternalStorageDirectory()
                        + "/Gui2Go/Projects/" + ProjectName + "/";
        String pathOldFile = toClone + ".xml";
        String pathNewFile = activityName + ".xml";
        File f = new File(directoryPath + pathOldFile);
        File newF = new File(directoryPath + pathNewFile);
        try {
            FileHelper.copy(f, newF);
        } catch (IOException e) {
            // Log.d("copy exception", e.getMessage());
        }
    }

    public void changeProjectSDK(String newSDKTarget) {
        NodeList actList = doc.getElementsByTagName("Project");

        actList.item(0).getAttributes().item(1).setNodeValue(newSDKTarget);

        DumpFile();
    }
}
