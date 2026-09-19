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
    private Relation student_relation;
    private Relation classroom_relation;

    @BeforeAll
    public void setup() {
        raImpl = new RAImpl();
        assertNotNull(raImpl);

        instructor_relation = new RelationBuilder()
            .attributeNames(List.of("ID", "name", "dept_name", "salary"))
            .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.STRING, Type.DOUBLE))
            .build();
        instructor_relation.loadData("test-tables/instructor.csv");
        validInstructorRelation();

        student_relation = new RelationBuilder()
             .attributeNames(List.of("ID", "name", "dept_name", "tot_cred"))
             .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.STRING, Type.INTEGER))
            .build();
        student_relation.loadData("test-tables/student.csv");
        validStudentRelation();

        classroom_relation = new RelationBuilder()
            .attributeNames(List.of("building", "room_number", "capacity"))
            .attributeTypes(List.of(Type.STRING, Type.INTEGER, Type.INTEGER))
            .build();
        classroom_relation.loadData("test-tables/classroom.csv");
        validClassroomRelation();
    }

    @Test
    public void contextLoads() { }

    @Test
    public void validInstantiation() {
        raImpl = new RAImpl();
        assertNotNull(raImpl);
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

    @Test
    public void validStudentRelation() {
        assertNotNull(student_relation);
        assertEquals(4, student_relation.getAttrs().size());
        assertEquals(4, student_relation.getTypes().size());
        assertEquals(2000, student_relation.getSize());

        Cell firstCell = student_relation.getRow(0).get(0);
        assertNotNull(firstCell);
        assertEquals(Type.INTEGER, firstCell.getType());
    }

    @Test
    public void validClassroomRelation() {
        assertNotNull(classroom_relation);
        assertEquals(3, classroom_relation.getAttrs().size());
        assertEquals(3, classroom_relation.getTypes().size());
        assertEquals(30, classroom_relation.getSize());

        Cell firstCell = classroom_relation.getRow(0).get(0);
        assertNotNull(firstCell);
        assertEquals(Type.STRING, firstCell.getType());
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
            new PredicateImpl(0, "*", 0), // wildcard operator, should return all rows
        };

        for (Predicate p : predicates) {
            Relation result = raImpl.select(instructor_relation, p);
            
            assertNotNull(result);
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
            List<String> attrs_before_project = instructor_relation.getAttrs();
            System.out.println(attrs_before_project);
            Relation result = raImpl.project(instructor_relation, attr);
            
            assertNotNull(result);
            assertTrue(result.getSize() == 50);
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

            System.out.printf("Union relation of predicates %d & %d: \n", i, i + 1);
            System.out.printf("\t%s \n\t%s\n", predicate1.toString(), predicate2.toString());
            unionResult.print();
            System.out.println();
        }

        // Invalid Tests

        Relation[] badRelations = {
            raImpl.project(instructor_relation, List.of("ID", "name")),
            raImpl.project(instructor_relation, List.of("ID", "name", "salary")), // additional attribute
            raImpl.project(instructor_relation, List.of("ID", "name")), // missing attribute
            raImpl.project(instructor_relation, List.of("name", "salary")), // partial mismatch (incl. type mismatch)
            raImpl.project(instructor_relation, List.of("ID", "dept_name")) // complete mismatch (incl. type mismatch)
        };

        for (int i = 0; i < badRelations.length - 1; i++) {
            Relation br1 = badRelations[i];
            Relation br2 = badRelations[i + 1];

            // test that the relations are not flawed on their own
            assertNotNull(br1);
            assertTrue(br1.getSize() <= 50);
            assertFalse(br1.getSize() < 0);
            
            assertNotNull(br2);
            assertTrue(br2.getSize() <= 50);
            assertFalse(br2.getSize() < 0);

            // confirm that union of the two relations will fail
            assertThrows(IllegalArgumentException.class, () -> raImpl.union(br1, br2));
        }
    }

    @Test
    public void testIntersect() {

        // Valid tests (borrowed predicates from Joshua's select tests!)
        PredicateImpl[] predicates = new PredicateImpl[] {
                new PredicateImpl(2, "=", Cell.val("Athletics")), // department name is Athletics
                new PredicateImpl(3, ">", Cell.val(100000.0)), // salary is greater than 100000.0
                new PredicateImpl(0, "<", Cell.val(100)), // ID is less than 100 (should return no rows)
                new PredicateImpl(1, "!=", Cell.val("Mird")), // name is not Mird
                new PredicateImpl(0, ">", Cell.val(60000)), // ID is greater than 60000
                new PredicateImpl(0, "<=", Cell.val(10000)), // ID is less than or equal to 10000
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

            System.out.printf("Intersect relation of predicates %d & %d: \n", i, i + 1);
            System.out.printf("\t%s \n\t%s\n", predicate1.toString(), predicate2.toString());
            intersectResult.print();
            System.out.println();
        }

        // Invalid Tests

        // Different Arity
        Relation[] badRelations = {
            raImpl.project(instructor_relation, List.of("ID", "name")),
            raImpl.project(instructor_relation, List.of("ID", "name", "salary")), // additional attribute
            raImpl.project(instructor_relation, List.of("ID", "name")), // missing attribute
            raImpl.project(instructor_relation, List.of("name", "salary")), // partial mismatch (incl. type mismatch)
            raImpl.project(instructor_relation, List.of("ID", "dept_name")) // complete mismatch (incl. type mismatch)
        };

        for (int i = 0; i < badRelations.length - 1; i++) {
            Relation br1 = badRelations[i];
            Relation br2 = badRelations[i + 1];

            // test that the relations are not flawed on their own
            assertNotNull(br1);
            assertTrue(br1.getSize() <= 50);
            assertFalse(br1.getSize() < 0);
            
            assertNotNull(br2);
            assertTrue(br2.getSize() <= 50);
            assertFalse(br2.getSize() < 0);

            // confirm that union of the two relations will fail
            assertThrows(IllegalArgumentException.class, () -> raImpl.intersect(br1, br2));
        }
    }

    @Test
    public void testDiff() {
        // --- Good Tests -------------------------------------------------------------------------
        Predicate[] inversePredicates = {
                new PredicateImpl(2, "=", Cell.val("Athletics")), // department name is Athletics
                new PredicateImpl(2, "!=", Cell.val("Athletics")), // department name is not Athletics
                new PredicateImpl(3, ">", Cell.val(100000.0)), // salary is greater than 100000.0
                new PredicateImpl(3, "<=", Cell.val(100000.0)), // salary is less than or equal to 100000.0
                new PredicateImpl(0, "<", Cell.val(100)), // ID is less than 100 (should return no rows)
                new PredicateImpl(0, ">=", Cell.val(100)), // ID is greater than or equal to 100 (should return all rows)
                new PredicateImpl(1, "=", Cell.val("Mird")), // name is Mird
                new PredicateImpl(1, "!=", Cell.val("Mird")) // name is not Mird
        };

        for (int i = 0; i < inversePredicates.length && i < inversePredicates.length - 1; i += 2) {
            Relation r1 = raImpl.select(instructor_relation, inversePredicates[i]);
            Relation r2 = raImpl.select(instructor_relation, inversePredicates[i + 1]); // should always be the inverse of r1

            Relation diff1 = raImpl.diff(instructor_relation, r1);
            Relation diff2 = raImpl.diff(instructor_relation, r2); // should always be tihe inverse of diff1

            // NOTE: assertEquals doesn't work for relation-type objects

            // assert size comparisons
            assertTrue(diff1.getSize() == r2.getSize());
            assertTrue(diff2.getSize() == r1.getSize());
            assertTrue(r1.getSize() == instructor_relation.getSize() - diff1.getSize());
            assertTrue(r2.getSize() == instructor_relation.getSize() - diff2.getSize());

            assertTrue(r1.getSize() >= 0);
            assertTrue(r2.getSize() >= 0);
            assertTrue(diff1.getSize() >= 0);
            assertTrue(diff2.getSize() >= 0);

            assertTrue(r1.getSize() <= instructor_relation.getSize());
            assertTrue(r2.getSize() <= instructor_relation.getSize());
            assertTrue(diff1.getSize() <= instructor_relation.getSize());
            assertTrue(diff2.getSize() <= instructor_relation.getSize());
        }
        
        // --- Bad Tests --------------------------------------------------------------------------

        Relation[] incompatableRelations = {
                instructor_relation,
                raImpl.project(instructor_relation, List.of("ID", "dept_name")), // fewer, matching attributes, matching entries
                raImpl.project(instructor_relation, List.of("ID", "salary")), // same number, but mismatching attributes, matching entries 
                raImpl.project(instructor_relation, List.of("name", "salary")), // same number, but wrong attributes 
                instructor_relation, // more, matching attributes, matching entries
                student_relation // completely different table
        };

        for (int i = 0; i < incompatableRelations.length - 1; i++) {
            Relation ir1 = incompatableRelations[i];
            Relation ir2 = incompatableRelations[i + 1];

            assertNotNull(ir1);
            assertNotNull(ir2);
    

            assertThrows(Exception.class, () -> {
                raImpl.diff(ir1, ir2);
            });
        }
    }

    @Test
    public void testRename() {
        // --- Good Tests -------------------------------------------------------------------------
        List<List<String>> renameLists = List.of(
            List.of("ID", "name", "dept_name", "salary"), 
            List.of("rel1_ID", "rel1_name", "rel1_dept_name", "rel1_salary"),
                            
            List.of("ID", "name"),
            List.of("rel1.ID", "rel1.name"),
                            
            List.of("salary"), 
            List.of("rel1.salary"),
                    
            List.of("dept_name"), 
            List.of("re1.dept_name")
        );

        for (int i = 0; i < renameLists.size() && i < renameLists.size() - 1; i += 2) {
            List<String> currNames = renameLists.get(i);
            List<String> newNames = renameLists.get(i + 1);

            assertTrue(currNames.size() == newNames.size());
            assertTrue(instructor_relation.getAttrs().containsAll(currNames));
            newNames.forEach(newName -> assertFalse(instructor_relation.getAttrs().contains(newName)));

            Relation renamedRelation = raImpl.rename(instructor_relation, currNames, newNames);

            assertTrue(renamedRelation.getAttrs().containsAll(newNames));
            currNames.forEach(currName -> assertFalse(renamedRelation.getAttrs().contains(currName)));

            for (int j = 0; j < currNames.size(); j++)
                assertTrue(instructor_relation.getAttrIndex(currNames.get(j)) == renamedRelation
                        .getAttrIndex(newNames.get(j)));

            renamedRelation.print();
        }
        
        // --- Bad Tests --------------------------------------------------------------------------
        List<List<String>> badLists = List.of(
            // empty lists
            // List.of(), 
            // List.of(),
            // mismatchcing lengths (extra rename)
            List.of("ID"),
            List.of("rel1.ID", "rel1.name"),
            // mismatching lengths (extra name)
            List.of("dept_name", "salary"), 
            List.of("re1.dept_name"),
            // non-existant attribute
            List.of("YRALAS"), 
            List.of("rel1.salary")
        );

        for (int i = 0; i < badLists.size() && i < badLists.size() - 1; i += 2) {
            List<String> currNames = badLists.get(i);
            List<String> newNames = badLists.get(i + 1);

            assertThrows(Exception.class, () -> {
                raImpl.rename(instructor_relation, currNames, newNames);
            });
        }
    }

    @Test
    public void testCartesianProduct() {
        // --- Good Tests -------------------------------------------------------------------------
        Relation[] goodRealtions = {
                raImpl.select(instructor_relation, new PredicateImpl(2, "=", Cell.val("Athletics"))), // department name is Athletics
                raImpl.select(classroom_relation, new PredicateImpl(0, "=", Cell.val("Gates"))), // building is gates
                raImpl.select(instructor_relation, new PredicateImpl(3, ">", Cell.val(100000.0))), // salary is greater than 100000.0
                raImpl.select(classroom_relation, new PredicateImpl(1, ">", Cell.val(900))), // building is gates
                raImpl.select(instructor_relation, new PredicateImpl(0, "<", Cell.val(100))), // ID is less than 100 (should return no rows)
                raImpl.select(classroom_relation, new PredicateImpl(2, "<", Cell.val(15))), // building is gates
        };

        for (int i = 0; i < goodRealtions.length - 1; i++) {
            Relation cartProd = raImpl.cartesianProduct(goodRealtions[i], goodRealtions[i + 1]);

            assertNotNull(cartProd);
            assertTrue(cartProd.getSize() >= 0);
            assertTrue(cartProd.getSize() <= instructor_relation.getSize() * classroom_relation.getSize());
            assertTrue(cartProd.getSize() == goodRealtions[i].getSize() * goodRealtions[i + 1].getSize());

            cartProd.print();
        }
        
        // --- Bad Tests --------------------------------------------------------------------------
        Relation[] badRelations = {
                raImpl.select(instructor_relation, new PredicateImpl(2, "=", Cell.val("Athletics"))),
                raImpl.select(instructor_relation, new PredicateImpl(2, "=", Cell.val("Athletics"))), // duplicate relation
                raImpl.select(instructor_relation, new PredicateImpl(2, "=", Cell.val("Pol. Sci"))), // Same attributes, different entries
        };

        for (int i = 0; i < badRelations.length - 1; i++) {
            Relation br1 = badRelations[i];
            Relation br2 = badRelations[i + 1];

            assertThrows(Exception.class, () -> {
                raImpl.cartesianProduct(br1, br2);
            });
        }
    }

    @Test
    public void testNaturalJoin() {

        Relation dept_relation = new RelationBuilder().attributeNames(List.of("dept_name", "building", "budget")).attributeTypes(List.of(Type.STRING, Type.STRING, Type.DOUBLE)).build();
        dept_relation.loadData("test-tables/department.csv");
        Relation natural_join = raImpl.join(instructor_relation, dept_relation);
        natural_join.print();
        System.out.println(natural_join.getAttrs());

    }

    @Test
    public void testThetaJoin() {
        Relation dept_relation = new RelationBuilder().attributeNames(List.of("dept_name", "building", "budget")).attributeTypes(List.of(Type.STRING, Type.STRING, Type.DOUBLE)).build();
        dept_relation.loadData("test-tables/department.csv");

        Relation rename_dept = raImpl.rename(dept_relation, List.of("dept_name"), List.of("rel2.dept_name"));
        // needed to rename as the exception is done on raw attributes which do have common values
        Predicate p = new PredicateImpl(2, "=", 4);
        Relation theta_join = raImpl.join(instructor_relation, rename_dept, p);
        theta_join.print();
        System.out.println(instructor_relation.getAttrs());
        System.out.println(theta_join.getAttrs());

        Relation rename_instructor = raImpl.rename(instructor_relation, List.of("dept_name"), List.of("rel1.dept_name"));
        Relation theta_join2 = raImpl.join(rename_instructor, dept_relation,p);
        theta_join2.print();
        System.out.println(instructor_relation.getAttrs());
        System.out.println(theta_join2.getAttrs());
    }

    @Test
    public void checkExceptionThetaJoin() {
        Relation course_relation = new RelationBuilder().attributeNames(List.of("course_id", "title", "dept_name", "credits")).attributeTypes(List.of(Type.STRING, Type.STRING, Type.STRING, Type.DOUBLE)).build();
        course_relation.loadData("test-tables/course.csv");
        Predicate p = new PredicateImpl(2, "=", 6);
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> raImpl.join(instructor_relation, course_relation, p));
        System.out.println(ex1.getMessage());
    }
}
