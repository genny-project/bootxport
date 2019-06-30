package life.genny.bootxport.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import io.vavr.collection.Tree;
import io.vavr.collection.Tree.Node;

public class DirectoryStructure {

  String pathParent = System.getProperty("user.home")
      .concat("/.genny/configurations");

  final String MULTITENANCY = "multitenancy";

  final String MHOST_FILE = "m-host";

  // final List<String> realms = new ArrayList<String>();

  final String moduleFile = "realm-modules";

  final String MODULE_DIRECTORY = "modules";

  final String MODULE_FILE = "module-gen";


  Node<String> mHost = Tree.of(MHOST_FILE);

  List<Node<String>> realmDirectory;

  Node<String> module = Tree.of(MODULE_FILE);

  Node<String> moduleDirectory;

  List<Node<String>> rootDirectory = new ArrayList<Node<String>>();
  List<Node<String>> realms = new ArrayList<Node<String>>();

  {
    realmDirectory = new ArrayList<Node<String>>();
    realmDirectory.add(module);
    realmDirectory.add(moduleDirectory);
  }

  public void setRootDirectory(List<Node<String>> elements) {

    rootDirectory.addAll(elements);
  }


  Node<String> root;


  public void setRoot(Node<String> root) {

    this.root = root;
  }

  public void setModules(List<Node<String>> modules) {
    moduleDirectory = Tree.of(MODULE_DIRECTORY, modules);
  }


  public void setRealms(List<Node<String>> realms) {

    this.realms = realms;
  }

  public void generateRealm() {

  }

  public void setRealm(Node<String> realm) {
    realms.add(realm);
  }

  private List<String> moduleNames;
  // private List<String> realmDir = new ArrayList<String>();


  public void setModuleNames(List<String> moduleNames) {
    this.moduleNames = moduleNames;
  }

  public List<String> getModuleNames() {
    return this.moduleNames;
  }

  public List<Node<String>> generateModuleNode() {
    return getModuleNames().stream().map(Tree::of)
        .collect(Collectors.toList());
  }


  public Node<String> generateModuleRoot() {
    return Tree.of("Modules", generateModuleNode());
  }


  public Node<String> setUpDirectoryTree() {

    io.vavr.collection.List<Realm> multitenancy2 =
        Processor.getProcessor().multitenancy;

    String pathParent = System.getProperty("user.home")
        .concat("/.genny/configurations");

    // DirectoryStructure ds = new DirectoryStructure();

    List<RealmDirectory> javaList = multitenancy2.map(realm -> {
      realm.getName();
      RealmDirectory rd1 = new RealmDirectory();
      rd1.setName(realm.getName());
      List<String> moduleNames = new ArrayList<String>();
      moduleNames.add(realm.getName().concat(".xlsx"));
      moduleNames.add(realm.getName().concat(".xlsx"));
      setModuleNames(moduleNames);
      rd1.setModule(generateModuleRoot());
      return rd1;
    }).toJavaList();

    List<RealmDirectory> zs = new ArrayList<RealmDirectory>();


    zs.addAll(javaList);

    List<Node<String>> realmDir = zs.stream()
        .map(realm -> Tree.of(realm.getName(), realm.getModuleSet()))
        .collect(Collectors.toList());
    Node<String> mHost = Tree.of("Multitenancy.xlsx");
    realmDir.add(mHost);
    Node<String> rootTree =
        Tree.of(pathParent.concat("/multitenancy"), realmDir);

    return rootTree;

  }



  public List<String> g(List<Node<String>> t, String path) {
    System.out.println(path);
    String r= "";
    for (Node<String> l : t) {
      if (l.getChildren().isEmpty()) {
        System.out.println("Should be file: " + l.get() + " on path "
            + path.concat("/".concat(l.get())));

        
            r = path.concat("/".concat(l.get()));


      } else {
//        System.out.println("Should be Dir: " + l.get());
        g(l.getChildren().toJavaList(),
            path.concat("/".concat(l.get())));
      }
    }
    ArrayList<String> arrayList = new ArrayList<String>() {};

    arrayList.add(r);
    return arrayList;
  }
  
  
  public List<String> getFilePaths(){
    
    return null;
  }

//  public static void main(String... args) {
//    DirectoryStructure ds = new DirectoryStructure();
//
//    // ds.setUpDirectoryTree().traverse().forEach(node -> System.out.println(node.draw() + " is empty? "
//    // + node.isBranch()));;
//    // ds.setUpDirectoryTree().traverse().fo)
//    Node<String> tree = ds.setUpDirectoryTree();
//    ds.g(tree.getChildren().toJavaList(), tree.get());
//    // ds.gettr(ds.setUpDirectoryTree());
//    // System.out.println();
//    // System.out.println();
//    //
//
//
//
//    // Node<String> root = ds.setUpDirectoryTree();
//    //
//    //
//    // root.traverse().forEach(d -> {
//    //
//    // System.out.println("- " + d);
//    // System.out.println("- " + d.getChildren());
//    //
//    // });
//
//
//    // root.reduceLeftOption((o, p) -> {
//    // System.out.println();
//    //
//    // System.out.println(o.concat("/".concat(p)));
//    // return o.concat("/".concat(p));
//    // });
//    // Node<String> string =
//    // ds.setUpDirectoryTree().traverse().reduce((acc, n) -> {
//    //
//    // String h = StringUtils.join(acc, "/", n);
//    // Node<String> node =
//    // Tree.of(StringUtils.join(acc.get(), "/", n.get()));
//    // System.out.println(node);
//    // return node;
//    // });
//    //
//    // string.traverse().forEach(node -> System.out
//    // .println(node.draw() + " is empty? " + node.isBranch()));
//
//  }

}

