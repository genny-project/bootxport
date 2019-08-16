package life.genny.bootxport.xlsimport;

import java.io.FileInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.vavr.Function2;
import io.vavr.Function3;
import life.genny.bootxport.bootx.QwandaRepository;
import life.genny.bootxport.bootx.QwandaRepositoryImpl;
import life.genny.bootxport.utils.HibernateUtil;


public class XlsImportOnline {

  private final String RANGE = "!A1:Z";


  private final JsonFactory JSON_FACTORY =
      JacksonFactory.getDefaultInstance();

  private HttpTransport HTTP_TRANSPORT;

  private final List<String> SCOPES =
      Arrays.asList(SheetsScopes.SPREADSHEETS);

  private Sheets service;

  private XlsImportOnline() {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      service = getSheetsService();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  private static volatile XlsImportOnline instance = null;

  public static XlsImportOnline getInstance() {
    if (instance == null) {
      synchronized (XlsImportOnline.class) {
        if (instance == null) {
          instance = new XlsImportOnline();
        }
      }
    }
    return instance;
  }


  public Sheets getSheetsService() throws Exception {
    final Credential credential = authorize();
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        credential).build();
  }

  public Credential authorize() throws Exception {
    Optional<String> path =
        Optional.ofNullable(System.getenv("GOOGLE_SVC_ACC_PATH"));

    GoogleCredential credential =
        GoogleCredential.fromStream(new FileInputStream(path.get()),
            HTTP_TRANSPORT, JSON_FACTORY).createScoped(SCOPES);

    System.out.println("Spreadsheets being read with user id: "
        + credential.getServiceAccountId());
    System.out.println(credential.getTokenServerEncodedUrl());
    return credential;
  }

  public List<Map<String, String>> toTableFormat(
      final List<List<Object>> values) {

    List<String> header = values.get(0)
        .stream()
        .map(d -> d.toString())
        .collect(Collectors.toList());
    final List<Map<String, String>> k = new ArrayList<>();
    for (final Object key : values.get(0)) {
      header.add((String) key);
    }
    values.remove(0);
    for (final List row : values) {
      final Map<String, String> mapper =
          new HashMap<String, String>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(header.get(counter), row.get(counter).toString());
      }
      k.add(mapper);
    }
    return k;
  }

  public Map<String, Map<String, String>> toTableFormatInKey(
      final List<List<Object>> values, Set<String> keyColumns) {

    List<String> header = values.get(0)
        .stream()
        .map(d -> d.toString())
        .collect(Collectors.toList());

    final Map<String, Map<String, String>> k = new HashMap<>();
    for (final Object key : values.get(0)) {
      header.add((String) key);
    }
    values.remove(0);
    for (final List row : values) {
      final Map<String, String> mapper =
          new HashMap<String, String>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(header.get(counter), row.get(counter).toString());
      }
      String join = mapper.keySet().stream()
          .filter(keyColumns::contains).map(mapper::get).collect(Collectors.joining());


      k.put(join, mapper);
    }
    return k;
  }

  private List<Map<String, String>> table(final String sheetId,
      final String sheetName) {
    final String absoluteRange = sheetName + RANGE;
    ValueRange response = null;
    // try {
    try {
      response = service.spreadsheets().values()
          .get(sheetId, absoluteRange).execute();
    } catch (IOException e) {
      System.out.println("Does not exist");
    }
    List<List<Object>> data = null;
    try {
      data = response.getValues();
    } catch (NullPointerException e) {
      System.out.println("Looks like you dont have internet");
    }
    return toTableFormat(data);

  };

  private Map<String, Map<String, String>> tableInKey(
      final String sheetId, final String sheetName,
      Set<String> keyColumns) {
    final String absoluteRange = sheetName + RANGE;
    ValueRange response = null;
    // try {
    try {
      response = service.spreadsheets().values()
          .get(sheetId, absoluteRange).execute();
    } catch (IOException e) {
    }
    List<List<Object>> data = null;
    try {
      data = response.getValues();
    } catch (NullPointerException e) {
    }
    return toTableFormatInKey(data, keyColumns);

  };

  public Function2<String, String, List<Map<String, String>>> getInTableFormat =
      Function2.of(this::table).memoized();

  public Function3<String, String, Set<String>, Map<String, Map<String, String>>> getInTableFormatInKey =
      Function3.of(this::tableInKey).memoized();


  public void update() {

    getInTableFormat = Function2.of(this::table).memoized();
    getInTableFormatInKey = Function3.of(this::tableInKey).memoized();
  }

  public static void main(String... args)
      throws GoogleJsonResponseException {
    XlsImportOnline xlsOnline = XlsImportOnline.getInstance();
    long timeBefore = 0;
    long timeAfter = 0;
    timeBefore = System.currentTimeMillis();
    List<Realm> realms = xlsOnline.getInTableFormat
        .apply("1zzz6bYXuryASR09Tsyok4_qiJI9n81DBsxD4oFBk5mw",
            "Projects")
        .stream().map(d -> {

          Realm mx = new Realm(d.get("code"), d);
          return mx;
        }).collect(Collectors.toList());
    realms.forEach(d -> System.out.println(d.getBaseEntity().size()));
    
    
//    timeAfter = System.currentTimeMillis();
//    BootstrapState state = BootstrapState.getInstance();
////    state.setRealms(realms);
//    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();   
//    Session openSession = sessionFactory.openSession();
//    EntityManager createEntityManager = openSession.getEntityManagerFactory().createEntityManager();
//    QwandaRepository repo = new QwandaRepositoryImpl(createEntityManager);
//    BatchLoading bl = new BatchLoading(repo);
////    bl.persistProject(realms2.get(0));
//    realms.stream().forEach(bl::persistProject);
//
//    sessionFactory.close();
//    System.out.println(timeAfter - timeBefore);
  }

}
//1239
//317
//352
//345

