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
	},GetQuestions {
		public String toString() {
			return "User asked table questions";
		}
	},QuestionsResponse {
		public String toString() {
			return "Here is the questions table";
		}
	},EditQuestion {
		public String toString() {
			return "User asked to edit question";
		}
	},AddNewQuestion{
		public String toString() {
			return "User asked to add a question";
		}
	},QuestionAddedSuccessfuly{
		public String toString() {
			return "User added a question";
		}

	},DeleteQuestion {
		public String toString() {
			return "User asked to delete question";
		}
	},QuestionDeleted {
		public String toString() {
			return "User deleted a question";
		}
	},QuestionUpdated {
		public String toString() {
		return "User updated a question ";
	}
	}


}


