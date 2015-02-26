package de.fu.st.main.xmlparser;

/**
 * Created by Nemo on 22.02.2015.
 */
public class User {
  private final boolean _stillActive;
  public int _id;
  private int _reputation;
  public String displayName;

  public boolean wasUsed = false;

  public User(int id, int reputation, String displayName, boolean stillActive){
    _id = id;
    _reputation = reputation;
    this.displayName = displayName;
    _stillActive = stillActive;
  }

  public static String getHeader(){
    return "ID, Display Name, Reputation, Still Active\n";
  }

  public String toRow(){
    return _id + ","+ displayName +","+_reputation+","+String.valueOf(_stillActive).toUpperCase()+"\n";
  }
}
