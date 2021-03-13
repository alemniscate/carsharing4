package carsharing;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.io.*;

import java.sql.Connection; 
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; 
import java.sql.Statement;  

public class Main { 
  
    public static void main(String[] args) { 
        Arguments arguments = new Arguments(args);
        String dbFileName = arguments.get("-databaseFileName", "carsharing");
//        System.out.println(ReadText.getAbsolutePath(dbFileName));
        Database db = new Database(dbFileName);
        Scanner scanner = new Scanner(System.in);

        Action ac = new Action(scanner, db);

        while (true) {
            System.out.println("1. Log in as a manager");
            System.out.println("2. Log in as a customer");
            System.out.println("3. Create a customer");
            System.out.println("0. Exit");
            int menuno = Integer.parseInt(scanner.nextLine());
            System.out.println();
            switch (menuno) {
                case 1:
                    ac.managerLogin();
                    break;
                case 2:
                    ac.customerLogin();
                    break;
                case 3:
                    ac.createCustomer();
                    break;
                case 0:
                    ac.exit();
                    break;
            }
            if (menuno == 0) {
                break;
            }
            System.out.println();
        }

        scanner.close();
    }  
 
}

class Action {
  
    Scanner scanner;
    Database db;

    Action(Scanner scanner, Database db) {
        this.scanner = scanner;
        this.db = db;
    }

    void managerLogin() {
        while (true) {
            System.out.println("1. Company list");
            System.out.println("2. Create a company");
            System.out.println("0. Back");
            int menuno = Integer.parseInt(scanner.nextLine());
            System.out.println();
            switch (menuno) {
                case 1:
                    companyList();
                    break;
                case 2:
                    createCompany();
                    break;
            }
            if (menuno == 0) {
                return;
            }
            System.out.println();
        }
    }

    void companyList() {
        Company company = chooseCompany("the");
        if (company == null) {
            return;
        }
        companyLogin(company);
    }

    void createCompany() {
        System.out.println("Enter the company name:");
        String name = scanner.nextLine();
        if (db.addCompany(name)) {
            System.out.println("The company was created!");
        }
    }

    void exit() {
    }

    void companyLogin(Company company) {
   
        while (true) {
            System.out.println(String.format("'%s' company", company.getName()));
            System.out.println("1. Car list");
            System.out.println("2. Create a car");
            System.out.println("0. Back");
            int menuno2 = Integer.parseInt(scanner.nextLine());
            System.out.println();
            switch (menuno2) {
                case 1:
                    carList(company);
                    break;
                case 2:
                    createCar(company);
                    break;
            }
            if (menuno2 == 0) {
                break;
            }
            System.out.println();
        }
    }
    
    void carList(Company company) {
        List<String> list = db.getCarList(company);
        if (list.isEmpty()) {
            System.out.println("The car list is empty!");
            return;
        }
        System.out.println("Car List:");
        IntStream.range(0, list.size()).forEach(i -> System.out.println((i + 1) + ". " + list.get(i)));
    }

    void createCar(Company company) {
        System.out.println("Enter the car name:");
        String name = scanner.nextLine();
        if (db.addCar(name, company)) {
            System.out.println("The car was created!");
        }
    }

    void createCustomer() {
        System.out.println("Enter the customer name:");
        String name = scanner.nextLine();
        if (db.addCustomer(name)) {
            System.out.println("The customer was added!");
        }
    }

    void customerLogin() {
        List<String> list = db.getCustomerList();
        if (list.isEmpty()) {
            System.out.println("The customer list is empty!");
            return;
        }
   
        System.out.println("Customer list:");
        IntStream.range(0, list.size()).forEach(i -> System.out.println((i + 1) + ". " + list.get(i)));
        System.out.println("0. Back");
        int menuno = Integer.parseInt(scanner.nextLine());
        if (menuno == 0) {
            return;
        }
        System.out.println();
        Customer customer = db.getCustomer(menuno);
        customerMenu(customer);
    }
   
