
<div align="middle">
  <img src="https://github.com/user-attachments/assets/0e56a82f-7852-43d7-a912-35e4a78f7409"/>
</div>

<h1 align="center">
  CEMS - Computerized tests management system
</h1>



-----
<div align="middle">
  <img src="https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" href="https://www.java.com/"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=fff" href="https://www.mysql.com/"/>
</div>

# Overview
CEMS is a program for lecturers and students, giving users the capability of creating, managing and performing tests. Users can write edit and delete tests, and various statistics of submitted tests are available for viewing. Developed as a midterm project in the ***Software Engineering Department*** at ***Braude Academic College***.


# Features

## Create a Test
<div align="middle">
    <img src="https://github.com/user-attachments/assets/d6670737-61bb-4f10-a32a-8f3f78531ab9" width="500px"/>
</div>

  - Users with a "lecturer" role can add new tests to the database.
  - Tests consist of multiple-choice questions which are also be written by the lecturers.
  - The lecturers can set the test's duration, the subject and course, and add notes for students or other lecturers.
    
## Take a Test
  - Users with a "student" role can take tests by entering the test ID.
  - Multiple users can take a test simultaneously, with the test having a shared start and end time.
  - The lecturer can add extra time or lock the test if needed.
  - The results and data of the test are saved to a MySQL database.

## Test verification
  - The tests are chekced automatically, and further changes to the grade, in addition to personalized comments can be done for each test by the lecturer.
  - Once a test has been manually checked by the lecturer, they can finish the verification process and save the test with the updated data in the database.

## Reports
<div align="middle">
    <img src="https://github.com/user-attachments/assets/ccde4a8d-237f-46a3-9668-475fbcef7fe5" width="500px"/>
</div>
  - Users with a "head of department" and a "lecturer" role can view statistical data for all tests that have been administered.\
  - Available reports can be filtered by subject, semester, and year.
  - The screen gives the user the ability to see the grades distribution for the specified test, along with the median and average grades.

## Contributors
- [Fredi Bulshtein](https://github.com/fredi1574)
- [Roman Guriy](https://github.com/Roman-G-579)
- [Yuval Hilay](https://github.com/YuvalHilay)
- [Abed Tayar](https://github.com/AbedTayar1)
- [Shay Garbuz](https://github.com/garbuzshay)
- [May Caspi](https://github.com/MayCaspi)
- [Noa Krispin](https://github.com/noakrispin)


## Licence
This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/Oneill19/GPTalk/blob/main/LICENSE) file for details.
