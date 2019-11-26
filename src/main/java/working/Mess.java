package working;

import io.vavr.collection.Seq;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;

public class Mess {

  public static String message = "Messages";

  public static String[] messagesH =
      new String[] {"code", "name", "description", "subject",
          "email_templateId", "toast_template", "sms_template",};

  public Seq<R<QBaseMSGMessageTemplate>> getMessageRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllMessages());
  }

}
