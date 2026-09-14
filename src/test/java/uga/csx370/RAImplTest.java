package uga.csx370;

// Util Imports
import java.util.List;

// UGA imports
import uga.csx370.mydb.Cell;
import uga.csx370.mydb.Predicate;
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
        // --- Good Tests -------------------------------------------------------------------------
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

        // --- Bad Tests --------------------------------------------------------------------------
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
    public void testProject() {
        // --- Good Tests -------------------------------------------------------------------------
        List<List<String>> attrLists = List.of(
            List.of("ID", "name", "dept_name", "salary"), // all attributes
            List.of("ID", "name"), // first two attributes
            List.of("dept_name", "salary"), // last two attributes
            List.of("name"), // single attribute
            List.of("ID", "salary"), // non-contiguous attributes
            List.of("ID", "dept_name", "salary") // three attributes
        );

        for(List<String> attr : attrLists) {
            Relation result = raImpl.project(instructor_relation, attr);
            
            assertNotNull(result);
            assertTrue(result.getSize() <= 50);
            assertFalse(result.getSize() < 0);
            assertEquals(attr.size(), result.getAttrs().size());
            assertEquals(attr.size(), result.getTypes().size());

            System.out.println("Attributes: " + attr.toString());
            result.print();
            System.out.println();
        }

        // --- Bad Tests --------------------------------------------------------------------------
        List<List<String>> badAttrLists = List.of(
            List.of("ID", "name", "dept_name", "salary", "extra"), // extra attribute
            List.of("ID", "name", "dept_name", ""), // empty attribute
            List.of("ID", "name", "blahblah", "salary"), // invalid attribute
            List.of("@#$%", "name", "dept_name", "salary"), // invalid attribute
            List.of(),
            List.of("\n"),
            List.of("   ")
        );
        
        for(List<String> attr : badAttrLists) {
            assertThrows(Exception.class, () -> {
                raImpl.project(instructor_relation, attr);
            });
        }
    }

    @Test
    public void testUnion() {

        // Valid tests (borrowed predicates from Joshua's select tests!)
        PredicateImpl[] predicates = new PredicateImpl[] {
                new PredicateImpl(2, "=", Cell.val("Athletics")), // department name is Athletics
                new PredicateImpl(3, ">", Cell.val(100000.0)), // salary is greater than 100000.0
                new PredicateImpl(0, "<", Cell.val(100)), // ID is less than 100 (should return no rows)
                new PredicateImpl(1, "=", Cell.val("Mird")), // name is Mird
                new PredicateImpl(1, "!=", Cell.val("Mird")), // name is not Mird
                new PredicateImpl(0, "<=", Cell.val(10000)), // ID is less than or equal to 10000
                new PredicateImpl(0, ">", Cell.val(60000)), // ID is greater than 60000
                new PredicateImpl(0, "*", Cell.val(0)), // wildcard operator, should return all rows
        };

        for (int i = 0; i < predicates.length - 1; i++) {
            PredicateImpl predicate1 = predicates[i];
            PredicateImpl predicate2 = predicates[i + 1];

            Relation predicateResult1 = raImpl.select(instructor_relation, predicate1);
            Relation predicateResult2 = raImpl.select(instructor_relation, predicate2);

            assertNotNull(predicateResult1);
            assertTrue(predicateResult1.getSize() <= 50);
            assertFalse(predicateResult1.getSize() < 0);

            assertNotNull(predicateResult2);
            assertTrue(predicateResult2.getSize() <= 50);
            assertFalse(predicateResult2.getSize() < 0);

            Relation unionResult = raImpl.union(predicateResult1, predicateResult2);

            assertNotNull(unionResult);
            assertTrue(unionResult.getSize() >= predicateResult1.getSize());
            assertTrue(unionResult.getSize() >= predicateResult2.getSize());
            assertTrue(unionResult.getSize() <= predicateResult1.getSize() + predicateResult2.getSize());

            System.out.printf("Union relation of predicates %d & %d: ", i, i + 1);
            unionResult.print();
            System.out.println();
        }

        // Invalid Tests

        // Different Arity
        Relation projRel1 = raImpl.project(instructor_relation, List.of("ID", "name"));
        Relation projRel2 = raImpl.project(instructor_relation, List.of("ID", "name", "salary"));

        assertNotNull(projRel1);
        assertTrue(projRel1.getSize() <= 50);
        assertFalse(projRel1.getSize() < 0);

        assertNotNull(projRel2);
        assertTrue(projRel2.getSize() <= 50);
        assertFalse(projRel2.getSize() < 0);

        assertThrows(IllegalArgumentException.class, () -> raImpl.union(projRel1, projRel2));

        // Different Attribute Types
        Relation projRel3 = raImpl.project(instructor_relation, List.of("dept_name", "salary"));

        assertNotNull(projRel3);
        assertTrue(projRel3.getSize() <= 50);
        assertFalse(projRel3.getSize() < 0);

        assertThrows(IllegalArgumentException.class, () -> raImpl.union(projRel1, projRel3));
    }

    @Test
    public void testIntersect() {

        // Valid tests (borrowed predicates from Joshua's select tests!)
        PredicateImpl[] predicates = new PredicateImpl[] {
                new PredicateImpl(2, "=", Cell.val("Athletics")), // department name is Athletics
                new PredicateImpl(3, ">", Cell.val(100000.0)), // salary is greater than 100000.0
                new PredicateImpl(0, "<", Cell.val(100)), // ID is less than 100 (should return no rows)
                new PredicateImpl(1, "!=", Cell.val("Mird")), // name is not Mird
                new PredicateImpl(0, "<=", Cell.val(10000)), // ID is less than or equal to 10000
                new PredicateImpl(0, ">", Cell.val(60000)), // ID is greater than 60000
                new PredicateImpl(0, "*", Cell.val(0)), // wildcard operator, should return all rows
        };

        for (int i = 0; i < predicates.length - 1; i++) {
            PredicateImpl predicate1 = predicates[i];
            PredicateImpl predicate2 = predicates[i + 1];

            Relation predicateResult1 = raImpl.select(instructor_relation, predicate1);
            Relation predicateResult2 = raImpl.select(instructor_relation, predicate2);

            assertNotNull(predicateResult1);
            assertTrue(predicateResult1.getSize() <= 50);
            assertFalse(predicateResult1.getSize() < 0);

            assertNotNull(predicateResult2);
            assertTrue(predicateResult2.getSize() <= 50);
            assertFalse(predicateResult2.getSize() < 0);

            Relation intersectResult = raImpl.intersect(predicateResult1, predicateResult2);

            System.out.printf("Intersect relation of predicates %d & %d: ", i, i + 1);
            intersectResult.print();
            System.out.println();
        }

        // Invalid Tests

        // Different Arity
        Relation projRel1 = raImpl.project(instructor_relation, List.of("ID", "name"));
        Relation projRel2 = raImpl.project(instructor_relation, List.of("ID", "name", "salary"));

        assertNotNull(projRel1);
        assertTrue(projRel1.getSize() <= 50);
        assertFalse(projRel1.getSize() < 0);

        assertNotNull(projRel2);
        assertTrue(projRel2.getSize() <= 50);
        assertFalse(projRel2.getSize() < 0);

        assertThrows(IllegalArgumentException.class, () -> raImpl.intersect(projRel1, projRel2));

        // Different Attribute Types
        Relation projRel3 = raImpl.project(instructor_relation, List.of("dept_name", "salary"));

        assertNotNull(projRel3);
        assertTrue(projRel3.getSize() <= 50);
        assertFalse(projRel3.getSize() < 0);

        assertThrows(IllegalArgumentException.class, () -> raImpl.intersect(projRel1, projRel3));
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
