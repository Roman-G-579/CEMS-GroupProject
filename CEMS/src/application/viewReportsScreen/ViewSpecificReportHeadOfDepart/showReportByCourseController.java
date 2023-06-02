package application.viewReportsScreen.ViewSpecificReportHeadOfDepart;

import client.Client;
import client.ClientUI;
import common.MsgHandler;
import common.TypeMsg;
import entity.Course;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import util.ExitButton;
import util.MinimizeButton;
import util.PathConstants;
import util.ScreenManager;

import java.util.List;

public class showReportByCourseController {
    @FXML
    private Text usernameText;

    @FXML
    private AnchorPane header;
    @FXML
    private ComboBox<String> CourseCombo;
    ObservableList<Course> coursesList;

    private void createCourseCombo(String username) {
        MsgHandler getCourses = new MsgHandler(TypeMsg.importCourses, username);
        ClientUI.chat.accept(getCourses);
        List<Object> courseObjectsList = ClientUI.chat.getCourses();
        coursesList = FXCollections.observableArrayList((List) courseObjectsList);
        ObservableList<String> courseNames = FXCollections.observableArrayList();

        for (Course course : coursesList) {
            String courseName = course.getCourseName();
            courseNames.add(courseName);
        }
        CourseCombo.setItems(courseNames);
    }

    public void initialize() {
        ScreenManager.dragAndDrop(header);
        usernameText.setText(Client.user.getFullName());
        createCourseCombo(Client.user.getUserName());
    }

    @FXML
    void LogOut(ActionEvent event) {
        ScreenManager.goToNewScreen(event, PathConstants.loginPath);
    }


    @FXML
    void closeClient(ActionEvent event) {
        ExitButton.closeClient(event);

    }

    @FXML
    public void goBackToPreviousScreen(ActionEvent event) {
        ScreenManager.goToNewScreen(event, PathConstants.ViewReportHeadOfDepartmentPath);
    }

    @FXML
    public void minimizeWindow(ActionEvent event) {
        MinimizeButton.minimizeWindow(event);
    }

    @FXML
    void showReportForSpecificCourse(ActionEvent event) {
        String chosenCourse = CourseCombo.getSelectionModel().getSelectedItem().toString();
        MsgHandler getData = new MsgHandler(TypeMsg.GetTestsByCourse,chosenCourse);
        ClientUI.chat.accept(getData);
        ScreenManager.goToNewScreen(event, PathConstants.EnterReportByCoursePath);
    }

}
