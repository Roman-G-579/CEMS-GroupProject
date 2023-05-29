package entity;

import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.Observable;

public class Test extends Observable implements Serializable {
    private String id;
    private final String testNumber;
    private final String author;
    private String testDuration;
    private final String subject;
    private final String courseName;
    private String teacherComments;
    private String studentComments;
    private TestTypeEnum testType;
    private String year;
    private String semester;
    private String session;
    private ObservableList<TestQuestion> questions;

    //constructor for fully created test
    public Test(String testNumber, String id, String author, String testDuration, String courseName,
                String teacherComments, TestTypeEnum TestType, String studentComments, String subject,
                String year, String session, String semester) {
        this.id = id;
        this.testNumber = testNumber;
        this.author = author;
        this.testDuration = testDuration;
        this.courseName = courseName;
        this.teacherComments = teacherComments;
        this.testType = TestType;
        this.studentComments = studentComments;
        this.subject = subject;
        this.year = year;
        this.session= session;
        this.semester = semester;
    }
    public void setStudentComments(String studentComments) {
        this.studentComments = studentComments;
    }
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }



    public ObservableList<TestQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(ObservableList<TestQuestion> questions) {
        this.questions = questions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestNumber() {
        return testNumber;
    }


    public String getAuthor() {
        return author;
    }


    public String getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(String testDuration) {
        this.testDuration = testDuration;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTeacherComments() {
        return teacherComments;
    }

    public void setTeacherComments(String teacherComments) {
        this.teacherComments = teacherComments;
    }

    public TestTypeEnum getTestType() {
        return testType;
    }

    public void setTestType(TestTypeEnum testType) {
        this.testType = testType;
    }

    public String getStudentComments() {
        return studentComments;
    }

    public void setStudentComment(String studentComment) {
        this.studentComments = studentComment;
    }

    public String getSubject() {
        return subject;
    }

}