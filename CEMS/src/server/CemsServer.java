package server;

import client.Client;
import common.ConnectToClients;
import common.MsgHandler;
import common.TypeMsg;
import entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CemsServer extends AbstractServer {
    // This holds the list of the connected clients to the server and their status
    static ObservableList<ConnectToClients> clientList = FXCollections.observableArrayList();
    //Constructors ****************************************************
    public String passwordSQL;
    Object obj;
    MsgHandler<Object> msg;
    Question question;
    Test test;
    ActiveTest activeTest;
    TestQuestion testQuestion;
    TestForApproval testApprove;
    public static MsgHandler<Object> messageFromServerToAll;

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public CemsServer(int port, String passwordSQL) {
        super(port);
        this.passwordSQL = passwordSQL;
    }

    /**
     * This method updates the Connected\Disconnect clients to our Server Each time
     * a user Connect\disconnects we update the Connect Users in our Server GUI if a
     * user connects his status is "Connected" and if he disconnects status changes
     * to "Disconnected"
     *
     * @param client
     * @param connectionStatus
     */
    static void updateClientList(ConnectionToClient client, String connectionStatus) {
        for (int i = 0; i < clientList.size(); i++) {
            /* Comparing clients by IP addresses */
            try {
                if (clientList.get(i).getIp().equals(client.getInetAddress().getHostAddress())) {
                    clientList.get(i).setStatus(connectionStatus);
                    clientList.remove(i);
                }
            } catch (NullPointerException ex) {
                System.out.println("Error! Client not found");
            }
        }

        //In both cases of Connect and Disconnected, we will need to add Client into the
        //list so this function covers both of them simultaneously
        try {
            clientList.add(new ConnectToClients(client.getInetAddress().getHostAddress(),
                    client.getInetAddress().getHostName(), connectionStatus));
        } catch (NullPointerException ex) {
            System.out.println("Error! Client not found");
        }
    }

    /**
     * gets a list of clients connected to the server
     *
     * @return an observable list of the connected clients
     */
    public static ObservableList<ConnectToClients> getClientList() {
        return clientList;
    }

    public static void setClientList(ObservableList<ConnectToClients> clientList) {
        CemsServer.clientList = clientList;
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server starts listening for connections.
     */
    protected void serverStarted() {
        System.out.println("Server Started\n" + "Server listening for connections on port " + getPort());

        try {
            MysqlConnection.connectToDb(passwordSQL);
        } catch (Exception ex) {
            System.out.println("Error! Connection Failed");
        }
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server stops listening for connections.
     */
    protected void serverStopped() {
        try {
            super.close();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        MysqlConnection.closeConnection();

    }

    @Override
    public void sendToAllClients(Object msg) {
        Thread[] clientThreadList = getClientConnections();
        messageFromServerToAll = (MsgHandler<Object>) msg;
        System.out.println("handleMessageFromServer ");
        System.out.println(messageFromServerToAll.getType().toString());
        switch (messageFromServerToAll.getType()) {
            case RequestIsDeclined:
                this.msg = (MsgHandler<Object>) msg;
                this.obj = (String) this.msg.getMsg();
                for (int i = 0; i < clientThreadList.length; i++) {
                    ConnectionToClient client = (ConnectionToClient) clientThreadList[i];
                    String name = client.getName();
                    if (name.equals(obj)) {
                        try {
                            client.sendToClient(new MsgHandler<>(TypeMsg.RequestIsDeclinedToLecturer, "Time Request Declined"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
                break;
            case RequestIsApproved:
                this.msg = (MsgHandler<Object>) msg;
                this.obj = (String) this.msg.getMsg();
                for (int i = 0; i < clientThreadList.length; i++) {
                    ConnectionToClient client = (ConnectionToClient) clientThreadList[i];
                    String name = client.getName();
                    if (name.equals(obj)) {
                        try {
                            client.sendToClient(new MsgHandler<>(TypeMsg.TestDurationApprovedPopLecturer, "Time Request Approved"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                break;
            case TestDurationChanged:
                this.msg = (MsgHandler<Object>) msg;
                this.obj = this.msg.getMsg();
                if (obj instanceof Integer) {
                    for (int i = 0; i < clientThreadList.length; i++) {
                        ConnectionToClient client = (ConnectionToClient) clientThreadList[i];
                        String name = (String) client.getInfo(client.getName());
                        if (name.equals("Student")) {
                            try {
                                client.sendToClient(new MsgHandler<>(TypeMsg.TestDurationChangedManual, obj));
                                client.sendToClient(new MsgHandler<>(TypeMsg.TestDurationChangedComputerized, obj));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

                break;
            case StudentsTestIsApprvoedToAllClients:
                this.msg = (MsgHandler<Object>) msg;
                this.obj = this.msg.getMsg();
                for (int i = 0; i < clientThreadList.length; i++) {
                    ConnectionToClient client = (ConnectionToClient) clientThreadList[i];
                    String name = client.getName();
                    if (name.equals(obj)) {
                        try {
                            client.sendToClient(new MsgHandler<>(TypeMsg.PopupTestApprove, "Test is Approved"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                break;
            case LockTestForStudentByLecturer:
                for (int i = 0; i < clientThreadList.length; i++) {
                    ConnectionToClient client = (ConnectionToClient) clientThreadList[i];
                    String name = (String) client.getInfo(client.getName());
                    if (name.equals("Student")) {
                        try {
                            client.sendToClient(new MsgHandler<>(TypeMsg.TestIsForcedLocked, null));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }


        }

    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        MsgHandler<Object> messageFromClient = (MsgHandler<Object>) msg;
        System.out.println("Message received from " + client);
        try {
            switch (messageFromClient.getType()) {
                case Connected:
                    updateClientList(client, "Connected");
                    client.sendToClient(new MsgHandler(TypeMsg.Connected, null));
                    break;

                case Disconnected:
                    updateClientList(client, "Disconnected");
                    client.sendToClient(new MsgHandler<>(TypeMsg.Disconnected, null));
                    break;

                case GetQuestionsBySubject:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<Question> list = MysqlConnection.getQuestionsTable("SELECT obj.* " +
                            "FROM question obj " +
                            "JOIN lecturersubject ls ON obj.subject = ls.subjectID " +
                            "JOIN user u ON u.id = ls.id " +
                            "WHERE u.username = + '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.QuestionsBySubjectImported, list));
                    break;

                case EditQuestion:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    if (obj instanceof Question) {
                        this.question = (Question) obj;
                        String updateQuery = "UPDATE cems.question SET questionNumber='" + question.getQuestionNumber() + "'," +
                                " subject='" + question.getSubject() + "'," +
                                " courseName='" + question.getCourseName() + "'," +
                                " questionText='" + question.getQuestionText() + "'," +
                                " author='" + question.getAuthor() + "'," +
                                " answer1='" + question.getAnswer1() + "'," +
                                " answer2='" + question.getAnswer2() + "'," +
                                " answer3='" + question.getAnswer3() + "'," +
                                " correctAnswer='" + question.getCorrectAnswer() + "'," +
                                " answer4='" + question.getAnswer4() + "'" +
                                " WHERE id='" + question.getId() + "'";

                        MysqlConnection.update(updateQuery);
                        client.sendToClient(new MsgHandler<>(TypeMsg.QuestionUpdated, null));
                    }
                    break;

                case DeleteQuestion:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (Question) this.msg.getMsg();
                    this.question = (Question) obj;
                    String DeleteQuestionQuery = "DELETE FROM cems.question WHERE id='" + question.getId() + "'";
                    MysqlConnection.update(DeleteQuestionQuery);
                    client.sendToClient(new MsgHandler<>(TypeMsg.QuestionDeleted, null));
                    break;

                case TryLogin:
                    this.msg = (MsgHandler<Object>) msg;
                    List<Object> details = (List<Object>) this.msg.getMsg();
                    String Username = (String) details.get(0);
                    String Password = (String) details.get(1);
                    Object user = MysqlConnection.authenticateUser(Username, Password);
                    if (user instanceof User) {
                        String fullname = ((User) user).getFullName();
                        client.setName(fullname);
                        client.setInfo(client.getName(), ((User) user).getRole());
                    }
                    client.sendToClient(new MsgHandler<>(TypeMsg.LoginResponse, user));
                    break;

                case importSubjects:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    ArrayList<Subject> importSubjects = MysqlConnection.getSubjectList("SELECT * " +
                            "FROM subject s " +
                            "JOIN lecturersubject ls ON s.subjectID = ls.subjectID " +
                            "JOIN user u ON u.id = ls.id " +
                            "WHERE u.username = '" + obj.toString() + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.SubjectsimportSuccess, importSubjects));
                    break;

                case importCourses:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    ArrayList<Course> importCourses = MysqlConnection.getCourseList("SELECT * " +
                            "FROM course AS c " +
                            "JOIN lecturersubject AS ls ON c.subjectID = ls.subjectID " +
                            "JOIN user AS u ON ls.id = u.id " +
                            "WHERE u.username = '" + obj.toString() + "'");

                    client.sendToClient(new MsgHandler<>(TypeMsg.CoursesimportSuccess, importCourses));
                    break;

                case GetAllQuestions:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<Question> allQuestions = MysqlConnection.getQuestionsTable("SELECT * FROM question");
                    client.sendToClient(new MsgHandler<>(TypeMsg.allQuestionImported, allQuestions));
                    break;

                case AddNewQuestion:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (Question) this.msg.getMsg();
                    Question question = (Question) obj;
                    try {
                        String newQuery = "INSERT INTO cems.question (id, subject, courseName, questionText, questionNumber, author, answer1, answer2, answer3, correctAnswer, answer4) " +
                                "VALUES ('" + question.getId() + "', '" +
                                question.getSubject() + "', '" +
                                question.getCourseName() + "', '" +
                                question.getQuestionText() + "', '" +
                                question.getQuestionNumber() + "', '" +
                                question.getAuthor() + "', '" +
                                question.getAnswer1() + "', '" +
                                question.getAnswer2() + "', '" +
                                question.getAnswer3() + "', '" +
                                question.getCorrectAnswer() + "', '" +
                                question.getAnswer4() + "')";
                        MysqlConnection.update(newQuery);
                    } catch (Exception ignored) {
                    }
                    client.sendToClient(new MsgHandler<>(TypeMsg.QuestionAddedSuccessfuly, null));
                    break;

                case GetCourseTable:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();

                    ArrayList<Course> courseList = MysqlConnection.getCourseList("SELECT * FROM cems.course WHERE subjectID = " +
                            "(SELECT subjectid FROM cems.lecturersubject WHERE id = " + obj + ")");
                    ;
                    client.sendToClient(new MsgHandler<>(TypeMsg.CourseTableResponse, courseList));
                    break;

                case GetAllTestsTable:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();

                    ArrayList<Test> allTestsList = MysqlConnection.getTestTable("select * from cems.test;");
                    client.sendToClient(new MsgHandler<>(TypeMsg.GetAllTestsTableResponse, allTestsList));
                    break;

                case GetTestsBySubject:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();

                    ArrayList<Test> testsBySubjectList = MysqlConnection.getTestTable(
                            "SELECT t.* " +
                                    "FROM cems.test AS t " +
                                    "JOIN lecturersubject ls ON t.subject = (SELECT subjectName FROM subject WHERE subjectID = ls.subjectid) " +
                                    "JOIN user u ON u.id = ls.id " +
                                    "WHERE u.username =  + '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.GetTestsBySubjectResponse, testsBySubjectList));

                    break;

                case AddNewTest:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    if (obj instanceof Test) {
                        this.test = (Test) obj;
                        String newQuery = "INSERT INTO cems.test (testNumber, id, testDuration, author, subject, courseName, " +
                                "teacherComment, testType, studentComment, semester, year, session, testCode) " +
                                "VALUES ('" + test.getTestNumber() + "', " +
                                "'" + test.getId() + "', " +
                                "'" + test.getTestDuration() + "', " +
                                "'" + test.getAuthor() + "', " +
                                "'" + test.getSubject() + "', " +
                                "'" + test.getCourseName() + "', " +
                                "'" + test.getTeacherComments() + "', " +
                                "'" + test.getTestType() + "', " +
                                "'" + test.getStudentComments() + "', " +
                                "'" + test.getSemester() + "', " +
                                "'" + test.getYear() + "', " +
                                "'" + test.getSession() + "', " +
                                "'" + test.getTestCode() + "') ";
                        MysqlConnection.update(newQuery);
                        client.sendToClient(new MsgHandler<>(TypeMsg.AddNewTestResponse, null));
                    }
                    break;

                case AddNewTestQuestion:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    if (obj instanceof TestQuestion) {
                        this.testQuestion = (TestQuestion) obj;
                        String newQuery = "INSERT INTO cems.testquestion (questionID, questionNumber, points, " +
                                "questionText, testID, courseName, subject, author) " +
                                "VALUES ('" + testQuestion.getQuestionID() + "', " +
                                "'" + testQuestion.getQuestionNumber() + "', " +
                                "'" + testQuestion.getPoints() + "', " +
                                "'" + testQuestion.getQuestionText() + "', " +
                                "'" + testQuestion.getTestID() + "', " +
                                "'" + testQuestion.getCourseName() + "', " +
                                "'" + testQuestion.getSubject() + "', " +
                                "'" + testQuestion.getAuthor() + "') ";
                        MysqlConnection.update(newQuery);
                        client.sendToClient(new MsgHandler<>(TypeMsg.AddNewTestQuestionsResponse, null));
                        break;
                    }
                case GetRequestsBySubject:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<TestRequestForApproval> listOfRequests = MysqlConnection.getRequestsTable("SELECT tr.* " +
                            "FROM testrequest AS tr " +
                            "JOIN lecturersubject AS ls ON tr.subject = ls.subjectid " +
                            "JOIN user AS u ON ls.id = u.id " +
                            "WHERE u.username =  + '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.RequestImportedSuccessfully, listOfRequests));
                    break;
                case ApproveRequestByHeadOfDepartment:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    sendToAllClients(new MsgHandler<>(TypeMsg.RequestIsApproved, obj));
                    client.sendToClient(new MsgHandler<>(TypeMsg.RequestIsApproved, null));
                    break;
                case changeTestDuration:
                    this.msg = (MsgHandler<Object>) msg;
                    List<Object> TestChangement = (List<Object>) this.msg.getMsg();
                    String testID = (String) TestChangement.get(0);
                    String addedTime = (String) TestChangement.get(1);
                    MysqlConnection.update("UPDATE test SET testDuration = testDuration + '" + Integer.parseInt(addedTime) + "' WHERE id = '" + testID + "'");
                    sendToAllClients(new MsgHandler<>(TypeMsg.TestDurationChanged, Integer.parseInt(addedTime)));
                    client.sendToClient(new MsgHandler<>(TypeMsg.changeTestDurationAnswer, null));
                    break;
                case DeleteRequest:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    String deleteRequest = "DELETE FROM cems.testrequest WHERE id='" + obj + "'";
                    MysqlConnection.update(deleteRequest);
                    client.sendToClient(new MsgHandler<>(TypeMsg.DeleteRequestCompleted, null));
                    break;
                case DeclineRequestByHeadOfDepartment:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    sendToAllClients(new MsgHandler<>(TypeMsg.RequestIsDeclined, obj));
                    client.sendToClient(new MsgHandler<>(TypeMsg.RequestIsDeclined, null));
                    break;
                case GetStudentReport:
                    this.msg = (MsgHandler<Object>) msg;
                    Object messageData = this.msg.getMsg();
                    if (messageData instanceof List<?>) {
                        List<Object> information = (List<Object>) messageData;
                        String studentsID = information.get(0).toString();
                        String headID = information.get(1).toString();

                        ArrayList<StudentTest> studentInfo = MysqlConnection.getStudentInfo(
                                "SELECT st.* " +
                                        "FROM studentstest st " +
                                        "JOIN lecturersubject ls ON st.subjectID = ls.subjectID " +
                                        "JOIN user u ON u.id = st.studentID " +
                                        "WHERE ls.id = '" + headID + "' AND st.studentID = '" + studentsID + "'"
                        );
                        client.sendToClient(new MsgHandler<>(TypeMsg.StudentReportImported, studentInfo));
                    }
                    break;
                case GetUser:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<User> userLists = MysqlConnection.getUser("SELECT * FROM user u " +
                            "JOIN lecturersubject ls ON u.id = ls.id " +
                            "WHERE ls.subjectid = (" +
                            "   SELECT subjectid " +
                            "   FROM lecturersubject " +
                            "   WHERE id = '" + obj + "')");
                    client.sendToClient(new MsgHandler<>(TypeMsg.UserImported, userLists));
                    break;
                case GetTestsByLecutrer:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<StudentTest> testsList = MysqlConnection.getStudentInfo("SELECT st.* FROM studentstest st JOIN test t ON st.testID = t.id WHERE t.author = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.ImportedTestsByLecturer, testsList));
                    break;
                case GetTestsByLecutrerForLecturerReport:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<StudentTest> testsListforReport = MysqlConnection.getStudentInfo("SELECT st.* FROM studentstest st JOIN test t ON st.testID = t.id WHERE t.author = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.ImportedTestsByLecturerForLecturerReport, testsListforReport));
                    break;
                case GetTestsByCourse:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<StudentTest> testsListbyCourse = MysqlConnection.getStudentInfo("SELECT st.* FROM studentstest st WHERE st.course = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.ImportedTestsByCourse, testsListbyCourse));
                    break;

                case DeleteTest:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (Test) this.msg.getMsg();
                    this.test = (Test) obj;
                    String DeleteTestQuery = "DELETE FROM cems.test WHERE id='" + test.getId() + "'";
                    MysqlConnection.update(DeleteTestQuery);

                    String deleteQuestionsQuery = "DELETE FROM cems.testquestion WHERE testID='" + test.getId() + "'";
                    MysqlConnection.update(deleteQuestionsQuery);
                    client.sendToClient(new MsgHandler<>(TypeMsg.DeleteTestResponse, null));
                    break;
                case GetTestQuestions:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    if (obj instanceof Test) {
                        this.test = (Test) obj;
                        ArrayList<TestQuestion> testsQuestionsList = MysqlConnection.getTestQuestionsTable("SELECT * FROM cems.testquestion WHERE testID='" + test.getId() + "'");
                        client.sendToClient(new MsgHandler<>(TypeMsg.GetTestQuestionsResponse, testsQuestionsList));
                    }
                    break;
                case GetActiveTests:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<ActiveTest> activeTestsList = MysqlConnection.getActiveTestsTable("select * from cems.activetest;");
                    client.sendToClient(new MsgHandler<>(TypeMsg.GetActiveTestsResponse, activeTestsList));
                    break;
                case GetActiveTestsByLecturer:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<ActiveTest> lecturerActiveTestsList = MysqlConnection.getActiveTestsTable(
                            "SELECT at.* FROM cems.activetest AS at " +
                                    "JOIN test AS t ON at.id = t.id " +
                                    "WHERE t.author = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.GetActiveTestsByLecturerResponse, lecturerActiveTestsList));
                    break;
//                case UpdateRemainingTime:
//                    this.msg = (MsgHandler<Object>) msg;
//                    this.obj = (ActiveTest) this.msg.getMsg();
//                    this.activeTest = (ActiveTest) obj;
//                    String updateRemainingTimeQuery = "UPDATE cems.activetest SET " +
//                            " timeLeft='" + activeTest.getTimeLeft() + "'" +
//                            " WHERE id='" + activeTest.getId() + "'";
//
//                    MysqlConnection.update(updateRemainingTimeQuery);
//                    client.sendToClient(new MsgHandler<>(TypeMsg.UpdateRemainingTimeResponse, null));
                // break;
                case getQuestionAndAnswerFromTest:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    Question questionData = MysqlConnection.getQuestionData("SELECT * FROM question WHERE id = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.importedQuestionAndAnswerFromTest, questionData));
                    break;
                case GetTestQuestionsById:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<TestQuestion> testsQuestionsList = MysqlConnection.getTestQuestionsTable("SELECT * FROM cems.testquestion WHERE testID='" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.GetTestQuestionsResponse, testsQuestionsList));
                    break;
                case AddStudentAnswer:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (AnswerOfStudent) this.msg.getMsg();
                    AnswerOfStudent answer = (AnswerOfStudent) obj;
                    MysqlConnection.update("INSERT INTO cems.answersofstudent (studentID, testID, questionId, studentsAnswer) " +
                            "VALUES ('" + answer.getStudentID() + "', " +
                            "'" + answer.getTestID() + "', " +
                            "'" + answer.getQuestionID() + "', " +
                            "'" + answer.getStudentsAnswer() + "')");

                    client.sendToClient(new MsgHandler<>(TypeMsg.StudentAnswerAdded, null));
                    break;
                case GetTestByID:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    Test testInfo = MysqlConnection.getTestData("SELECT * FROM cems.test WHERE id ='" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.ImportedTestByID, testInfo));
                    break;
                case AddNewTestOfStudent:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    if (obj instanceof StudentTest) {
                        StudentTest newTest = (StudentTest) obj;
                        String newQuery = "INSERT INTO cems.studentstest (studentID, testID, subjectID, course, " +
                                "testType, score, fullname, year, semester, session, suspicionOfCheating, " +
                                "correctAnswers, totalQuestions, lecturerComments, approved,testDuration) " +
                                "VALUES ('" + newTest.getStudentID() + "', " +
                                "'" + newTest.getTestID() + "', " +
                                "'" + newTest.getSubjectID() + "', " +
                                "'" + newTest.getCourse() + "', " +
                                "'" + newTest.getTestType() + "', " +
                                "'" + newTest.getScore() + "', " +
                                "'" + newTest.getFullname() + "', " +
                                "'" + newTest.getYear() + "', " +
                                "'" + newTest.getSemester() + "', " +
                                "'" + newTest.getSession() + "', " +
                                "'" + newTest.getSuspicionOfCheating() + "', " +
                                "'" + newTest.getCorrectAnswers() + "', " +
                                "'" + newTest.getTotalQuestions() + "', " +
                                "'" + newTest.getLecturerComments() + "', " +
                                "'" + newTest.getApproved() + "', " +
                                "'" + newTest.getTestDuration() + "') ";
                        MysqlConnection.update(newQuery);
                        client.sendToClient(new MsgHandler<>(TypeMsg.TestOfStudentSaved, null));

                    }
                    break;
                case CheckStudentRegisteredCourse:
                    this.msg = (MsgHandler<Object>) msg;
                    List<Object> studentsDetails = (List<Object>) this.msg.getMsg();
                    String studentID = (String) studentsDetails.get(0);
                    String courseName = (String) studentsDetails.get(1);
                    StudentCourse isRegistered = MysqlConnection.checkRegistered("SELECT * FROM studentscourse WHERE studentID = '" + studentID + "' AND course = '" + courseName + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.StudentVerified, isRegistered));
                    break;
                case IcreaseStudentsEnteringTest:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    MysqlConnection.update("UPDATE aftertestinfo SET totalStudents = totalStudents + 1 WHERE testID = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.TotalStudentsInTestIncreased, null));
                    break;
                case IcreaseStudentsFinishedTest:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    MysqlConnection.update("UPDATE aftertestinfo SET totalFinished = totalFinished + 1 WHERE testID = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.StudentsFinishedTestIncreased, null));
                    break;

                case GetsubjectNametoID:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    Subject subjectName = MysqlConnection.SubjectName("SELECT * FROM subject WHERE subjectName = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.ImportedSubjectIDfromName, subjectName));
                case RequestExtraTime:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    if (obj instanceof TestRequestForApproval) {
                        TestRequestForApproval newRequest = (TestRequestForApproval) obj;
                        String newQuery = "INSERT INTO cems.testrequest (newDuration, explanation, id, subject,course,author) " +
                                "VALUES ('" + newRequest.getNewDuration() + "', " +
                                "'" + newRequest.getExplanation() + "', " +
                                "'" + newRequest.getId() + "', " +
                                "'" + newRequest.getSubject() + "', " +
                                "'" + newRequest.getCourse() + "', " +
                                "'" + newRequest.getAuthor() + "') ";
                        MysqlConnection.update(newQuery);
                        client.sendToClient(new MsgHandler<>(TypeMsg.ExtraTimeRequested, null));
                    }
                    break;
                case GetTestForApproval:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    ArrayList<TestForApproval> testsForApproval = MysqlConnection.getTestForApproval(
                            "SELECT st.* FROM cems.studentstest AS st " +
                            "JOIN test AS t ON st.testId = t.id " +
                            "LEFT JOIN activetest AS at ON t.id = at.id " +
                            "WHERE t.author = '" + obj + "' AND at.id IS NULL");
                    client.sendToClient(new MsgHandler<>(TypeMsg.GetTestForApprovalResponse, testsForApproval));
                    break;
                case UpdateTheApproveofLecturer:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = this.msg.getMsg();
                    if (obj instanceof TestForApproval) {
                        this.testApprove = (TestForApproval) obj;
                        String updateQuery = "UPDATE cems.studentstest " +
                                "SET studentID = '" + testApprove.getStudentID() + "', " +
                                "subjectID = '" + testApprove.getSubjectID() + "', " +
                                "course = '" + testApprove.getCourse() + "', " +
                                "score = '" + testApprove.getGrade() + "', " +
                                "fullname = '" + testApprove.getFullname() + "', " +
                                "year = '" + testApprove.getYear() + "', " +
                                "semester = '" + testApprove.getSemester() + "', " +
                                "session = '" + testApprove.getSession() + "', " +
                                "suspicionOfCheating = '" + testApprove.getSuspicionOfCheating() + "', " +
                                "correctAnswers = '" + testApprove.getCorrectAnswers() + "', " +
                                "totalQuestions = '" + testApprove.getTotalQuestions() + "', " +
                                "lecturerComments = '" + testApprove.getLecturerComments() + "', " +
                                "approved = '" + testApprove.getApproved() + "', " +
                                "testType = '" + testApprove.getTestType() + "' " +
                                "WHERE testID = '" + testApprove.getTestID() + "'" +
                                "AND studentID = '" + testApprove.getStudentID() + "'";
                        MysqlConnection.update(updateQuery);
                        client.sendToClient(new MsgHandler<>(TypeMsg.UpdateTheApproveofLecturerResponse, null));
                        break;
                    }
                case AddNewActiveTest:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (ActiveTest) this.msg.getMsg();
                    ActiveTest newActiveTest = (ActiveTest) obj;
                    String newActiveTestQuery = "INSERT INTO cems.activetest (id, numOfQuestions, testDate, " +
                            "startingTime, testCode) " +
                            "VALUES ('" + newActiveTest.getId() + "', " +
                            "'" + newActiveTest.getNumOfQuestions() + "', " +
                            "'" + newActiveTest.getTestDate() + "', " +
                            "'" + newActiveTest.getStartingTime() + "', " +
                            "'" + newActiveTest.getTestCode() + "') ";

                    MysqlConnection.update(newActiveTestQuery);
                    client.sendToClient(new MsgHandler<>(TypeMsg.AddNewActiveTestResponse, null));
                    break;
                case AddNewAfterTestInfo:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String[]) this.msg.getMsg();
                    String[] infoArray = (String[]) obj;

                    String newAfterTestInfoQuery = "INSERT INTO cems.aftertestinfo (testID,date,testDuration,actualDuration,totalFinished,totalForcedFinished,totalStudents) " +
                            "VALUES ('" + infoArray[0] + "', " +
                            "'" + infoArray[1] + "', " +
                            "'" + infoArray[2] + "', " +
                            "'" + 0 + "', " +
                            "'" + 0 + "', " +
                            "'" + 0 + "', " +
                            "'" + 0 + "') ";


                    MysqlConnection.update(newAfterTestInfoQuery);
                    client.sendToClient(new MsgHandler<>(TypeMsg.AddNewAfterTestInfoResponse, null));
                    break;
                case FinishAfterTestInfo:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String[]) this.msg.getMsg();
                    String[] afterTestInfo = (String[]) obj;
                    MysqlConnection.update("UPDATE aftertestinfo SET actualDuration = '" + Integer.parseInt(afterTestInfo[0]) +
                            "', totalForcedFinished = '" + Integer.parseInt(afterTestInfo[1]) +
                            "' WHERE testID = '" + afterTestInfo[2] + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.AfterTestRowCompleted, null));
                    break;

                case NumberOfAttendedCounter:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    int totalStudentsInTest = MysqlConnection.getTotalStudentsInTest("SELECT totalStudents FROM aftertestinfo WHERE testID = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.ImportedNumberOfAttendedCounter, totalStudentsInTest));
                    break;
                case CountRegisteredStudents:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    int totalStudentsRegistered = MysqlConnection.getTotalStudentsRegisteredToTest("SELECT COUNT(*) AS row_count FROM studentscourse WHERE course = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.ImportedRegisteredStudents, totalStudentsRegistered));
                    break;
                case CountNumberOfFinished:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    int totalStudentsFinished = MysqlConnection.getTotalStudentsFinishedTheTest("SELECT totalFinished FROM aftertestinfo WHERE testID = '" + obj + "'");
                    client.sendToClient(new MsgHandler<>(TypeMsg.ImportedNumberOfFinished, totalStudentsFinished));
                    break;
                case UnActivateTest:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    String DeleteActiveTestQuery = "DELETE FROM cems.activetest WHERE id='" + obj + "'";
                    MysqlConnection.update(DeleteActiveTestQuery);
                    client.sendToClient(new MsgHandler<>(TypeMsg.CompleteUnactivatingTest, null));
                    break;
                case DetectedCheating:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                     String QueryOfCheating = "UPDATE cems.studentstest " +
                             "SET suspicionOfCheating = 'YES' " +
                             "WHERE EXISTS (" +
                             "    SELECT 1 " +
                             "    FROM cems.answersofstudent a1 " +
                             "    JOIN cems.answersofstudent a2 ON a1.testID = a2.testID " +
                             "        AND a1.questionID = a2.questionID " +
                             "        AND a1.studentsAnswer = a2.studentsAnswer " +
                             "        AND a1.studentID <> a2.studentID " +
                             "    WHERE a1.testID = cems.studentstest.testID" +
                             "      AND a1.studentID = cems.studentstest.studentID" +
                             "    GROUP BY a1.testID, a1.studentID " +
                             "    HAVING COUNT(DISTINCT a1.questionID) = " +
                             "           (SELECT COUNT(DISTINCT questionID) " +
                             "            FROM cems.answersofstudent " +
                             "            WHERE testID = cems.studentstest.testID" +
                             "              AND studentID = a1.studentID)" +
                             ") " +
                             "AND score < 100";
                    MysqlConnection.update(QueryOfCheating);
                    client.sendToClient(new MsgHandler<>(TypeMsg.DetectedCheatingResponse, null));
                    break;
                case ChangeIsLoggedValue:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String[]) this.msg.getMsg();
                    String[] LoginValues = (String[]) obj;
                    String changeLoginValue = "UPDATE user SET isLoggedIn = '" + Integer.parseInt(LoginValues[1]) + "' WHERE id = '" + LoginValues[0] + "'";
                    MysqlConnection.update(changeLoginValue);
                    client.sendToClient(new MsgHandler<>(TypeMsg.IsLoggedValueChanged, null));
                    break;
                case StudentsTestIsApprvoed:
                    this.msg = (MsgHandler<Object>) msg;
                    this.obj = (String) this.msg.getMsg();
                    String idTofullName = "SELECT fullName FROM user WHERE id = '" + obj + "'";
                    String fullname = MysqlConnection.getIDreturnFullname(idTofullName);
                    sendToAllClients(new MsgHandler<>(TypeMsg.StudentsTestIsApprvoedToAllClients, fullname));
                    client.sendToClient(new MsgHandler<>(TypeMsg.StudentsTestIsApprvoedResponse, null));
                    break;
                case LecturerCllickedLockTest:
                    sendToAllClients(new MsgHandler<>(TypeMsg.LockTestForStudentByLecturer, null));
                    client.sendToClient(new MsgHandler<>(TypeMsg.LecturerCllickedLockTestResponse, null));
                    break;
            }

        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }
}