package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Employee extends Model
{
    private Long employeeId;
    private Long reportsTo;
    private String firstName;
    private String lastName;
    private String email;
    private String title;

    public Employee()
    { // new employee for insert
    }

    private Employee(ResultSet results) throws SQLException
    {
        firstName = results.getString("FirstName");
        lastName = results.getString("LastName");
        email = results.getString("Email");
        employeeId = results.getLong("EmployeeId");
        reportsTo = results.getLong("ReportsTo");
        title = results.getString("Title");
    }

    public static List<Employee.SalesSummary> getSalesSummaries()
    {
        //COMPLETE - a GROUP BY query to determine the sales (look at the invoices table), using the SalesSummary class
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT *, COUNT(InvoiceId) as SalesCount, SUM(Total) as SalesTotal" +
                                 " FROM employees" +
                                 " JOIN customers ON customers.SupportRepId = employees.EmployeeId" +
                                 " JOIN invoices ON invoices.CustomerId = customers.CustomerId" +
                                 " GROUP BY employees.Email"))
            {

                ResultSet results = stmt.executeQuery();
                List<Employee.SalesSummary> resultList = new LinkedList<>();

                while (results.next())
                {
                    resultList.add(new SalesSummary(results));
                }
                return resultList;
            }
            catch (SQLException sqlException)
            {
                throw new RuntimeException(sqlException);
            }
    }

    @Override
    public boolean verify()
    {
        _errors.clear(); // clear any existing errors
        if (firstName == null || "".equals(firstName)) {
            addError("First name can't be null or blank!");
        }
        if (lastName == null || "".equals(lastName)) {
            addError("Last name can't be null or blank!");
        }
        if (email == null || "".equals(email)) {
            addError("Email name can't be null or blank!");
        }
        if (email != null  && email.indexOf('@') == (-1) ) {
            addError("All emails require a @ symbol.");
        }
        return !hasErrors();
    }

    @Override
    public boolean update()
    {
        if (verify())
        {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE employees SET FirstName=?, LastName=?, Email=? WHERE EmployeeId=?"))
            {
                stmt.setString(1, this.getFirstName());
                stmt.setString(2, this.getLastName());
                stmt.setString(3, this.getEmail());
                stmt.setLong(4, this.getEmployeeId());

                stmt.executeUpdate();
                return true;
            }
            catch (SQLException sqlException)
            {
                throw new RuntimeException(sqlException);
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean create()
    {
        if (verify())
        {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO employees (FirstName, LastName, Email) VALUES (?, ?, ?)"))
            {
                stmt.setString(1, this.getFirstName());
                stmt.setString(2, this.getLastName());
                stmt.setString(3, this.getEmail());

                stmt.executeUpdate();
                employeeId = DB.getLastID(conn);
                return true;
            }
            catch (SQLException sqlException)
            {
                throw new RuntimeException(sqlException);
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public void delete()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM employees WHERE EmployeeID=?"))
        {
            stmt.setLong(1, this.getEmployeeId());

            stmt.executeUpdate();
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Long getEmployeeId() {
        return employeeId;
    }
    public List<Customer> getCustomers() {
        return Customer.forEmployee(employeeId);
    }
    public Long getReportsTo() {
        return reportsTo;
    }
    public void setReportsTo(Long reportsTo) {
        this.reportsTo = reportsTo;
    }

    public List<Employee> getReports()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM employees WHERE ReportsTo=?"))
        {
            stmt.setLong(1, this.getEmployeeId());

            ResultSet results = stmt.executeQuery();
            List<Employee> resultList = new LinkedList<>();

            while (results.next())
            {
                resultList.add(new Employee(results));
            }
            return resultList;
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public Employee getBoss()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees WHERE EmployeeId=?"))
        {
            stmt.setLong(1, this.getReportsTo());

            ResultSet results = stmt.executeQuery();
            if (results.next())
            {
                return new Employee(results);
            }
            else
            {
                return null;
            }
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Employee> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Employee> all(int page, int count)
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM employees LIMIT ? OFFSET ?"))
        {
            int offsetNum = (page - 1) * count; // Page # - One and Multiply By One Hundred --> (i.e. 1 - > 0, 2 - > 100, 3 - > 200, etc.)

            stmt.setInt(1, count);
            stmt.setInt(2, offsetNum);

            ResultSet results = stmt.executeQuery();
            List<Employee> resultList = new LinkedList<>();

            while (results.next())
            {
                resultList.add(new Employee(results));
            }
            return resultList;
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public static Employee findByEmail(String newEmailAddress)
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees WHERE Email=?"))
        {
            stmt.setString(1, newEmailAddress);

            ResultSet results = stmt.executeQuery();
            if (results.next())
            {
                return new Employee(results);
            }
            else
            {
                return null;
            }
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public static Employee find(long employeeId)
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees WHERE EmployeeId=?"))
        {
            stmt.setLong(1, employeeId);

            ResultSet results = stmt.executeQuery();
            if (results.next())
            {
                return new Employee(results);
            }
            else
            {
                return null;
            }
        }
        catch (SQLException sqlException)
        {
            throw new RuntimeException(sqlException);
        }
    }

    public void setTitle(String programmer) {
        title = programmer;
    }
    public String getTitle() {
        return title;
    }
    // COMPLETE implement
    public void setReportsTo(Employee employee) { reportsTo = employee.getEmployeeId(); }

    public static class SalesSummary
    {
        private String firstName;
        private String lastName;
        private String email;
        private Long salesCount;
        private BigDecimal salesTotals;
        private SalesSummary(ResultSet results) throws SQLException
        {
            firstName = results.getString("FirstName");
            lastName = results.getString("LastName");
            email = results.getString("Email");
            salesCount = results.getLong("SalesCount");
            salesTotals = results.getBigDecimal("SalesTotal");
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() { return email; }

        public Long getSalesCount() {
            return salesCount;
        }

        public BigDecimal getSalesTotals() {
            return salesTotals;
        }
    }
}