    void customerMenu(Customer customer) {
        while (true) {
            System.out.println("1. Rent a car");
            System.out.println("2. Return a rented car");
            System.out.println("3. My rented car");
            System.out.println("0. Back");
            int menuno = Integer.parseInt(scanner.nextLine());
            System.out.println();
            switch (menuno) {
                case 1:
                    rentCar(customer);
                    break;
                case 2:
                    returnCar(customer);
                    break;
                case 3:
                    rentedCar(customer);
                    break;
            }
            if (menuno == 0) {
                break;
            }
            System.out.println();
        }
    }

    void rentCar(Customer customer) {
        if (customer.getRentedCarId() != null) {
            System.out.println("You've already rented a car!");
            return;
        }
        Company company = chooseCompany("a");
        if (company == null) {
            return;
        }
        Car car = chooseCar(company);
        if (car == null) {
            return;
        }
        db.updateCustomer(customer, car);
        customer.setRentedCarId(car.getId());
        System.out.println(String.format("You rented '%s'", car.getName()));
    }

    void returnCar(Customer customer) {
        if (customer.getRentedCarId() == null) {
            System.out.println("You didn't rent a car!");
            return;
        }
        db.updateCustomer(customer, null); 
        customer.setRentedCarId(null);
        System.out.println("You've returned a rented car!");
    }

    void rentedCar(Customer customer) {
        if (customer.getRentedCarId() == null) {
            System.out.println("You didn't rent a car!");
            return;
        }
        Car car = db.getCar(customer);
        Company company = db.getCompany(car);
        System.out.println("Your rented car:");
        System.out.println(car.getName());
        System.out.println("Company:");
        System.out.println(company.getName());    
    }

    Company chooseCompany(String article) {
        List<String> list = db.getCompanyList();
        if (list.isEmpty()) {
            System.out.println("The company list is empty!");
            return null;
        }
   
        System.out.println(String.format("Choose %s company:", article));
        IntStream.range(0, list.size()).forEach(i -> System.out.println((i + 1) + ". " + list.get(i)));
        System.out.println("0. Back");
        int menuno = Integer.parseInt(scanner.nextLine());
        if (menuno == 0) {
            return null;
        }
        System.out.println();
        Company company = db.getCompany(menuno);
        return company;
    }

    Car chooseCar(Company company) {
        List<String> list = db.getCarList(company);
        if (list.isEmpty()) {
            System.out.println("The car list is empty!");
            return null;
        }
   
        System.out.println("Choose a car:");
        IntStream.range(0, list.size()).forEach(i -> System.out.println((i + 1) + ". " + list.get(i)));
        System.out.println("0. Back");
        int menuno = Integer.parseInt(scanner.nextLine());
        if (menuno == 0) {
            return null;
        }
        System.out.println();
        Car car = db.getCar(company, menuno);
        return car;
    }

}

class Car {
    int id;
    String name;
    int companyId;

    Car(int id, String name, int companyId) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
    }

    int getId() {
        return id;
    }

    String getName() {
        return name;
    }
    
    int getCompanyId() {
        return companyId;
    }
}

class Customer {
    int id;
    String name;
    Integer rentedCarId;

    Customer(int id, String name, Integer rentedCarId) {
        this.id = id;
        this.name = name;
        this.rentedCarId = rentedCarId;
    }

    int getId() {
        return id;
    }

    String getName() {
        return name;
    }

    Integer getRentedCarId() {
        return rentedCarId;
    }

    void setRentedCarId(Integer rentedCarId) {
        this.rentedCarId = rentedCarId;
    }
}

class Company {
    int id;
    String name;

    Company(int id, String name) {
        this.id = id;
        this.name = name;
    }

    int getId() {
        return id;
    }

    String getName() {
        return name;
    }
}

class Database {
    String folderName = "./src/carsharing/db/";
    String JDBC_DRIVER = "org.h2.Driver";   
    String USER = "sa"; 
    String PASS = ""; 
    Connection con = null; 
    PreparedStatement addCompanyPs;
    PreparedStatement listCompanyPs;
    PreparedStatement getCompanyPs;
    PreparedStatement getCompanyFromIdPs;
    PreparedStatement addCarPs;
    PreparedStatement listCarPs;
    PreparedStatement getCarPs;
    PreparedStatement getCarFromIdPs;
    PreparedStatement addCustomerPs;
    PreparedStatement listCustomerPs;
    PreparedStatement getCustomerPs;
    PreparedStatement updateCustomerPs;
  
