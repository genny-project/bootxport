package life.genny.bootxport.export;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class ExportMain {

  public static Path parentPath = Paths.get(Paths.get(System.getProperty("user.home")).toString(),".genny", "multitenancy");
  
  static Multitenancy all = new Multitenancy();
  
  public static void main(String...str) {

    all.all();


    Set<String> collect = all.collect;
    

    collect.stream().forEach(System.out::println);

    List<Path> collect2 = collect.stream().filter(Objects::nonNull)
        .map(parentPath::resolve)
        .collect(Collectors.toList());

    List<File> collect3 = collect2.stream()
        .map(d -> d.resolve("modules").toFile())
        .collect(Collectors.toList());
    
    collect3.forEach(d -> d.mkdirs());
    
    Seq<Realm<BaseEntity>> beRealm = all.be.getBERealm();
    Seq<Realm<Map<String, String>>> attrRealm = all.attr.getAttrRealm();
    Seq<Realm<Map<String, String>>> dataTypeRealm = all.attr.getDataTypeRealm();
    Seq<Realm<EntityAttribute>> entAttrRealm = all.entAttr.getEntAttrRealm();
    Seq<Realm<Ask>> askRealm = all.ask.getAskRealm();
    Seq<Realm<Question>> questionRealm = all.que.getQuestionRealm();
    Seq<Realm<QuestionQuestion>> queQueRealm = all.queQue.getQueQueRealm();
    Seq<Realm<QBaseMSGMessageTemplate>> messageRealm = all.mess.getMessageRealm();
    Seq<Realm<EntityEntity>> entEntRealm = all.entEnt.getEntEntRealm();
    Seq<Realm<Validation>> valRealm = all.val.getValRealm();       

    beRealm.forEach(d -> export(d, all.be.be ,Arrays.asList(all.be.baseEntityH)));
    attrRealm.forEach(d -> export(d, all.attr.attr ,Arrays.asList(all.attr.attributeH)));
    dataTypeRealm.forEach(d -> export(d, all.attr.dtype ,Arrays.asList(all.attr.dataTypeH)));
    entAttrRealm.forEach(d -> export(d, all.entAttr.entAttr ,Arrays.asList(all.entAttr.entityAttributeH)));
    askRealm.forEach(d -> export(d, all.ask.ask ,Arrays.asList(all.ask.askH)));
    questionRealm.forEach(d -> export(d, all.que.question ,Arrays.asList(all.que.questionH)));
    queQueRealm.forEach(d -> export(d, all.queQue.questionQuestion ,Arrays.asList(all.queQue.questionQuestionH)));
    messageRealm.forEach(d -> export(d, all.mess.message ,Arrays.asList(all.mess.messagesH)));
    entEntRealm.forEach(d -> export(d, all.entEnt.entEnt ,Arrays.asList(all.entEnt.entityEntityH)));
    valRealm.forEach(d -> export(d, all.val.val, Arrays.asList(all.val.validationH)));
   
    

    System.out.println("Almost!");
    Map<String, List<Map<String, String>>> map = moduleRealmsWithPaths.entrySet().stream()
      .map(d ->{
        Map<String,List<Map<String,String>>> p = new HashMap<>();
            p.put(
                d.getKey(),
                d.getValue()
                        .stream()
                        .map(a -> {
                          Map<String,String> p1 = new HashMap<>();
                          p1.put("sheetID",a);
                          p1.put("name",d.getKey());
                          return p1;
                        })
                        .collect(Collectors.toList()));
            return p;
      }).reduce((a,b)->{
      
       b.putAll(a);
      return b;
    }).get();
    
    map.entrySet().forEach(d -> exportModule(d.getValue(),d.getKey(), Arrays.asList("sheetID","name")));


    System.out.println("Finished");
//    exportModule(moduleRealmsWithPaths, "Modules", Arrays.asList("sheetId"));
    List<Map<String, String>> collect4 = collect.stream().map(d -> {
      Map<String, String> map1 = new HashMap<>();
      map1.put("sheetID", Paths.get(".genny","multitenancy",d, "modules".concat(".xlsx")).toString());
      map1.put("name", d);
      map1.put("code", d);

//      .put(Paths.get("/Users/helios/.genny/multitenancy",d, "modules.xlsx"));
      return map1;

    }).collect(Collectors.toList()) ;

    exportMultitenancy(collect4, Arrays.asList("sheetID", "name","code") );
  }
  
  public static Tuple2<Path,Path> getPathsEntities(String realm, String name){
    Path parent = Paths.get("/Users/helios/.genny/multitenancy",realm,"modules",name.concat("-template.xlsx"));
    Path output = Paths.get("/Users/helios/.genny/multitenancy", realm, "modules",name.concat(".xlsx"));
   return Tuple.of(parent, output); 
  }

  public static Tuple2<Path,Path> getPathsModules(String realm){
    Path parent = Paths.get("/Users/helios/.genny/multitenancy",realm,"modules".concat("-template.xlsx"));
    Path output = Paths.get("/Users/helios/.genny/multitenancy", realm, "modules".concat(".xlsx"));
   return Tuple.of(parent, output); 
  }

  public static Tuple2<Path,Path> getPathsMultitenancy(){
    Path parent = Paths.get("/Users/helios/.genny/multitenancy", "multitenancy".concat("-template.xlsx"));
    Path output = Paths.get("/Users/helios/.genny/multitenancy", "multitenancy".concat(".xlsx"));
   return Tuple.of(parent, output); 
  }

  static Map<String,List<String>> moduleRealmsWithPaths = new HashMap<>();

  public static <T> void export(Realm<T> r, String name, List<String> header) {

    Tuple2<Path, Path> paths = getPathsEntities(r.name,name);

    if(moduleRealmsWithPaths.containsKey(r.name)) {
      List<String> list = moduleRealmsWithPaths.get(r.name);
      list.add(paths._2.subpath(2, paths._2.getNameCount()).toString());
    }
    else {
      List<String> list = new ArrayList<>();
      list.add(paths._2.subpath(2, paths._2.getNameCount()).toString());
      moduleRealmsWithPaths.put(r.name,list);
    }

    Path parent = paths._1;
    Path output = paths._2;
    
    File moduleDomain = parent.toFile();
    
    try {
      List<String> tablesNames = Arrays.asList(name);
      ApplicationSheet.createWorkSheets(tablesNames, parent.toString());
      Transformer tm = null;
      try {
        InputStream is = moduleDomain.toURI().toURL().openStream();
        OutputStream os = new FileOutputStream(output.toString());
        tm = PoiTransformer.createTransformer(is, os);
      } catch (InvalidFormatException | IOException e) {}
      try {
        ApplicationSheet.applyToWorksheet(name,r.entity , header, tm);
      } catch (InvalidFormatException | IOException e) {
        e.printStackTrace();
      }
      tm.write();
      moduleDomain.delete();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static <T> void exportModule(List<Map<String,String>> props, String realm, List<String> header) {

    Tuple2<Path, Path> paths = getPathsModules(realm);

    Path parent = paths._1;
    Path output = paths._2;
    
    File moduleDomain = parent.toFile();
    
    try {

      List<String> tablesNames = Arrays.asList("Modules");
      ApplicationSheet.createWorkSheets(tablesNames, parent.toString());
      Transformer tm = null;
      try {
        InputStream is = moduleDomain.toURI().toURL().openStream();
        OutputStream os = new FileOutputStream(output.toString());
        tm = PoiTransformer.createTransformer(is, os);
      } catch (InvalidFormatException | IOException e) {}
      try {
        ApplicationSheet.applyToWorksheet("Modules",props, header, tm);
      } catch (InvalidFormatException | IOException e) {
        e.printStackTrace();
      }

      tm.write();
      moduleDomain.delete();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static <T> void exportMultitenancy(List<Map<String,String>> props, List<String> header) {

    Tuple2<Path, Path> paths = getPathsMultitenancy();

    Path parent = paths._1;
    Path output = paths._2;
    
    File moduleDomain = parent.toFile();
    
    try {

      List<String> tablesNames = Arrays.asList("Projects");
      ApplicationSheet.createWorkSheets(tablesNames, parent.toString());
      Transformer tm = null;
      try {
        InputStream is = moduleDomain.toURI().toURL().openStream();
        OutputStream os = new FileOutputStream(output.toString());
        tm = PoiTransformer.createTransformer(is, os);
      } catch (InvalidFormatException | IOException e) {}
      try {
        ApplicationSheet.applyToWorksheet("Projects",props, header, tm);
      } catch (InvalidFormatException | IOException e) {
        e.printStackTrace();
      }

      tm.write();
      moduleDomain.delete();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String lastFromSplit(String str) {
    String[] split = str.split("(\\s*(\\.|\\s)\\s*)");
    int lenght = split.length - 1;
    return split[lenght];
  }
}
