package application;

import java.io.Serializable;
import java.util.Observable;

public class Question extends Observable implements Serializable {
    private String id;
    private String question_number;
    private String subject;
    private String course_name;
    private String question_text;
    private String lecturer;



    public Question(String questionNumber,String questionId,String questionText, String questionWrittenBy,String subject,String course_name) {
        this.question_number = questionNumber;
        this.id = questionId;
        this.question_text = questionText;
        this.lecturer = questionWrittenBy;
        this.course_name=course_name;
        this.subject=subject;
    }



    public String getId() {
        return id;
    }


    public String getQuestion_number() {
        return question_number;
    }

    public String getQuestion_text() {
        return question_text;
    }

    public String getLecturer() {
        return lecturer;
    }

    public String getCourse_name() {
        return course_name;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "MyClass{" +
                "id='" + id + '\'' +
                ", question_number='" + question_number + '\'' +
                ", subject='" + subject + '\'' +
                ", course_name='" + course_name + '\'' +
                ", question_text='" + question_text + '\'' +
                ", lecturer='" + lecturer + '\'' +
                '}';
    }
}
