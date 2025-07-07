package com.newgen.spring;

import com.newgen.spring.models.DocumentDTO;
import com.newgen.spring.models.ItemAttr;
import com.newgen.spring.models.ItemDTO;
import com.newgen.spring.utilities.AppUtilities;
import com.newgen.spring.utilities.UnzipUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.newgen.spring.utilities.AppUtilities.Base64ToFile;
import static com.newgen.spring.utilities.AppUtilities.dateConversion;


@Controller
public class NewGenFileUploadController {

    @Autowired
    private Environment environment;


    public NewGenFileUploadController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/index")
	  public String hello() {
	    return "uploader";
	  }


	  @PostMapping("/upload") 
	  public ResponseEntity<?> handleFileUpload( @RequestParam("file") MultipartFile file ) {
		  String fileName = file.getOriginalFilename();
		  try {
			file.transferTo( new File(environment.getProperty("initial.dxl.output.prefix") + fileName));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} 
		return ResponseEntity.ok("File uploaded successfully.");
		  
	  }

    @GetMapping("/processAllFiles")
    public ResponseEntity<?> processFiles() {

        String directory = "C:\\dxl\\";
        String pattern = ".*\\.dxl"; // Regex pattern for .dxl files

        List<Path> fileList;
        try {
            fileList = getFilesMatchingPattern(directory, pattern);
            System.out.println(getFilesMatchingPattern(directory, pattern));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Loop through the file paths
        for (Path filePath : fileList) {
            System.out.println("found: "+filePath);
            try {
                MultipartFile multipartFile = getMultipartFileFromPath(filePath.toString());
                System.out.println("MultipartFile created: " + multipartFile.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(fileList);


        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/dxlUpload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleDxlUpload( @RequestParam("file") MultipartFile file ) {
        String fileName = file.getOriginalFilename();
        System.out.println(fileName);
        try {
            file.transferTo(new File(environment.getProperty("initial.dxl.upload.directory") + fileName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        File directory = new File(Objects.requireNonNull(environment.getProperty("initial.dxl.upload.directory")));
        List<File> thisDXLList = getXmlFiles(directory);


        List<DocumentDTO> docL = null;
        for (File filed : thisDXLList) {
            System.out.println(filed.getName());
            docL = parseXmlFile(filed);
        }

        assert docL != null;
        return ResponseEntity.ok(docL);


    }
	public List<File> getXmlFiles(File directory) {
        return AppUtilities.getXmlFiles(directory);
	}

	public List<DocumentDTO> parseXmlFile(File xmlFile) {

        String content;
        String contentFileName;
        List<DocumentDTO> docL = List.of();
        String filePrefix = environment.getProperty("initial.dxl.output.prefix");
        DocumentDTO docI;
        int collatCount = 0;
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();
            docL = new ArrayList<>();
            docI = new DocumentDTO();

            NodeList nodeList = document.getElementsByTagName("document");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    docI.setReplicaId(element.getAttribute("replicaid"));
                    docI.setDatabaseId(element.getAttribute("replicaid"));
                    docI.setForm(element.getAttribute("form"));
                }
            }

            nodeList = document.getElementsByTagName("noteinfo");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    docI.setNoteId(element.getAttribute("noteid"));
                    docI.setDocumentUniqueId(element.getAttribute("unid"));
                }
            }

            nodeList = document.getElementsByTagName("updatedby");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    docI.setUserName(element.getFirstChild().getTextContent());
                }
            }

            // The created entity is used for the meta username when possible.  It's the most
//            reliable entity from Domino for this.  However, it may not be available, and should revert
//            to values only stored in the item entities.

            nodeList = document.getElementsByTagName("created");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    docI.setCreatedDate((String) dateConversion(element.getFirstChild().getTextContent()));
                }
            }

            nodeList = document.getElementsByTagName("modified");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    docI.setLastUpdateDate((String) dateConversion(element.getFirstChild().getTextContent()));
                }
            }

            NodeList collatList = document.getElementsByTagName("item");
            List<ItemDTO> itemList = new ArrayList<>();




            for (int i = 0; i < collatList.getLength(); i++) {
                Node node = collatList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    // if the DXL does not contain file data because it is stored from a
                    // different process, this check is not needed.

                    if (element.getAttribute("name").equals("$FILE")) {

                        String filePathStringAttach = filePrefix + "data//attachments//00//000//00000"+docI.getNoteId()+"//";

                        File directory = new File(filePathStringAttach);

                        collatCount=collatCount+1;
                        content = element.getElementsByTagName("filedata").item(0).getTextContent().trim();
                        contentFileName = element.getFirstChild().getFirstChild().getAttributes().getNamedItem("name").getTextContent();




                        if (directory.mkdirs()) {
                              System.out.println("Directory created successfully!");
                              Base64ToFile(content, directory.getPath()+"//"+contentFileName);
                        }
                        else
                        {
                            Base64ToFile(content, directory.getPath()+"//"+contentFileName);
                        }

                        if (contentFileName.contains(".zip")) {
                            String zipFilePath = directory.getPath()+"//"+contentFileName; // Replace with your zip file path
                            String destDirectory = directory.getPath()+"//"+ environment.getProperty("zip.temp.dest"); // Replace with your desired destination

                            UnzipUtility unzipper = new UnzipUtility();
                            try {
                                unzipper.unzip(zipFilePath, destDirectory);
                                System.out.println("Zip file unzipped successfully!");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                        List<ItemAttr> itemAttrList = new ArrayList<>();
                        ItemAttr itemAttr = new ItemAttr();
                        itemAttr.setKey("fileSize");
                        itemAttr.setValue(directory.getPath()+"//"+contentFileName.length());
                        itemAttrList.add(itemAttr);



    }
                    docI.setAttachmentsDetected(collatCount);


                    ItemDTO itemDTO = new ItemDTO();
                    itemDTO.setKey(element.getAttribute("name"));

                    //Record any attributes

                    itemDTO.setType(element.getFirstChild().getNodeName());
                    itemDTO.setValue(itemDTO.getType().equals("datetime") ? (String) dateConversion(element.getTextContent()) : element.getTextContent());
                    if (itemDTO.getType().equals("datetimelist") ) {
                       StringBuilder dateList = new StringBuilder();
                        for (int k = 0; k < element.getElementsByTagName("datetime").getLength(); k++) {
                            dateList.append(dateConversion(element.getElementsByTagName("datetime").item(k).getTextContent())).append(" ");
                            itemDTO.setValue(dateList.toString());
                        }
                    }

                    if (collatList.item(i).hasAttributes()) {

                        List<ItemAttr> itemAttrList = new ArrayList<>();

                        for (int l = 0; l < element.getAttributes().getLength(); l++) {
                            if (!element.getAttributes().item(l).getNodeName().equals("name")) {
                                ItemAttr itemAttr = new ItemAttr();
                                itemAttr.setKey(element.getAttributes().item(l).getNodeName());
                                itemAttr.setValue(element.getAttributes().item(l).getTextContent());
                                itemAttrList.add(itemAttr);

                            }
                        }
                        itemDTO.setAttr(itemAttrList);
                    }

                    if (collatList.item(i).hasChildNodes()) {
                        collatList.item(i).getFirstChild().getTextContent();
                    }
                    itemList.add(itemDTO);
                }
            }
            docI.setItemDTOList(itemList);
            docL.add(docI);

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return docL;

    }


    public String UncompressTemp(String zippedFile) throws IOException {

        String fileZip = zippedFile;
        File destDir = new File(zippedFile+environment.getProperty("zip.temp.dest"));

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            // ...
        }

        zis.closeEntry();
        zis.close();

        return "processComplete";
    }






    public static List<Path> getFilesMatchingPattern(String directory, String pattern) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();
        Path dirPath = Paths.get(directory);

        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().matches(pattern)) {
                    matchingFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return matchingFiles;
    }

    public static MultipartFile getMultipartFileFromPath(String filePath) throws IOException {
        File file = new File(filePath);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return new MockMultipartFile(
                    file.getName(),          // Original file name
                    file.getName(),          // File name in the request
                    "application/octet-stream", // Content type
                    inputStream              // File content
            );
        }
        }

}
