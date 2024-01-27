package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@RequestMapping("/home")
	public String home(Model model) {
		model.addAttribute("title", "Smart-Contact Manager");
		return "home";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register- Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// Handler for registering user
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}

			if (result.hasErrors()) {
				System.out.println(result);

				// model.addAttribute("user", user);
				return "signup";
			}
		
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println(user);
			System.out.println(agreement);

			User res = userRepository.save(user);
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));

			model.addAttribute("user", new User());

			return "signup";

		} 
			catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));
			return "signup";
		}

		
	}
	
	@GetMapping("/signin")
	public String customLogin(Model m)
	{
		m.addAttribute("title","Login page");
		return "login";
	}

}


