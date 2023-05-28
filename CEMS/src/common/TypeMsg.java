package common;

public enum TypeMsg {


    Disconnected {
        public String toString() {
            return "Disconnected";
        }
    },
    Connected {
        public String toString() {
            return "Connected";
        }
//	},
//	ImportedSuccessfully {
//		public String toString() {
//			return "User asks to logout";
//		}
    }, GetQuestions {
        public String toString() {
            return "User asked for specific table questions";
        }
    }, QuestionsResponse {
        public String toString() {
            return "Here is the questions table";
        }
    }, GetAllQuestions {
        public String toString() {
            return "User asked for all questions";
        }
    }, allQuestionImported {
        public String toString() {
            return "imported all questions successfully ";
        }
    }, TryLogin {
        public String toString() {
            return "new user try to login.";
        }
    }, LoginResponse {
        public String toString() {
            return "User pressed Login";
        }

    }, importSubjects {
        public String toString() {
            return "import Subjects list for user";
        }
    }, CoursesimportSuccess {
        public String toString() {
            return "import courses Successfully";
        }
    }, importCourses {
        public String toString() {
            return "import Courses list for user";
        }

    }, EditQuestion {
        public String toString() {
            return "User asked to edit question";
        }
    }, AddNewQuestion {
        public String toString() {
            return "User asked to add a question";
        }
    }, QuestionAddedSuccessfuly {
        public String toString() {
            return "User added a question";
        }

    }, DeleteQuestion {
        public String toString() {
            return "User asked to delete question";
        }
    }, QuestionDeleted {
        public String toString() {
            return "User deleted a question";
        }
    }, QuestionUpdated {
        public String toString() {
            return "User updated a question ";
        }
    }, SubjectsimportSuccess {
        public String toString() {
            return "import subjects Successfully";
        }
    }


}


