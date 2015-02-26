package de.fu.st.main.xmlparser;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Soeren Titze on 22.02.2015.
 */
public class DaterPruner {

  private static HashMap<String, FileWriter> _fileWriters = new HashMap<>();

  private static String ptidString = "PostTypeId=";
  private static String phtidString = "PostHistoryTypeId=";
  private static String cDateString = "CreationDate";

  public static void pruneUserXML(String fileName, boolean prune) throws IOException {
    int skipLines = 3;
    int lineCount = 1;

    File file = new File(fileName);
    BufferedReader br = new BufferedReader(new FileReader(file));
    OutputStreamWriter fw =  new OutputStreamWriter(new FileOutputStream(fileName+"pruned"+"utf8"), "UTF8");

    fw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<users>\n");
    fw.flush();

    String line;

    while ((line = br.readLine()) != null){

      lineCount++;

      if (lineCount <= skipLines) continue;

      String cLine = line;
      if (prune) {
        cLine.replaceAll("CreationDate=\"[^\"]*\"", "")
                .replaceAll("LastAccessDate=\"[^\"]*\"", "")
                .replaceAll("WebsiteUrl=\"[^\"]*\"", "")
                .replaceAll("LastActivityDate=\"[^\"]*\"", "")
                .replaceAll("Location=\"[^\"]*\"", "")
                .replaceAll("AboutMe=\"[^\"]*\"", "")
                .replaceAll("Views=\"[^\"]*\"", "")
                .replaceAll("ProfileImageUrl=\"[^\"]*\"", "")
                .replaceAll("Age=\"[^\"]*\"", "")
                .replaceAll("AccountId=\"[^\"]*\"", "");
      }

      fw.write(cLine+"\n");
      fw.flush();
    }

    fw.write("</users>");
    fw.flush();
    fw.close();

    br.close();
  }

  private static void prunePostsXML(String fileName) throws IOException, ParseException {
    int skipLines = 3;
    int lineCount = 1;

    File file = new File(fileName);
    BufferedReader br = new BufferedReader(new FileReader(file));

    String line;

    while ((line = br.readLine()) != null){

      lineCount++;
      int type = line.length() < 20 ? 0 : Character.getNumericValue(line.charAt(line.indexOf(ptidString) + 1 + ptidString.length()));

      if (lineCount <= skipLines || type != 1) continue;

      String cLine = line.replaceAll("Body=\"[^\"]*\"", "")
              .replaceAll("Title=\"[^\"]*\"", "")
              .replaceAll("Tags=\"[^\"]*\"","")
              .replaceAll("LastActivityDate=\"[^\"]*\"","")
              .replaceAll("LastEditorUserId=\"[^\"]*\"","")
              .replaceAll("LastEditorDisplayName=\"[^\"]*\"","")
              .replaceAll("LastEditDate=\"[^\"]*\"","")
              .replaceAll("CommunityOwnedDate=\"[^\"]*\"","");

      int start = cLine.indexOf(cDateString) + cDateString.length()+2;
      int end = start + 4;
      String dString = cLine.substring(start, end);

      FileWriter fw = getFileWriter(fileName, dString);

      fw.write(cLine+"\n");
      fw.flush();
    }

    _fileWriters.values().forEach(fw -> {
      try {
        fw.write("</posts>");
        fw.flush();

        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    br.close();
  }

  public static void prunePostHistoryXML(String fileName) throws IOException {
    int skipLines = 3;
    int lineCount = 1;

    File file = new File(fileName);
    BufferedReader br = new BufferedReader(new FileReader(file));

    String line;

    List<Integer> types = new ArrayList<>();
    types.add(10);
    types.add(11);
    types.add(12);
    types.add(13);

    while ((line = br.readLine()) != null){

      lineCount++;
      int startType = line.indexOf(phtidString) + 1 + phtidString.length();
      int endType = startType+2;
      int type = 0;

      try{
        type = Integer.valueOf(line.substring(startType,endType));
      } catch (Exception e){}

      if (lineCount <= skipLines || !types.contains(type)) continue;

      String cLine = line.replaceAll("RevisionGUID=\"[^\"]*\"", "")
              .replaceAll("Comment=\"[^\"]*\"", "");

      int start = cLine.indexOf(cDateString) + cDateString.length()+2;
      int end = start + 4;
      String dString = cLine.substring(start, end);

      FileWriter fw = getFileWriter(fileName, dString);

      fw.write(cLine+"\n");
      fw.flush();
    }

    _fileWriters.values().forEach(fw -> {
      try {
        fw.write("</posthistory>");
        fw.flush();

        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    br.close();
  }

  private static FileWriter getFileWriter(String fileName, String dString) throws IOException {
    String efName = fileName+dString;

    if (!_fileWriters.containsKey(efName)){
      File fileC = new File(efName);
      FileWriter fw = new FileWriter(fileC);

      fw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<posthistory>\n");
      fw.flush();
      _fileWriters.put(efName, fw);
    }

    return _fileWriters.get(efName);
  }

}