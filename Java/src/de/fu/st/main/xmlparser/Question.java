package de.fu.st.main.xmlparser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Nemo on 22.02.2015.
 */
public class Question {

  private int _id;
  public int _score;
  public int _viewCount;
  public int _answerCount;
  public int _commentCount;
  public Date closedDate;
  public Date reopenDate;
  public User _owner;
  public List<User> closedVoters;
  public List<User> reopenVoters;

  public Question(int id, User owner) {
    closedVoters = new ArrayList<>();
    reopenVoters = new ArrayList<>();

    _id = id;
  }

  public static String getHeader(){
    return "ID, Score, Answers, Owner, Closed Date, Closed Voters, Reopen Date, Reopen Voters\n";
  }

  public String toRow(){
    if (_owner == null){
      return "";
    }
    return _id+","+_score+","+_answerCount+","+
            _owner._id+","
            +(closedDate != null ? closedDate : "")+","+getListString(closedVoters)+","
            +(reopenDate != null ? reopenDate :"")+","+getListString(reopenVoters)+"\n";
  }

  private String getListString(List<User> users) {
    StringBuilder sb = new StringBuilder("(");

    for(int i = 0; i < users.size(); i++){
      sb.append(users.get(i)._id);
      if (i < users.size()-1) sb.append(";");
    }

    sb.append(")");
    return sb.toString();
  }
}
