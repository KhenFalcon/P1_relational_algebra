/**
 * Copyright (c) 2025 Sami Menik, PhD. All rights reserved.
 * 
 * Unauthorized copying of this file, via any medium, is strictly prohibited.
 * This software is provided "as is," without warranty of any kind.
 */
package uga.csx370.mydbimpl;

import java.util.List;

import uga.csx370.mydb.Relation;
import uga.csx370.mydb.RelationBuilder;
import uga.csx370.mydb.Type;

// Sasha import
import uga.csx370.mydb.Cell;
import uga.csx370.mydb.Predicate;
import uga.csx370.mydb.RA;

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

        // Sasha Add
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

        // Original
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
            10,
            "=",
            Cell.val("Comp. Sci.")
        );
        
        Relation csCourses = ra.select(
            studentTakesCourses,
            csPredicate
        );
        
        Predicate coursePredicate = new PredicateImpl(
            9,
            "=",
            Cell.val("International Practicum")
        );
        
        Relation practicum = ra.select(
            csCourses,
            coursePredicate
        );
        
        Predicate gradePredicate = new PredicateImpl(
            8,
            "=",
            Cell.val("A")
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
    }

}
