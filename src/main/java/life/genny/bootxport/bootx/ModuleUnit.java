package life.genny.bootxport.bootx;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class ModuleUnit extends DataUnit {
    protected static final Logger log = org.apache.logging.log4j.LogManager.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
    private static final String RANGE = "!A1:Z";
    private static final String VALIDATION = "Validation";
    private static final String DATATYPE = "DataType";
    private static final String ATTRIBUTE = "Attribute";
    private static final String ATTRIBUTE_LINK = "AttributeLink";
    private static final String BASE_ENTITY = "BaseEntity";
    private static final String QUESTION_QUESTION = "QuestionQuestion";
    private static final String QUESTION = "Question";
    private static final String ASK = "Ask";
    private static final String NOTIFICATION = "Notifications";
    private static final String MESSAGE = "Messages";
    private static final String ENTITY_ATTRIBUTE= "EntityAttribute";
    private static final String ENTITY_ENTITY= "EntityEntity";
    private static final String DEF_BASE_ENTITY = "DEF_BaseEntity";
    private static final String DEF_ENTITY_ATTRIBUTE = "DEF_EntityAttribute";

    private ImportService service;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Set<String> initValidTitles() {
        Set<String> validSheetsTitle = new HashSet<>();
        validSheetsTitle.add(VALIDATION);
        validSheetsTitle.add(DATATYPE);
        validSheetsTitle.add(ATTRIBUTE);
        validSheetsTitle.add(ATTRIBUTE_LINK);
        validSheetsTitle.add(BASE_ENTITY);
        validSheetsTitle.add(QUESTION_QUESTION);
        validSheetsTitle.add(QUESTION);
        validSheetsTitle.add(ASK);
        validSheetsTitle.add(NOTIFICATION);
        validSheetsTitle.add(MESSAGE);
        validSheetsTitle.add(ENTITY_ATTRIBUTE);
        validSheetsTitle.add(ENTITY_ENTITY);
        validSheetsTitle.add(DEF_BASE_ENTITY);
        validSheetsTitle.add(DEF_ENTITY_ATTRIBUTE);
        return validSheetsTitle;
    }

    public ModuleUnit(BatchLoadMode mode, String sheetURI) {
        this.service = new ImportService(mode, SheetState.getState());

        System.out.println("Processing spreadsheet:" + sheetURI);
        Set<String> validSheetsTitle = initValidTitles();
        Sheets sheetsService = GoogleImportService.getInstance().getService();
        ArrayList<Sheet> sheets = getSheets(sheetsService,sheetURI);

        // get tiles
        Set<String> titles = new HashSet<>();
        for (Sheet sheet : sheets) {
            SheetProperties sheetProperties = (SheetProperties)sheet.get("properties");
            String title = sheetProperties.getTitle();
            if (validSheetsTitle.contains(title))
                titles.add(title);
        }

        ArrayList<ValueRange> valueRanges = getValueRanges(sheetsService, sheetURI, titles);

        processValues(sheetsService, titles, valueRanges, sheetURI);

    /*
        this.validations = service.fetchValidation(sheetURI);
        this.dataTypes = service.fetchDataType(sheetURI);
        this.attributes = service.fetchAttribute(sheetURI);
        this.attributeLinks = service.fetchAttributeLink(sheetURI);

        this.baseEntitys = service.fetchBaseEntity(sheetURI);
        this.questionQuestions = service.fetchQuestionQuestion(sheetURI);
        this.questions = service.fetchQuestion(sheetURI);
        this.asks = service.fetchAsk(sheetURI);
        this.notifications = service.fetchNotifications(sheetURI);

        this.messages = service.fetchMessages(sheetURI);
        this.entityAttributes = service.fetchEntityAttribute(sheetURI);
        this.entityEntitys = service.fetchEntityEntity(sheetURI);

        this.def_baseEntitys = service.fetchDefBaseEntity(sheetURI);
        this.def_entityAttributes = service.fetchDefEntityAttribute(sheetURI);
         */
        }

    // Get all sheets from spreadSheet
    private ArrayList<Sheet> getSheets(Sheets service, String spreadsheetId) {
        // The ranges to retrieve from the spreadsheet.
        List<String> ranges = new ArrayList<>();
        ArrayList<Sheet> sheets = new ArrayList<>();

        // True if grid data should be returned.
        // This parameter is ignored if a field mask was set in the request.
        boolean includeGridData = false;
        try {
            Sheets.Spreadsheets.Get request = service.spreadsheets().get(spreadsheetId);
            request.setRanges(ranges);
            request.setIncludeGridData(includeGridData);

            Spreadsheet response = request.execute();
            sheets = (ArrayList<Sheet>) response.get("sheets");
        } catch (IOException ioe) {
            log.error("IOException occurred when fetch SpreadSheets:" + spreadsheetId);
        }
        return sheets;
    }

    private ArrayList<ValueRange> getValueRanges(Sheets service, String spreadsheetId, Set<String> titles) {
        // The ranges to retrieve from the spreadsheet.
        List<String> ranges = new ArrayList<>();
        ArrayList<ValueRange> valueRanges = null;

        for (String title: titles) {
            ranges.add(title + RANGE);
        }

        // True if grid data should be returned.
        // This parameter is ignored if a field mask was set in the request.
        try {
            Sheets.Spreadsheets.Values.BatchGet request = service.spreadsheets().values().batchGet(spreadsheetId);
            request.setRanges(ranges);
            BatchGetValuesResponse response = request.execute();
            valueRanges = (ArrayList<ValueRange>) response.get("valueRanges");
        } catch (IOException ioe) {
            log.error("IOException occurred when fetch SpreadSheets:" + spreadsheetId);
        }
        return valueRanges;
    }

    private void processValues (Sheets sheetsService, Set<String> titles, ArrayList<ValueRange> valueRanges,
                                String sheetURI) {
        XlsxImportOnline xlsxImportOnline = new XlsxImportOnline(sheetsService);
        for (ValueRange valueRange : valueRanges) {
            String title = valueRange.getRange().split("!")[0];
            if (titles.contains(title)) {
                List<List<Object>> values = valueRange.getValues();
                System.out.println("processing " + title + ", value size:" + values.size());

                Map<String, Map<String, String>> tmp =  new HashMap<>();
                try {
                    tmp = xlsxImportOnline.mappingKeyHeaderToHeaderValues(values, DataKeyColumn.CODE);
                } catch (Exception ex) {
                    logFetchExceptionForSheets(ex.getMessage(), title, sheetURI);
                }
                switch (title) {
                    case VALIDATION:
                        this.validations = tmp;
                        break;
                    case DATATYPE:
                        this.dataTypes= tmp;
                        break;
                    case ATTRIBUTE:
                        this.attributes= tmp;
                        break;
                    case ATTRIBUTE_LINK:
                        this.attributeLinks= tmp;
                        break;
                    case BASE_ENTITY:
                        this.baseEntitys= tmp;
                        break;
                    case QUESTION_QUESTION:
                        this.questionQuestions= tmp;
                        break;
                    case QUESTION:
                        this.questions= tmp;
                        break;
                    case ASK:
                        this.asks= tmp;
                        break;
                    case NOTIFICATION:
                        this.notifications= tmp;
                        break;
                    case MESSAGE:
                        this.messages= tmp;
                        break;
                    case ENTITY_ATTRIBUTE:
                        this.entityAttributes= tmp;
                        break;
                    case ENTITY_ENTITY:
                        this.entityEntitys= tmp;
                        break;
                    case DEF_BASE_ENTITY:
                        this.def_baseEntitys= tmp;
                        break;
                    case DEF_ENTITY_ATTRIBUTE:
                        this.def_entityAttributes= tmp;
                        break;
                    default:
                        break;
                }
            }
        }
        System.out.println("All done");
    }

    private void logFetchExceptionForSheets(String exception, String sheetName, String sheetURI) {
        log.error("ATTENTION!, Exception:"  +  exception + " occurred when fetching sheetName:" + sheetName
                + ", sheetURI:" + sheetURI + ", return EMPTY HashMap!!!");
    }
}
