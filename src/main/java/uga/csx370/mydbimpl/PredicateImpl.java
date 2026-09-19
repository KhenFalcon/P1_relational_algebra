package uga.csx370.mydbimpl;

import java.util.List;

import uga.csx370.mydb.Cell;
import uga.csx370.mydb.Predicate;
import uga.csx370.mydb.Type;

/**
 * A short implementation of the Predicate interface. This class is used primarily to test the 
 * implementation of RAImpl.java. This class has three instance fields: 
 * 1) the column index of the row to be checked
 * 2) the operator to be used for comparison
 * 3) the criterion to compare against
 * 
 * Valid operators are: "=", "!=", "<", "<=", ">", ">=", "*"
 * 
 * Warning: the criterion is a cell object and its type must match the type of the column being 
 * checked, check() will throw an IllegalArgumentException if the types do not match.
 * 
 */
public class PredicateImpl implements Predicate {

    private int columnIndex;
    private String operator;
    private Cell criterion;

    // new attributes to support column-to-column comparision, e.g. joins
    private int columnIndex2;
    private boolean columnComparison; // boolean value to determine if we are doing column to column comparison

    public PredicateImpl(int columnIndex, String operator, Cell criterion) {
        this.columnIndex = columnIndex;
        this.operator = operator;
        this.criterion = criterion;
        this.columnComparison = false; // checking is a single column meets a certain criteria -> not column to column
    }

    // Constructor to be able to do column to column comparison for joins
    public PredicateImpl(int columnIndex, String operator, int columnIndex2) {
        this.columnIndex = columnIndex;
        this.operator = operator;
        this.columnIndex2 = columnIndex2;
        this.columnComparison = true;
    }


    @Override
    public boolean check(List<Cell> row) {
        Cell cell = row.get(columnIndex);
        Cell cell2 = columnComparison ? row.get(columnIndex2) : this.criterion;
        // determines if we are doing column to column comparison
        // if columnComparison = true, we get the other column for columnIndex2
        // if columnComparison = false, we get the criterion to evaluate on cell;

        if (cell.getType() != cell2.getType())
            throw new IllegalArgumentException("Compared column's type does not match criterion type.");
        
        // check for general comparisons that are valid for all types (i.e. equality and inequality)
        switch (operator) {
            case "=":
                return cell.equals(cell2);
            case "!=":
                return !cell.equals(cell2);
            case "*":
                return true;
        }

        // check for comparisons that are only valid for numeric types (doubles and ints) (i.e. <, <=, >, >=)
        try {
            // Use Integer.compare() or Double.compare() to generalize the comparison logic to one variable
            double resultant = cell.getType() == Type.INTEGER 
                ? Integer.compare(cell.getAsInt(), cell2.getAsInt())
                : Double.compare(cell.getAsDouble(), cell2.getAsDouble());

            switch (operator) {
                case "<":
                    return resultant < 0;
                case "<=":
                    return resultant <= 0;
                case ">":
                    return resultant > 0;
                case ">=":
                    return resultant >= 0;
                default:
                    throw new IllegalArgumentException("Invalid operator: " + operator);
            }
            
        } catch (RuntimeException e) { // Specifically catch for an illegal cell type conversion exception (i.e. trying to compare "<String> greter than or equal to <String>")
            if(e.getMessage().equals("Illegal cell type conversion."))
                throw new IllegalArgumentException("Comparison type \'" + operator + "\'' is not supported for type " + cell.getType());
            else 
                throw e;
        }
    }

    @Override
    public String toString() {
        if (columnComparison) {
            return "PredicateImpl [columnIndex=" + columnIndex + ", operator=" + operator + ", columnIndex2=" + columnIndex2 + "]";
        }
        return "PredicateImpl [columnIndex=" + columnIndex + ", operator=" + operator + ", criterion=" + criterion
                + "]";
    }
}
