package de.fu.st.main.xmlparser;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Soeren Titze on 22.02.2015.
 */
public class Main {

  private static HashMap<Integer, Question> _questions;
  private static HashMap<Integer, User> _users;

  private static int _lastFakeId = -2;

  public static void main(String[] args){

    _users = new HashMap<>();
    _questions = new HashMap<>();

    try {
      readUser();
      readQuestions(2010);
      writeData(2010);
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private static void writeData(int year) throws IOException {
    OutputStreamWriter userWriter =  new OutputStreamWriter(new FileOutputStream(Constants.OutputUser+year+".csv"), "UTF8");

    userWriter.write(User.getHeader());
    userWriter.flush();

    _users.values().forEach(user -> {
      if (user.wasUsed) {
        try {
          userWriter.write(user.toRow());
          userWriter.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    userWriter.close();

    HashMap<String, OutputStreamWriter> questionWriters = new HashMap<>();

    for(Question question : _questions.values()){
      String row = question.toRow();
      if (!row.equals("") || question.closedDate == null){
        continue;
      }

      OutputStreamWriter questionWriter = getWriter(question, questionWriters);
      try {
          questionWriter.write(row);
          questionWriter.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    for(OutputStreamWriter writer : questionWriters.values()) {
      writer.close();
    }
  }

  private static OutputStreamWriter getWriter(Question question, HashMap<String, OutputStreamWriter> questionWriters) throws IOException {
    SimpleDateFormat df = new SimpleDateFormat("yyyy_MM");
    String fileName = Constants.OutputQuestions+df.format(question.closedDate)+".csv";
    if (!questionWriters.containsKey(fileName)) {
      OutputStreamWriter questionWriter =new OutputStreamWriter(new FileOutputStream(fileName), "UTF8");
      questionWriters.put(fileName, questionWriter);

      questionWriter.write(Question.getHeader());
      questionWriter.flush();

      return questionWriter;
    } else {
      return questionWriters.get(fileName);
    }
  }

  private static void readUser() throws ParserConfigurationException, IOException, SAXException {

    File userDoc = new File(Constants.userFileName+Constants.pruned);
    BufferedReader br = new BufferedReader(new FileReader(userDoc));

    String line;

    while((line = br.readLine()) != null){
      if (!line.trim().startsWith("<row")) continue;

      String id = readAttribute(line, Constants.id, Constants.AttributeStart, Constants.AttributeEnd);
      String displayName = readAttribute(line, Constants.displayName, Constants.AttributeStart, Constants.AttributeEnd);
      String reputation = readAttribute(line, Constants.reputation, Constants.AttributeStart, Constants.AttributeEnd);

      User user = new User(Integer.valueOf(id),
              Integer.valueOf(reputation),
              displayName, true);

      _users.put(Integer.valueOf(id), user);
    }

    }

  private static void readQuestions(int year) throws ParserConfigurationException, IOException, SAXException, ParseException {
    readPostHistory(year);

    File posts = new File(Constants.postsFileName+year);
    BufferedReader br = new BufferedReader(new FileReader(posts));

    String line;
      int last10k = 0;
      int count = 0;
      float finished = 0;
      double lastperc = 0;
      while((line = br.readLine()) != null) {
        if (!line.trim().startsWith("<row")) continue;

        String idString = readAttribute(line, Constants.id, Constants.AttributeStart, Constants.AttributeEnd);
        int id = Integer.valueOf(idString);

        count++;
        if (count >= 10000){
          last10k++;
          count = 0;
          System.out.println(last10k*10000);
        }

        if (!_questions.containsKey(id)) continue;

        String scoreString = readAttribute(line, Constants.Score, Constants.AttributeStart, Constants.AttributeEnd);
        String viewString = readAttribute(line, Constants.ViewCount, Constants.AttributeStart, Constants.AttributeEnd);
        String answerString = readAttribute(line, Constants.AnswerCount, Constants.AttributeStart, Constants.AttributeEnd);
        String commentString = readAttribute(line, Constants.CommentCount, Constants.AttributeStart, Constants.AttributeEnd);
        String ownerString = readAttribute(line, Constants.OwnerUserId, Constants.AttributeStart, Constants.AttributeEnd);
        String ownerDisplayName = readAttribute(line, Constants.OwnerDisplayName, Constants.AttributeStart, Constants.AttributeEnd);

        Question question = _questions.get(id);

        User owner = null;
        if (ownerDisplayName.equals("")){
           owner = _users.get(Integer.valueOf(ownerString));
        } else {

          for(User user : _users.values()){
            if (user.displayName.equals(ownerDisplayName)){
              owner = user;
              break;
            }
          }
        }

        if (owner == null){
          owner = new User(_lastFakeId, 0, ownerDisplayName, false);
          _users.put(_lastFakeId, owner);
          _lastFakeId--;
        }

        owner.wasUsed = true;
        question._owner = owner;

        question._answerCount = Integer.valueOf(answerString);
        question._score = Integer.valueOf(scoreString);
        question._commentCount = Integer.valueOf(commentString);
        question._viewCount = Integer.valueOf(viewString);

        finished++;
        double perc = Math.round(finished / _questions.values().size() * 100);
        if (perc != lastperc) {
          lastperc = perc;
          System.out.println(perc+"%");
        }

      }
    }

  private static void readPostHistory(int year) throws IOException, ParseException {
    File postHistory = new File(Constants.postHistoryFileName+year);
    BufferedReader br = new BufferedReader(new FileReader(postHistory));

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    String line;

    while((line = br.readLine()) != null){
      if (!line.trim().startsWith("<row")) continue;

      String postId = readAttribute(line, Constants.PostId, Constants.AttributeStart, Constants.AttributeEnd);
      String typeText = readAttribute(line, Constants.PostHistoryTypeId, Constants.AttributeStart, Constants.AttributeEnd);
      String text = readAttribute(line, Constants.Text, Constants.AttributeStart, Constants.AttributeEnd);
      String date = readAttribute(line, Constants.CreationDate, Constants.AttributeStart, Constants.AttributeEnd);

      Date creation = df.parse(date);
      int id = Integer.valueOf(postId);
      int type = Integer.valueOf(typeText);

      if (type != 10 && type != 11) continue;
      Question question;

      if (_questions.containsKey(id)){
        question = _questions.get(id);
      } else {
        question = new Question(id);
      }

      List<User> users = extractUsers(text);

      if (type == 10){
        question.closedVoters = users;
        question.closedDate = creation;
      } else {
        question.reopenVoters = users;
        question.reopenDate = creation;
      }

      _questions.put(id, question);
    }

  }

  private static List<User> extractUsers(String text) {

    List<User> result = new ArrayList<>();
    text = text.substring(text.indexOf(Constants.VotersSub));
    int start = text.indexOf("[")+1;
    int end = text.indexOf("]");

    String array = text.substring(start, end);
    String[] entries = array.split("},");

    for (String entry : entries){

      String userID = readAttribute(entry, Constants.id, Constants.UserStart, "," + Constants.UserEnd);
      int id = Integer.valueOf(userID);


      User user;
      if (!_users.containsKey(id)){
        String userName = readAttribute(
                entry.substring(entry.indexOf(userID)+userID.length()), Constants.displayName, Constants.UserStart+Constants.UserEnd, Constants.UserEnd);

        user = new User(id, Constants.CloseRep, userName, true);
        _users.put(id, user);
      } else {
        user = _users.get(id);
      }

      user.wasUsed = true;

      result.add(user);
    }

    return result;
  }

  private static String readAttribute(String line, String id, String startString, String endString) {
    String result = "";
    String search = id+startString;

    int start = line.indexOf(search) + search.length();
    String sub = line.substring(start);
    int end = start+sub.indexOf(endString);

    result = line.substring(start, end);

    return result;
  }
}