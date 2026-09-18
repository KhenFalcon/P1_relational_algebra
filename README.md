# Project 1: Relational Algebra

## Authors: (Team 25)
- Amy Vu
- Mia Martinez
- Joshua Mark (jcm68203)
- Sasha Park
- Adiva Parisa

## Description:
This is Team 25's submission for group project 1 for the class CSCI 4370 - Databse Management. 

This project is meant to implement the RAImpl class and demonstrate understanding of basic relational algebra that will later be used in application of larger SQL databases. For simplicity and to avoid using external data, two testing .csv table files have been provided (copied from previous activites and taken from the uni_in_class database) in the `test-tables` directory.

This Project's GitHub Repository: https://github.com/KhenFalcon/P1_relational_algebra

> [!NOTE] Disclaimer for Testing:
> The RA interface specifically instructs for an exception to be thrown when combining relations with matching attribute names. This specifically (though not exclusively) arises when using the cartesianProduct() and join() methods, which are originally designed to handle this case. To abide by the given instructions, the aforementioned methods (implemented in `RAImpl.java`) do not have the capability of reconciling duplicate attribute names. Please keep this in mind when using/testing these methods.

## Team Members' Contributions

### Amy Vu
- Implemented union() and intersect() methods in `RAImpl.java`
- Created private method, checkCompatable(), in `RAImpl.java`
- Created testUnion() and testIntersect() testing methods in `RAImplTest.java`
- Query: "Professors who have taught an 8am course"
### Mia Martiez
- Implemented rename(), cartesianProduct(), join(), and join(predicate) methods in `RAImpl.java`
- Expanded the `PredicateImpl.java` class to include column-to-column dynamic comparison.
- Created testRename(), testCartesianProduct(), testNaturalJoin(), createCourseTable(), createStudentTable(), checkExceptionRename(), checkExceptionCartesianProduct(), and checkExxceptionThetaJoin() testing methods in `RAImplTest.java`
- Query: "The names of the Instructors who advise students that are in the computer science department"
### Joshua Mark
- Parital Contribution to implementing `Driver.java`
- Implemented the select() and project() methods in `RAImpl.java`
- Created and copied the testing .csv files to the `test-tables` directory
- Created the `RAImplTest.java` file to test the method implementations in `RAImpl.java`.
- Created setup(), contextLoads(), validInstallation(), validInstructorRelation(), testSelect(), testProject(), testDiff(), and testExceptionDiff() testing methods in `RAImplTest.java` 
- Created the `PredicateImpl.java` file as a substitute implementation of the `Predicate.java` interface. It was created in the process of creating the `RAImplTest.java` tests.
- Created the GroupMe and GitHub repository for group coordination.
- Query: "The name and IDs of students who are advised by the Instructor, Mird"
### Sasha Park
- Implemented the team's queries in `Driver.java`
- Query: "The name of Students who earned an A in the course 'International Practicum'"
### Adiva Parisa
- Implemented the diff() method in `RAImpl.java`
- Query: "The instructor name, instructor department, course title, and course credits of courses with 4 credits or more" 

### Provided Files:
- all files in the `src/main/.../mydb` directory
- `src/test/.../AppTest.java`
- `src/main/.../Driver.java`