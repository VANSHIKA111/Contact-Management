package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	@	Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		
		return "forgot_email_form";
	}
	@RequestMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session )
	{
		Random random=new Random(1000);
		int otp=random.nextInt(999999);
		
		String subject ="OTP from SCM";
		String message="OTP="+otp+"";
		String to=email;
		boolean flag=this.emailService.sendEmail(subject, message, to);
		if(flag)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email",email);
			return "verify_otp";
		}
		else {
			session.setAttribute("message","Resend OTP");
			return "forgot_email_form";
		}

	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session)
	{ int myotp=(int )session.getAttribute("myotp");
	   String email=(String)session.getAttribute("email");
	   
	   if(myotp==otp)
	   { 
		   User user=this.userRepository.getUserByUserName(email);
		   if(user==null)
		   {
			   session.setAttribute("message","User does not exist");
			   return "forgot_email_form";
		   }
		   else {
			   
			   return "password_change_form";
		   }
		   
		   
		  
	   }
	   else {
		   session.setAttribute("message","You have entered wrong OTP");
		   return "verify_otp";
		   
	   }
	  
	
		
		
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session)
	{
		
		 String email=(String)session.getAttribute("email");
		 User user=this.userRepository.getUserByUserName(email);
		 user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		 this.userRepository.save(user);
		return "redirect:/signin?change=password changed successfully";
		
	}
	
	
	
	
}
