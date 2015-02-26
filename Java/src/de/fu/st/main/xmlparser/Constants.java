package de.fu.st.main.xmlparser;

/**
 * Created by Nemo on 24.02.2015.
 */
public class Constants {

  public static String base = "E:\\StackExchange Dump\\stackexchange\\Stackoverflow";
  public static String postsFileName = base+"\\Posts\\Posts.xml";
  public static String postHistoryFileName = base+"\\PostHistory.xml";
  public static String userFileName = base+"\\Users.xml";
  public static String pruned = "_pruned";

  public static String id = "Id";
  public static String reputation = "Reputation";
  public static String displayName = "DisplayName";
  public static String upVotes = "UpVotes";
  public static String downVotes = "DownVotes";

  public static String PostHistoryTypeId = "PostHistoryTypeId";
  public static String PostId = "PostId";
  public static String CreationDate = "CreationDate";
  public static String Text = "Text";

  public static String OwnerUserId = "OwnerUserId";
  public static String OwnerDisplayName = "OwnerDisplayName";

  public static String Score = "Score";
  public static String ViewCount = "ViewCount";
  public static String AnswerCount = "AnswerCount";
  public static String CommentCount = "CommentCount";

  public static int CloseRep = 3000;

  public static String AttributeStart = "=\"";
  public static String AttributeEnd = "\"";
  public static String UserStart = "&quot;:";
  public static String UserEnd = "&quot;";
  public static String VotersSub = "Voters";

  public static String OutputUser = base+"\\Out\\UserOut";
  public static String OutputQuestions = base+"\\Out\\QuestionsOut";

}
