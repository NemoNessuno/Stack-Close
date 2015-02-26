package de.fu.st.main.xmlparser;

/**
 * Created by Nemo on 22.02.2015.
 */
public class PostHistory {

  public enum Type{
    InitialTitle,
    InitialBody,
    InitialTags,
    EditTitle,
    EditBody,
    EditTags,
    RollbackTitle,
    RollbackBody,
    RollbackTags,
    PostClosed,
    PostReopened,
    PostDeleted,
    PostUndeleted,
    PostLocked,
    PostUnlocked,
    CommunityOwned,
    PostMigrated,
    QuestionMerged,
    QuestionProtected,
    QuestionUnprotected,
    PostDisassociated,
    QuestionUnmerged
  }

  private Type _type;
  private User _user;
  private String _userName;
  private String _text;

  public PostHistory(Type type, User user, String userName, String text){
    _type = type;
    _user = user;
    _userName = userName;
    _text = text;
  }
}
