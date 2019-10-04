package vn.topica.itlab4.schoolmanagement;

import vn.topica.itlab4.schoolmanagement.entities.StudentSubjectRegister;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * This class contains executions to show the reports.
 *
 * @author AnhLT14 (anhlt14@topica.edu.vn)
 */
public class Execute {
    public static void main(String[] args) {
        Database db = new Database();
        db.loadFullDb();

        /**
         * Exercise 1: Get the number of students of each school
         */
        System.out.println("Exercise 1:");
        db.listSchool.forEach(school -> {
            System.out.println("---School : " + school.getSchoolName());
            int sum = db.listClass.stream()
                    .filter(classOfSchool -> classOfSchool.getSchoolId() == school.getSchoolId())
                    .mapToInt(c -> (int) db.listStudent.stream()
                            .filter(student -> student.getClassId() == c.getClassId())
                            .count())
                    .sum();
            System.out.println("Number of students: " + sum);
        });
        System.out.println("------------------------------");

        /**
         * Exercise 2: Calculate the average score of each subject
         */
        System.out.println("Exercise 2:");
        db.listSchool.forEach(school -> {
            System.out.println("---School : " + school.getSchoolName());
            db.listSubject.forEach(subject -> {
                double avg = db.listClass.stream().filter(
                        classOfSchool -> classOfSchool.getSchoolId() == school.getSchoolId())
                        .mapToDouble(c -> db.listStudent.stream().filter(
                                student -> c.getClassId() == student.getClassId())
                                .mapToDouble(student -> db.listStudentSubjectRegister
                                        .stream()
                                        .filter(ssr -> ssr
                                                .getStudentId() == student
                                                .getStudentId()
                                                && ssr.getSubjectId() == subject
                                                .getSubjectId())
                                        .mapToDouble(StudentSubjectRegister::getScore)
                                        .summaryStatistics().getAverage())
                                .filter(value -> value != 0).summaryStatistics()
                                .getAverage())
                        .filter(value -> value != 0).summaryStatistics().getAverage();
                if (avg != 0) {
                    System.out.println("Subject : " + subject.getSubjectName() + " - Avg = " + avg);
                }
            });
        });
        System.out.println("------------------------------");

        /**
         * Exercise 3: Show the class with the highest average score in each school
         */
        System.out.println("Exercise 3:");
        db.listSchool.forEach(school -> {
            System.out.println("---School : " + school.getSchoolName());
            db.listClass.stream()
                    .filter(classOfSchool -> classOfSchool.getSchoolId() == school.getSchoolId())
                    .forEach(classOfSchool -> {
                        double avg = db.listStudent.stream().filter(
                                student -> student.getClassId() == classOfSchool.getClassId())
                                .mapToDouble(student -> db.listStudentSubjectRegister.stream()
                                        .filter(ssr -> ssr.getStudentId() == student.getStudentId())
                                        .mapToDouble(StudentSubjectRegister::getScore)
                                        .summaryStatistics().getAverage())
                                .filter(value -> value != 0).summaryStatistics()
                                .getAverage();
                        if (avg > school.max) {
                            school.max = avg;
                            school.classCode = classOfSchool.getClassCode();
                        }
                    });
            System.out.println("Class : " + school.classCode + " - Avg = " + school.max);
        });
        System.out.println("------------------------------");

        /**
         * Exercise 4: Show the best and worst 10 students with each subject
         */
        System.out.println("Exercise 4:");
        db.listSubject.forEach(subject -> {
            System.out.println("-------" + subject.getSubjectName());
            System.out.println("---10 worst students: ");
            db.listStudentSubjectRegister.stream()
                    .filter(ssr -> subject.getSubjectId() == ssr.getSubjectId())
                    .sorted(Comparator.comparingDouble(StudentSubjectRegister::getScore))
                    .limit(10).collect(Collectors.toList())
                    .forEach(ssr -> db.listStudent.stream()
                            .filter(student -> student.getStudentId() == ssr.getStudentId())
                            .limit(1)
                            .forEach(student -> System.out.println(student.getStudentName() + " - Score = " + ssr.getScore())));
            System.out.println("~~~~~~~");
            System.out.println("---10 best students: ");
            db.listStudentSubjectRegister.stream()
                    .filter(ssr -> subject.getSubjectId() == ssr.getSubjectId())
                    .sorted(Comparator.comparingDouble(StudentSubjectRegister::getScore).reversed())
                    .limit(10).collect(Collectors.toList())
                    .forEach(ssr -> db.listStudent.stream()
                            .filter(student -> student.getStudentId() == ssr.getStudentId())
                            .limit(1)
                            .forEach(student -> System.out.println(student.getStudentName() + " - Score = " + ssr.getScore())));
        });
        System.out.println("-------------------------------------");

        /**
         * Exercise 5: Get the class whose average score is highest, group by SubjectDomain
         */
        System.out.println("Exercise 5:");
        db.listSubjectDomain.forEach(sd -> {
            System.out.println("-----" + sd);
            double max = db.listClass.stream()
                    .mapToDouble(classOfSchool -> {
                        double avg = db.listStudent.stream()
                                .filter(student -> student.getClassId() == classOfSchool.getClassId())
                                .mapToDouble(student -> db.listSubject.stream()
                                        .filter(subject -> subject.getDomain() == sd)
                                        .mapToDouble(subject -> db.listStudentSubjectRegister.stream()
                                                .filter(ssr -> ssr.getStudentId() == student.getStudentId() && ssr.getSubjectId() == subject.getSubjectId())
                                                .mapToDouble(StudentSubjectRegister::getScore)
                                                .summaryStatistics().getAverage())
                                        .filter(value -> value != 0).summaryStatistics().getAverage())
                                .filter(value -> value != 0).summaryStatistics().getAverage();
                        return avg;
                    }).max().orElse(-1);
            System.out.println("Domain : " + sd + " - Max Avg = " + max);
        });
        System.out.println("-------------------------------------");

        /**
         * Exercise 6: Get the domain whose average score is highest in group by school.
         *             Get the domain whose number of students registered is the most
         */
        System.out.println("Exercise 6:");
        db.listSchool.forEach(school -> {
            System.out.println("---School : " + school.getSchoolName());
            db.listSubjectDomain.forEach(sd -> {
                double max = db.listClass.stream()
                        .filter(classOfSchool -> classOfSchool.getSchoolId() == school.getSchoolId())
                        .mapToDouble(classOfSchool -> db.listStudent.stream()
                                .filter(student -> student.getClassId() == classOfSchool.getClassId())
                                .mapToDouble(student -> db.listSubject.stream()
                                        .filter(subject -> subject.getDomain() == sd)
                                        .mapToDouble(subject -> db.listStudentSubjectRegister.stream()
                                                .filter(ssr -> ssr.getSubjectId() == subject.getSubjectId() && student.getStudentId() == ssr.getStudentId())
                                                .mapToDouble(StudentSubjectRegister::getScore)
                                                .summaryStatistics().getMax())
                                        .max().orElse(-1))
                                .max().orElse(-1))
                        .max().orElse(-1);
                if (max > 0) {
                    System.out.println("Domain : " + sd + " - Max Score = " + max);
                }
            });
        });

        System.out.println("~~~~~~~~~~~~~~~~~~~~");
        int max = db.listSubjectDomain.stream()
                .mapToInt(sd -> {
                    int sum = db.listSubject.stream()
                            .filter(subject -> subject.getDomain() == sd)
                            .mapToInt(subject -> (int) db.listStudentSubjectRegister.stream()
                                    .filter(ssr -> ssr.getSubjectId() == subject.getSubjectId())
                                    .count()).sum();
                    return sum;
                }).max().orElse(-1);
        System.out.println("Domain with the most students registered: " + max);
    }
}