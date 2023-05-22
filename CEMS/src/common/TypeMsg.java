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
	},TryLogin {
		public String toString() {
			return "new user try to login.";
		}
	},LoginSuccess {
		public String toString() {
			return "new user logged into the system.";
		}
	},LogoutSuccess {
		public String toString() {
			return "user logged out from the system.";
		}
	},EditQuestion {
		public String toString() {
			return "User asked to edit question";
		}
	},QuestionUpdated {
	public String toString() {
		return "User updated a question ";
	}
	}


}


