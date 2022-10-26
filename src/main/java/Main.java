import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException,
            ParserConfigurationException, SAXException, ParseException {
        String[] employee1 = "1,John,Smith,USA,25".split(",");
        String[] employee2 = "2,Ivan,Petrov,RU,23".split(",");
        try (CSVWriter writer = new CSVWriter(new FileWriter("data.csv"))) {
            writer.writeNext(employee1);
            writer.writeNext(employee2);
        } catch (IOException exp) {
            exp.printStackTrace();
        }

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> listCSV = parseCSV(columnMapping, fileName);

        String json = listToJson(listCSV);
        writeString(json, "data.json");

        List<Employee> listXML = parseXML("data.xml");
        String xmlToJson = listToJson(listXML);
        writeString(xmlToJson, "data2.json");

        String jsonStar = readString("new_data.json");
        List<Employee> list = jsonToList(jsonStar);
        list.forEach(System.out::println);

    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> employeesList = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {

            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            employeesList = csv.parse();
        } catch (IOException exp) {
            exp.printStackTrace();
        }
        return employeesList;
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        Type listType = new TypeToken<List<Employee>>() {}.getType();
        String json = gson.toJson(list, listType);
        return json;
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(json);
            file.flush();
        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public static List<Employee> parseXML(String fileName) throws ParserConfigurationException,
            IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileName));
        Node root = doc.getDocumentElement();
        List<Employee> listToReturn = new ArrayList<>();
        readXML(root, listToReturn);
        return listToReturn;
    }

    private static void readXML(Node node, List<Employee> list) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);
            if (Node.ELEMENT_NODE == node_.getNodeType()) {
                if (node_.getNodeName() == "employee") {
                    Element employee_ = (Element) node_;
                    long id = Long.parseLong(employee_.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = employee_.getElementsByTagName("firstName").item(0).getTextContent();
                    String secondName = employee_.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = employee_.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(employee_.getElementsByTagName("age").item(0).getTextContent());
                    Employee employee = new Employee(id, firstName, secondName, country, age);
                    list.add(employee);
                }
            }
            readXML(node_, list);
        }
    }

    public static String readString(String fileName) throws IOException, ParseException, ClassCastException {
        JSONParser parser = new JSONParser();
        String jsonString = null;
        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONArray jsonArray = (JSONArray) obj;
            jsonString = (String) jsonArray.toJSONString();
        } catch (IOException | ParseException | ClassCastException exp) {
            exp.printStackTrace();
        }
        return jsonString;
    }

    public static List<Employee> jsonToList(String jsonText) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        return gson.fromJson(jsonText, listType);
    }
}
