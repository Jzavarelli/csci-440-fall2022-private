package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeHelper {

    public static String makeEmployeeTree()
    {
        // TODO, change this to use a single query operation to get all employees
        Employee employee = Employee.find(1); // root employee
        // and use this data structure to maintain reference information needed to build the tree structure

        List<Employee> all = Employee.all();
        Map<Long, List<Employee>> employeeMap = new HashMap<>();
        for (Employee currentEmployee : all)
        {
            employeeMap.put(currentEmployee.getEmployeeId(), currentEmployee.getReports());
            // create a map of id-> all employees that report to that id.
        }

        return "<ul>" + makeTree(employee, employeeMap) + "</ul>";
    }

    // TODO - currently this method just uses the employee.getReports() function, which
    //  issues a query.  Change that to use the employeeMap variable instead
    public static String makeTree(Employee employee, Map<Long, List<Employee>> employeeMap)
    {
        String list = "<li><a href='/employees" + employee.getEmployeeId() + "'>"
                + employee.getEmail() + "</a><ul>";

        List<Employee> reports = employeeMap.get(employee.getEmployeeId());
        for (Employee report : reports)
        {
            list += makeTree(report, employeeMap);
        }

        return list + "</ul></li>";
    }
}
