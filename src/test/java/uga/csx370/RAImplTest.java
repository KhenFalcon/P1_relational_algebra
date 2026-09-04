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
        Predicate predicate = new PredicateImpl(2, "=", Cell.val("Athletics"));

        Relation result = raImpl.select(instructor_relation, predicate);
        result.print();
        // assertNotNull(result);
        // assertEquals(3, result.getSize());
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