    Database (String fileName) {
        String dbFileName = folderName + fileName;
        boolean dbExistFlag = ReadText.isExist(dbFileName + ".mv.db");
        String url = "jdbc:h2:" + dbFileName; 
        Statement stmt = null;
        String sql; 
        try { 
            Class.forName(JDBC_DRIVER);
            con = DriverManager.getConnection(url);  
            con.setAutoCommit(true);

            if (!dbExistFlag) {

                stmt = con.createStatement(); 
                sql =  "CREATE TABLE IF NOT EXISTS COMPANY (" + 
                " ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, " + 
                " NAME VARCHAR(255)" +
                ")";  
                stmt.executeUpdate(sql);
                sql = "ALTER TABLE COMPANY ADD CONSTRAINT NAME UNIQUE (NAME);";
                stmt.executeUpdate(sql);
                sql = "ALTER TABLE COMPANY ALTER COLUMN NAME VARCHAR(255) NOT NULL;";
                stmt.executeUpdate(sql);

                sql = "CREATE TABLE CAR (" +
                " ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " NAME VARCHAR(255) NOT NULL UNIQUE," +
                " COMPANY_ID INTEGER NOT NULL," +
                " CONSTRAINT COMPANY_ID FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID)" +
                " ON DELETE CASCADE" +
                " ON UPDATE CASCADE" +
                ");";
                stmt.executeUpdate(sql);

                sql = "CREATE TABLE CUSTOMER (" +
                " ID INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " NAME VARCHAR(255) NOT NULL UNIQUE," +
                " RENTED_CAR_ID INTEGER," +
                " CONSTRAINT RENTED_CAR_ID FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID)" +
                " ON DELETE SET NULL" +
                " ON UPDATE SET NULL" +
                ");";
                stmt.executeUpdate(sql);

                stmt.close(); 
            }
            
            sql = "INSERT INTO COMPANY (NAME) VALUES (?);";
            addCompanyPs = con.prepareStatement(sql);

            sql = "SELECT * FROM COMPANY;";
            listCompanyPs = con.prepareStatement(sql);

            sql = "SELECT * FROM COMPANY WHERE NAME = ?;";
            getCompanyPs = con.prepareStatement(sql);

            sql = "SELECT * FROM COMPANY WHERE ID = ?;";
            getCompanyFromIdPs = con.prepareStatement(sql);

            sql = "INSERT INTO CAR (NAME, COMPANY_ID) VALUES (?, ?);";
            addCarPs = con.prepareStatement(sql);

            sql = "SELECT * FROM CAR WHERE COMPANY_ID = ?;";
            listCarPs = con.prepareStatement(sql);

            sql = "SELECT * FROM CAR WHERE NAME = ?;";
            getCarPs = con.prepareStatement(sql);

            sql = "SELECT * FROM CAR WHERE ID = ?;";
            getCarFromIdPs = con.prepareStatement(sql);

            sql = "INSERT INTO CUSTOMER (NAME, RENTED_CAR_ID) VALUES (?, NULL);";
            addCustomerPs = con.prepareStatement(sql);

            sql = "SELECT * FROM CUSTOMER;";
            listCustomerPs = con.prepareStatement(sql);

            sql = "SELECT * FROM CUSTOMER WHERE NAME = ?;";
            getCustomerPs = con.prepareStatement(sql);

            sql = "UPDATE CUSTOMER SET RENTED_CAR_ID = ? WHERE ID = ?";
            updateCustomerPs = con.prepareStatement(sql);

        } catch(SQLException | ClassNotFoundException e) { 
            e.printStackTrace(); 
        } 
    }

