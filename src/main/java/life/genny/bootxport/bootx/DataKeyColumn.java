package life.genny.bootxport.bootx;

import java.util.HashSet;
import java.util.Set;

public class DataKeyColumn {

  public static Set<String> CODE = new HashSet<>();

  public static Set<String> CODE_BA =
      new HashSet<>();

  public static Set<String> CODE_TARGET_PARENT_LINK =
      new HashSet<>();

  public static Set<String> CODE_TARGET_PARENT = new HashSet<>();

  public static Set<String> CODE_QUESTION_SOURCE_TARGET =
      new HashSet<>();

  static {

    CODE.add("code".replaceAll("^\"|\"$|_|-", ""));

    CODE_BA.add("baseEntityCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
    CODE_BA.add("attributeCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

    CODE_TARGET_PARENT_LINK.add("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
    CODE_TARGET_PARENT_LINK.add("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
    CODE_TARGET_PARENT_LINK.add("linkCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

    CODE_TARGET_PARENT.add("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
    CODE_TARGET_PARENT.add("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
    CODE_TARGET_PARENT.add("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
//    CODE_TARGET_PARENT.add("linkCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

    CODE_QUESTION_SOURCE_TARGET.add("question_code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
    CODE_QUESTION_SOURCE_TARGET.add("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
    CODE_QUESTION_SOURCE_TARGET.add("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
  }
}
