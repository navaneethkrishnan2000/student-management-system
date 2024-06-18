package com.student.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.student.entity.Student;
import com.student.service.StudentService;

@Controller
public class StudentController {
	
	@Autowired
	StudentService studentService;
	
	private static String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";
	
	// Constructor to ensure the upload directory exists
	public StudentController() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
	
	//Handler to show the first page with the details
	@GetMapping("/students")
	public String getStudents(Model model) {	//Model object is used to pass information from controller to the view(html page). in our case using this getAllStudents() method we get all the information stored in the db and pass it as a value to the key(students)
        model.addAttribute("students", studentService.getAllStudents());	// Adds a new Student object to the model, which the form will bind to , key value pair, students is the key and studentService.getAllStudents() is the value
		return "studentList";
	}
	
	//Handler to show the create new student page
	@GetMapping("/students/new")
    public String createStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "createStudent";
    }
	
	//Handler to save new student to the db
	@PostMapping("/students")
    public String createStudent(@ModelAttribute("student") Student student, @RequestParam("profileImage") MultipartFile file) {
		String fileName = saveUploadedFile(file);
		if(fileName != null) {
			student.setProfileImage(fileName);
		}
		
        studentService.createStudent(student);
        return "redirect:/students"; // Redirect to the list of students after saving
    } 
	
	//Handler to show the  edit or update Student page
	@GetMapping("/students/edit/{id}")
	public String updateStudentForm( @PathVariable("id") Long id, Model model) {	//using path-variable annotation extracting the student details from the form
		model.addAttribute("student", studentService.getStudentById(id));
		return "updateStudent";
	}
	
	//Handler to save the updated student details to the db
	@PostMapping("/students/{id}")
	public String updateStudent(@PathVariable Long id,
			@ModelAttribute("student")Student student, @RequestParam("profileImage") MultipartFile file, 
			Model model) {
		
		String fileName = saveUploadedFile(file);
        if (fileName != null) {
            student.setProfileImage(fileName);
        }
		
		//get from the db by id
		Student existingStudent = studentService.getStudentById(id);
		existingStudent.setId(id);
		existingStudent.setProfileImage(student.getProfileImage());
		existingStudent.setFirstName(student.getFirstName());
		existingStudent.setLastName(student.getLastName());
		existingStudent.setEmail(student.getEmail());
		existingStudent.setDateOfBirth(student.getDateOfBirth());
		existingStudent.setGender(student.getGender());
		existingStudent.setAddress(student.getAddress());
		existingStudent.setPhoneNumber(student.getPhoneNumber());
		existingStudent.setEnrollmentDate(student.getEnrollmentDate());
		existingStudent.setGpa(student.getGpa());
		
		//save updates student object
		studentService.updateStudent(existingStudent);
		return "redirect:/students";
		
	}
	
	//Handler to delete the row
	@GetMapping("/students/{id}")
    public String deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
        return "redirect:/students";
    }
	
	private String saveUploadedFile(MultipartFile file) {
		if(!file.isEmpty()) {
			try {
				byte[] bytes = file.getBytes();
				Path path = Paths.get(UPLOAD_DIR + "/" + file.getOriginalFilename());
				Files.write(path, bytes);
				return file.getOriginalFilename();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