    boolean addCustomer(String name) {
        try {
            addCustomerPs.setString(1, name);
            addCustomerPs.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean addCompany(String name) {
        try {
            addCompanyPs.setString(1, name);
            addCompanyPs.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    List<String> getCompanyList() {
        List<String> list = new ArrayList<>();
        try {
            ResultSet rs = listCompanyPs.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    List<String> getCustomerList() {
        List<String> list = new ArrayList<>();
        try {
            ResultSet rs = listCustomerPs.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    Company getCompany(int menuno) {
        List<String> list = getCompanyList();
        String name = list.get(menuno - 1);
        try {
            getCompanyPs.setString(1, name);
            ResultSet rs = getCompanyPs.executeQuery(); 
            if (rs.next()) {
                int id = rs.getInt("ID");
                Company company = new Company(id, name);
                return company;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    Customer getCustomer(int menuno) {
        List<String> list = getCustomerList();
        String name = list.get(menuno - 1);
        try {
            getCustomerPs.setString(1, name);
            ResultSet rs = getCustomerPs.executeQuery(); 
            if (rs.next()) {
                int id = rs.getInt("ID");
                Integer rentedCarId = (Integer) rs.getObject("RENTED_CAR_ID");
                Customer customer = new Customer(id, name, rentedCarId);
                return customer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean addCar(String name, Company company) {
        try {
            addCarPs.setString(1, name);
            addCarPs.setInt(2, company.getId());
            addCarPs.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    List<String> getCarList(Company company) {
        List<String> list = new ArrayList<>();
        try {
            listCarPs.setInt(1, company.getId());
            ResultSet rs = listCarPs.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    Car getCar(Company company, int menuno) {
        List<String> list = getCarList(company);
        String name = list.get(menuno - 1);
        try {
            getCarPs.setString(1, name);
            ResultSet rs = getCarPs.executeQuery(); 
            if (rs.next()) {
                int id = rs.getInt("ID");
                int companyId = rs.getInt("COMPANY_ID");
                Car car = new Car(id, name, companyId);
                return car;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean updateCustomer(Customer customer, Car car) {
        try {
            if (car == null) {
                updateCustomerPs.setNull(1, java.sql.Types.NULL);
            } else {
                updateCustomerPs.setInt(1, car.getId());
            }
            updateCustomerPs.setInt(2, customer.getId());
            updateCustomerPs.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    Car getCar(Customer customer) {
        try {
            Integer id = customer.getRentedCarId();
            getCarFromIdPs.setInt(1, id);
            ResultSet rs = getCarFromIdPs.executeQuery(); 
            if (rs.next()) {
                String name = rs.getString("NAME");
                int companyId = rs.getInt("COMPANY_ID");
                Car car = new Car(id, name, companyId);
                return car;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    Company getCompany(Car car) {
        try {
            int companyId = car.getCompanyId();
            getCompanyFromIdPs.setInt(1, companyId);
            ResultSet rs = getCompanyFromIdPs.executeQuery(); 
            if (rs.next()) {
                int id = rs.getInt("ID");
                String name = rs.getString("NAME");
                Company company = new Company(id, name);
                return company;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
} 

class Arguments {

    Map<String, String> argMap;

    Arguments(String[] args) {
        
        List<String> argList = Arrays.asList(args);
        argMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argMap.put(argList.get(i), argList.get(i + 1));
        }
    }

    String get(String key, String defaultValue) {
        if (argMap.isEmpty()) {
            return defaultValue;
        }

        if (argMap.get(key) == null) {
            return defaultValue;
        }
        
        return argMap.get(key);
    }
}

class ReadText {

    static boolean isExist(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    static String getAbsolutePath(String fileName) {
        File file = new File(fileName);
        return file.getAbsolutePath();
    }

    static String readAllWithoutEol(String fileName) {
        String text = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));   
            text =  br.lines().collect(Collectors.joining());        
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return text;
    }

    static List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));   
            lines =  br.lines().collect(Collectors.toList());        
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return lines;
    }

    static String readAll(String fileName) {
        char[] cbuf = new char[4096];
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));           
            while (true) {
                int length = br.read(cbuf, 0, cbuf.length);
                if (length != -1) {
                    sb.append(cbuf, 0, length);
                }
                if (length < cbuf.length) {
                    break;
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return sb.toString();
    }
}
