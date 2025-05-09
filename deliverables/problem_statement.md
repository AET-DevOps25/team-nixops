# Problem Statement

## Main Functionality
  - Lecture/Course scheduling tool for creating a conflictless semester plan based on the user's preferences
  - Aggregate course information from TUM services
  - Determine scheduling conflicts
  - Determine user preferences (Course of study, content, description, lecturer, etc.) 
  - Rank courses based on preferences
  - Visualize current schedule
  - Export to calendar

## Intended Users
  - TUM students planning their next semesters
  - TUM students having to replan their current semester (due to unexpected circumstances)
  - Potential TUM students and exchange students trying to explore the TUM course catalogue

## GenAI Integration
  - Determine how well a course matches the users preferences and interests
  - Consider user feedback on courses
  - Suggest courses and give reasoning
  - Refine schedule over multiple iterations

## Scenarios

### Scenario 1: First-Semester Master's Student Creating an Initial Plan

**User:** Sara, a first-semester Master's student in Informatics\
**Goal:** Create a semester plan that aligns with her degree requirements and personal interests.\
**Flow:**

* Sara selects her degree program.
* She indicates interest in AI and software engineering.
* The tool imports course options from TUMonline.
* It highlights potential conflicts and generates multiple conflict-free combinations.
* GenAI suggests courses like "Introduction to Deep Learning" with rationales based on her interests.
* Sara reads the course descriptions and gives feedback
* The AI refines the course suggestions based on the feedback
* Sara exports the schedule to her calendar

### Scenario 2: Final-Year Student Filling Elective Credits

**User:** Max, a 5th-semester Bachelor's student in Electrical Engineering\
**Goal:** Fill elective credits with easier or well-rated courses.\
**Flow:**

* He wants courses that are low workload but still interesting, preferably in power systems or sustainability.
* The tool suggests non-overlapping, highly-rated courses based.
* Max sees a ranked list of courses, reads explanations, and finalizes a plan.

### Scenario 3: Student Trying to Avoid a Specific Lecturer

**User:** Anna, a second-year student\
**Goal:** Plan around a lecturer she finds difficult to follow.\
**Flow:**

* Anna lists a few preferred topics and names lecturers she wants to avoid.
* The AI checks all course instances and flags any with the specified lecturer.
* GenAI suggests alternative courses or lecture slots with different instructors.
* Anna selects a plan with no scheduling conflict and her preferred instructors.

### Scenario 4: International Exchange Student Exploring Courses

**User:** David, an Erasmus student visiting for one semester\
**Goal:** Explore interdisciplinary courses in English that don’t overlap.\
**Flow:**

* David tells the tool he only wants courses in english.
* The tool aggregates offerings across faculties and ensures no timing conflicts.
* GenAI gives recommendations based on popularity with other Erasmus students.
* David customizes the plan and exports it as a .ics file for his personal calendar.

### Scenario 5: Course Replanning due to Dissatisfaction

**User:** Lena, a 3rd-semester student in Mechanical Engineering\
**Goal:** Adjust schedule quickly after course turns out to be unsuitable.\
**Flow:**

* Lena visits the first lecture of a course and decides to drop out of the course.
* GenAI recommends substitute courses with overlapping topics and no timing conflicts.
* Lena evaluates suggestions and selects an alternative.
