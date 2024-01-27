package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME" + userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER" + user);

		model.addAttribute("user", user);
	}

// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

//	add contact page
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
		
			if (file.isEmpty()) {
				System.out.println("file is empty");
				contact.setImage("contact.png");

			} else {

				contact.setImage(file.getOriginalFilename());
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");

			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("data" + contact);
			System.out.println("contact added");
			session.setAttribute("message", new Message("Your contact is added.Add new contact", "success"));
		} catch (Exception e) {
			System.out.println("error" + e.getMessage());
			session.setAttribute("message", new Message("Something went wrong ", "danger"));
		}

		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContats(@PathVariable("page") Integer page,Model model,Principal principal)
	{
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		Pageable pageable= PageRequest.of(page, 4);
	Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());

		model.addAttribute("title", "Show user contacts");
		return "normal/show_contacts";
	}
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{ 
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
	 if(user.getId()==contact.getUser().getId())
	 {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
	 }
	
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model m,HttpSession session,Principal principal)
	{
		Optional <Contact> contactOptional= this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
//		contact.setUser(null);
//		this.contactRepository.delete(contact);
		User user=this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		
		
		
		session.setAttribute("message", new Message("contact deleted successfully","success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	@PostMapping("/update-contact/{cid}")
	     public String updateForm(@PathVariable("cid") Integer cid,Model m)
		{  
		Contact contact=this.contactRepository.findById(cid).get();
			m.addAttribute("title","Update contact");
			m.addAttribute("contact",contact);
			return "normal/update_form";
		}
	
     @PostMapping("/process-update")
     public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,Principal principal,HttpSession session)
     {
    	 Contact oldcontactDetail=this.contactRepository.findById(contact.getcId()).get();
    	 try {
    		 if(!file.isEmpty())
    		 {
    			 File deletefile = new ClassPathResource("static/img").getFile();
    			 File file1=new File(deletefile,oldcontactDetail.getImage());
    			 file1.delete();
    			 
    			 
    			 File savefile = new ClassPathResource("static/img").getFile();
 				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
 				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
 				contact.setImage(file.getOriginalFilename());
    		 }
    		 else {
    			 contact.setImage(oldcontactDetail.getImage());
    		 }
  
    		 User user=this.userRepository.getUserByUserName(principal.getName());
    		 contact.setUser(user);
    		 this.contactRepository.save(contact); 
    		
    		 session.setAttribute("message", new Message("Your contact is updated","success") );
    		 
    		 
    		 
    		 
    		 
    		 
    	 }
    	 catch(Exception e)
    	 {
    		 e.printStackTrace();
    	 }
    	 
    	 return "redirect:/user/"+contact.getcId()+"/contact";
     } 
     @GetMapping("/profile")
     public String yourProfile(Model m)
     {
    	 m.addAttribute("title","Profile page");
    	 return "normal/profile";
     }
     
     @GetMapping("/settings")
     public String openSettings()
     {
    	 return "normal/settings";
     }
     
     @PostMapping("/change-password")
     public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
     {
    	 String userName=principal.getName();
    	 User currentUser=this.userRepository.getUserByUserName(userName);
    	 if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
    	 {
    		 
    		 currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
    		 this.userRepository.save(currentUser);
    		 session.setAttribute("message",new Message("Your contact is updated","success"));
    		
    	 }
    	 else {
    		 session.setAttribute("message",new Message("Please enter correct old password","danger")); 
    		 return "redirect:/user/settings";
    	 }
    	 
    	 return "redirect:/user/index";
    	
     }
     
  
	
	}

