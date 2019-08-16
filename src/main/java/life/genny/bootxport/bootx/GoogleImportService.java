package life.genny.bootxport.bootx;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

public class GoogleImportService {

  private final JsonFactory JSON_FACTORY =
      JacksonFactory.getDefaultInstance();

  private HttpTransport HTTP_TRANSPORT;

  private final List<String> SCOPES =
      Arrays.asList(SheetsScopes.SPREADSHEETS);

  private Sheets service;

  public Sheets getService() {
    return service;
  }

  private static volatile GoogleImportService instance = null;

  public static GoogleImportService getInstance() {
    if (instance == null) {
      synchronized (GoogleImportService.class) {
        if (instance == null) {
          instance = new GoogleImportService();
        }
      }
    }
    return instance;
  }

  private GoogleImportService() {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      service = getSheetsService();
    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
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

}
