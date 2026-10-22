# Task-Tracker
Java Project
# Task Tracker CLI – Java

## Project Overview
Task Tracker CLI is a simple command-line application developed using Java to manage daily tasks. It allows users to add, update, delete, and track tasks based on their status. The application stores task details in a JSON file so that tasks remain available between program executions.

## Technologies Used
- Java
- Core Java
- Java File I/O
- JSON file storage
- Command-Line Interface (CLI)

## Features
- Add a new task
- Update an existing task
- Delete a task
- Mark a task as in progress
- Mark a task as done
- List all tasks
- List tasks by status: `todo`, `in-progress`, or `done`
- Automatically create `tasks.json` if it does not exist
- Store task ID, description, status, creation time, and update time
- Handle invalid inputs and missing tasks

## Project Structure

```text
TaskTracker/
├── TaskCLI.java
├── tasks.json
└── README.md
```

**Note:** The `tasks.json` file is created automatically when the application runs for the first time.

## Prerequisites
- Java Development Kit (JDK 11 or later)
- Command Prompt or terminal
- A text editor or Java IDE

## How to Compile and Run

### 1. Open the project directory
Open a terminal in the folder containing `TaskCLI.java`.

### 2. Compile the program

```bash
javac TaskCLI.java
```

### 3. Run the application

Use the following commands to perform different operations.

## Commands and Usage

### Add a Task

```bash
java TaskCLI add "Buy groceries"
```

Output:
```text
Task added successfully (ID: 1)
```

### Update a Task

```bash
java TaskCLI update 1 "Buy groceries and cook dinner"
```

### Delete a Task

```bash
java TaskCLI delete 1
```

### Mark a Task as In Progress

```bash
java TaskCLI mark-in-progress 1
```

### Mark a Task as Done

```bash
java TaskCLI mark-done 1
```

### List All Tasks

```bash
java TaskCLI list
```

### List Tasks by Status

List pending tasks:

```bash
java TaskCLI list todo
```

List tasks in progress:

```bash
java TaskCLI list in-progress
```

List completed tasks:

```bash
java TaskCLI list done
```

## Task Properties

Each task contains the following properties:

| Property | Description |
|---|---|
| `id` | Unique identifier for the task |
| `description` | Description of the task |
