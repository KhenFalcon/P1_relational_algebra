/**
 * Copyright (c) 2025 Sami Menik, PhD. All rights reserved.
 * 
 * Unauthorized copying of this file, via any medium, is strictly prohibited.
 * This software is provided "as is," without warranty of any kind.
 */
package uga.csx370.mydbimpl;

import java.util.List;

import uga.csx370.mydb.Cell;
import uga.csx370.mydb.Predicate;
import uga.csx370.mydb.RA;
import uga.csx370.mydb.Relation;
import uga.csx370.mydb.RelationBuilder;
import uga.csx370.mydb.Type;

public class Driver {
    
    public static void main(String[] args) {
        // Following is an example of how to use the relation class.
        // This creates a table with three columns with below mentioned
        // column names and data types.
        // After creating the table, data is loaded from a CSV file.
        // Path should be replaced with a correct file path for a compatible
        // CSV file.
        
        // Relation rel1 = new RelationBuilder()
        //         .attributeNames(List.of("Col01_Name", "Col02_Name", "Col03_Name"))
        //         .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.DOUBLE))
        //         .build();
        // rel1.loadData("/path/to/exported/csv_file");
        // rel1.print();

        // Load relations used by the queries
        RA ra = new RAImpl();
        Relation students = new RelationBuilder()
            .attributeNames(List.of("ID", "name", "dept_name", "tot_cred"))
            .attributeTypes(List.of(
                Type.INTEGER, Type.STRING, Type.STRING, Type.INTEGER))
            .build();
        
        students.loadData("test-tables/student.csv");
        
        Relation takes = new RelationBuilder()
            .attributeNames(List.of(
                "ID", "course_id", "sec_id", "semester", "year", "grade"))
            .attributeTypes(List.of(
                Type.INTEGER, Type.STRING, Type.STRING,
                Type.STRING, Type.INTEGER, Type.STRING))
            .build();
        
        takes.loadData("test-tables/takes.csv");
        
        
        Relation courses = new RelationBuilder()
            .attributeNames(List.of(
                "course_id", "title", "dept_name", "credits"))
            .attributeTypes(List.of(
                Type.STRING, Type.STRING, Type.STRING, Type.INTEGER))
            .build();
        
        courses.loadData("test-tables/course.csv");

        Relation advisors = new RelationBuilder()
            .attributeNames(List.of("s_ID", "i_ID"))
            .attributeTypes(List.of(Type.INTEGER, Type.INTEGER))
            .build();

        advisors.loadData("test-tables/advisor.csv");

        Relation departments = new RelationBuilder()
            .attributeNames(List.of("dept_name", "building", "budget"))
            .attributeTypes(List.of(Type.STRING, Type.STRING, Type.DOUBLE))
            .build();

        departments.loadData("test-tables/department.csv");

        Relation teaches = new RelationBuilder()
            .attributeNames(List.of(
                "ID", "course_id", "sec_id", "semester", "year"))
            .attributeTypes(List.of(
                Type.INTEGER, Type.STRING, Type.STRING,
                Type.STRING, Type.INTEGER))
            .build();

        teaches.loadData("test-tables/teaches.csv");

        Relation sections = new RelationBuilder()
            .attributeNames(List.of(
                "course_id", "sec_id", "semester", "year",
                "building", "room_number", "time_slot_id"))
            .attributeTypes(List.of(
                Type.STRING, Type.STRING, Type.STRING, Type.INTEGER,
                Type.STRING, Type.STRING, Type.STRING))
            .build();

        sections.loadData("test-tables/section.csv");

        Relation timeSlots = new RelationBuilder()
            .attributeNames(List.of(
                "time_slot_id", "day", "start_hr", "start_min",
                "end_hr", "end_min"))
            .attributeTypes(List.of(
                Type.STRING, Type.STRING, Type.INTEGER, Type.INTEGER,
                Type.INTEGER, Type.INTEGER))
            .build();

        timeSlots.loadData("test-tables/time_slot.csv");

        Relation instructors = new RelationBuilder()
            .attributeNames(List.of("ID", "name", "dept_name", "salary"))
            .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.STRING, Type.DOUBLE))
            .build();
        instructors.loadData("test-tables/instructor.csv");
        
        

        // instructors.print();

        // Relation students = new RelationBuilder()
        //     .attributeNames(List.of("ID", "name", "dept_name", "tot_cred"))
        //     .attributeTypes(List.of(Type.INTEGER, Type.STRING, Type.STRING, Type.INTEGER))
        //     .build();
        // students.loadData("test-tables/student.csv");
        // System.out.println("\nMyID: jcm68203 : 811281498");
        // students.print(); // warning: a lot of data will be printed to the console

        // Sasha's Query
        System.out.println(
            "\nQuery: Find the names of students who earned an A " +
            "in the Computer Science course \"International Practicum\"."
        );
        Relation renamedStudents = ra.rename(
            students,
            List.of("dept_name"),
            List.of("student_dept")
        );
        Relation studentTakes = ra.join(renamedStudents, takes);

        Relation studentTakesCourses = ra.join(studentTakes, courses);

        Predicate csPredicate = new PredicateImpl(
            studentTakesCourses.getAttrIndex("dept_name"),
            "=",
            Cell.val("Comp. Sci.")
        );
        
        Relation csCourses = ra.select(
            studentTakesCourses,
            csPredicate
        );
        
        Predicate coursePredicate = new PredicateImpl(
            csCourses.getAttrIndex("title"),
            "=",
            Cell.val("International Practicum")
        );
        
        Relation practicum = ra.select(
            csCourses,
            coursePredicate
        );

        Predicate gradePredicate = new PredicateImpl(
            practicum.getAttrIndex("grade"),
            "=",
            Cell.val("A ")
        );
        
        Relation gradeA = ra.select(
            practicum,
            gradePredicate
        );
        
        Relation result = ra.project(
            gradeA,
            List.of("name")
        );
        
        result.print();
        //Sasha query ends here

        // Joshua's Query
        System.out.println(
            "\nQuery: Find the IDs and names of all students " +
            "that are advised by the instructor named \"Mird\"."
        );

        Relation renamedInstructors = ra.rename(
            instructors,
            List.of("ID", "name", "dept_name"),
            List.of("i_ID", "advisor_name", "instructor_dept")
        );

        Predicate mirdPredicate = new PredicateImpl(
            renamedInstructors.getAttrIndex("advisor_name"),
            "=",
            Cell.val("Mird")
        );

        Relation mirdInstructor = ra.select(
            renamedInstructors,
            mirdPredicate
        );

        Relation mirdAdvisors = ra.join(
            mirdInstructor,
            advisors
        );

        Relation renamedStudentsForAdvisor = ra.rename(
            students,
            List.of("ID", "dept_name"),
            List.of("s_ID", "student_dept")
        );

        Relation advisedStudents = ra.join(
            mirdAdvisors,
            renamedStudentsForAdvisor
        );

        Relation advisorResult = ra.project(
            advisedStudents,
            List.of("s_ID", "name")
        );

        advisorResult.print();
        // Joshua's query ends here

        // Adiva's Query
        System.out.println(
            "\nQuery: Find instructors teaching advanced courses (4+ credits), " +
            "along with the course title, credits, and department name."
        );

        Relation renamedInstructorsAdvanced = ra.rename(
            instructors,
            List.of("ID", "name"),
            List.of("instructor_ID", "instructor_name")
        );

        Relation renamedTeachesAdvanced = ra.rename(
            teaches,
            List.of("ID"),
            List.of("instructor_ID")
        );

        Relation instructorTeaches = ra.join(
            renamedInstructorsAdvanced,
            renamedTeachesAdvanced
        );

        Relation renamedCoursesAdvanced = ra.rename(
            courses,
            List.of("dept_name"),
            List.of("course_dept")
        );

        Relation instructorCourses = ra.join(
            instructorTeaches,
            renamedCoursesAdvanced
        );

        Relation instructorCourseDepartments = ra.join(
            instructorCourses,
            departments
        );

        Predicate advancedCoursePredicate = new PredicateImpl(
            instructorCourseDepartments.getAttrIndex("credits"),
            ">=",
            Cell.val(4)
        );

        Relation advancedCourses = ra.select(
            instructorCourseDepartments,
            advancedCoursePredicate
        );

        Relation advancedCourseResult = ra.project(
            advancedCourses,
            List.of("instructor_name", "title", "credits", "dept_name")
        );

        advancedCourseResult.print();
        // Adiva's query ends

        // Amy's query
        System.out.println(
            "\nQuery: Find professors who have taught an 8 AM course."
        );

        Relation renamedInstructors8AM = ra.rename(
            instructors,
            List.of("ID"),
            List.of("instructor_ID")
        );

        Relation renamedTeaches8AM = ra.rename(
            teaches,
            List.of("ID"),
            List.of("instructor_ID")
        );

        Relation instructorTeaches8AM = ra.join(
            renamedInstructors8AM,
            renamedTeaches8AM
        );

        Relation instructorSections8AM = ra.join(
            instructorTeaches8AM,
            sections
        );

        Relation instructorTimeSlots8AM = ra.join(
            instructorSections8AM,
            timeSlots
        );

        Predicate eightAMPredicate = new PredicateImpl(
            instructorTimeSlots8AM.getAttrIndex("start_hr"),
            "=",
            Cell.val(8)
        );

        Relation eightAMInstructors = ra.select(
            instructorTimeSlots8AM,
            eightAMPredicate
        );

        Relation eightAMResult = ra.project(
            eightAMInstructors,
            List.of("name", "dept_name", "salary")
        );

        eightAMResult.print();
        // Amy's Query ends here

        // Mia's Query
        System.out.println(
            "\nQuery: Find the names of instructors who advise students " +
            "in the Computer Science department."
        );

        Relation renamedInstructorsCSAdvisor = ra.rename(
            instructors,
            List.of("ID", "name", "dept_name"),
            List.of("i_ID", "instructor_name", "instructor_dept")
        );

        Relation instructorAdvisorsCS = ra.join(
            renamedInstructorsCSAdvisor,
            advisors
        );

        Relation renamedStudentsCSAdvisor = ra.rename(
            students,
            List.of("ID"),
            List.of("s_ID")
        );

        Relation instructorsAndStudentsCS = ra.join(
            instructorAdvisorsCS,
            renamedStudentsCSAdvisor
        );

        Predicate csStudentPredicate = new PredicateImpl(
            instructorsAndStudentsCS.getAttrIndex("dept_name"),
            "=",
            Cell.val("Comp. Sci.")
        );

        Relation csAdvisedStudents = ra.select(
            instructorsAndStudentsCS,
            csStudentPredicate
        );

        Relation csAdvisorResult = ra.project(
            csAdvisedStudents,
            List.of("instructor_name")
        );

        csAdvisorResult.print();
        // Mia's Query ends here
    }

}
