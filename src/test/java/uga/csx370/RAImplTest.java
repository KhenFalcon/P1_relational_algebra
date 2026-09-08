package uga.csx370;

// Util Imports
import java.util.List;

import uga.csx370.mydb.Cell;
import uga.csx370.mydb.Predicate;
// UGA imports
import uga.csx370.mydb.Relation;
import uga.csx370.mydb.RelationBuilder;
import uga.csx370.mydb.Type;
import uga.csx370.mydbimpl.PredicateImpl;
import uga.csx370.mydbimpl.RAImpl;

// Testing framework imports
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A testing class for the RAImpl class. This class contains unit tests to verify the functionality
 * of the RAImpl class methods.
 * 
 * Test instance is set to the class level so that the private fields can be initialized once 
 * (in setup()) and reused across all test methods.
 * 
 * jcm68203
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RAImplTest {

    private RAImpl raImpl;
    private Relation instructor_relation;

    @BeforeAll
    public void setup() {
        raImpl = new RAImpl();
        assertNotNull(raImpl);

        instructor_relation = new RelationBuilder()
            .attributeNames(List.of("ID", "name", "dept_name", "salary"))
            .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.STRING, Type.DOUBLE))
            .build();
        instructor_relation.loadData("test-tables/instructor_export.csv");
        validInstructorRelation();
    }

    @Test
    public void contextLoads() { }

    @Test
    public void validInstantiation() {
        raImpl = new RAImpl();
        assertNotNull(raImpl);

    }

    @Test
    public void testSelect() {
        Predicate[] predicates = new PredicateImpl[] {
            new PredicateImpl(2, "=", Cell.val("Athletics")), // department name is Athletics
            new PredicateImpl(3, ">", Cell.val(100000.0)), // salary is greater than 100000.0
            new PredicateImpl(0, "<", Cell.val(100)), // ID is less than 100 (should return no rows)
            new PredicateImpl(1, "=", Cell.val("Mird")), // name is Mird
            new PredicateImpl(1, "!=", Cell.val("Mird")), // name is not Mird
            new PredicateImpl(0, "<=", Cell.val(10000)), // ID is less than or equal to 10000
            new PredicateImpl(0, ">", Cell.val(60000)), // ID is greater than 60000
            new PredicateImpl(0, "*", Cell.val(0)), // wildcard operator, should return all rows
        };

        for (Predicate p : predicates) {
            Relation result = raImpl.select(instructor_relation, p);
            
            assertNotNull(result);
            assertTrue(result.getSize() <= 50);
            assertFalse(result.getSize() < 0);
            
            System.out.println("Predicate: " + p.toString());
            result.print();
            System.out.println();
        }

        Predicate[] badPredicates = new PredicateImpl[] {
            new PredicateImpl(0, "=", Cell.val("Mird")), // ID is an integer, not a string
            new PredicateImpl(3, ">", Cell.val("Mird")), // salary is a double, not a string
            new PredicateImpl(1, "<", Cell.val(100)), // name is a string, not an integer
            new PredicateImpl(0, "&", Cell.val(100)), // invalid operator
            new PredicateImpl(-1, "=", Cell.val(100)), // invalid column index
            new PredicateImpl(100, "=", Cell.val(100)), // invalid column index
            new PredicateImpl(0, "", Cell.val(10000)), // Invalid operator (empty string)
            new PredicateImpl(0, null, Cell.val(10000)), // Invalid operator (null)
            new PredicateImpl(0, "=", null), // Invalid criterion (null)
        }; 

        for (Predicate p : badPredicates) {
            assertThrows(Exception.class, () -> {
                raImpl.select(instructor_relation, p);
            });
        }
    }

    @Test
    public void validInstructorRelation() {
        assertNotNull(instructor_relation);
        assertEquals(4, instructor_relation.getAttrs().size());
        assertEquals(4, instructor_relation.getTypes().size());
        assertEquals(50, instructor_relation.getSize());

        Cell firstCell = instructor_relation.getRow(0).get(0);
        assertNotNull(firstCell);
        assertEquals(Type.INTEGER, firstCell.getType());
    }
}
