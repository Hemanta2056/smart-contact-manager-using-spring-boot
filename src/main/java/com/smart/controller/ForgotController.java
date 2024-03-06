package com.smart.controller;

import java.util.Random;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
    @Autowired
    private UserRepository userRepository;

    Random random = new Random(1000);

    @Autowired
    private EmailService emailService;

    // Email id form open handler
    @GetMapping("/forget")
    public String openEmailForm() {
        return "forget_email_form";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, RedirectAttributes redirectAttributes,
            HttpSession session) { // Add HttpSession parameter

        System.out.println("EMAIL " + email);

        // Generating OTP of 4 digits
        int otp = random.nextInt(99999);
        System.out.println("OTP " + otp);

        // Write code for OTP to send email
        String subject = "OTP from SCM";
        String message = "OTP IS " + otp;
        String to = email;

        boolean flag = this.emailService.sendEmail(subject, message, to);

        if (flag) {
            // Store OTP and email in HttpSession
            session.setAttribute("myotp", otp);
            session.setAttribute("email", email);
            return "verify_otp";
        } else {
            redirectAttributes.addFlashAttribute("message", new Message("Check your email id", "success"));
            return "redirect:/forget"; // Redirect to the email form
        }
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") int otp, HttpSession session, Model model) {

        int myotp = (int) session.getAttribute("myotp");
        String email = (String) session.getAttribute("email");

        if (myotp == otp) {
            // Password change form
            User user = this.userRepository.getUserByUserName(email);

            if (user == null) {
                // Send error message
                model.addAttribute("message", new Message("User does not exist with this email", "danger"));
                return "forget_email_form";
            } else {
                // Send change password form
                return "password_change_form";
            }
        } else {
            model.addAttribute("message", new Message("You have entered the wrong OTP", "danger"));
            return "verify_otp";
        }
    }
    
    
    //change password
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword") String newpassword ,HttpSession session ,RedirectAttributes redirectAttributes) {
    	
    	 String email = (String) session.getAttribute("email");
    	 User user = this.userRepository.getUserByUserName(email);
    	
    	 user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
    	 this.userRepository.save(user);
    	 
    	 return "redirect:/signin?change=password changed successfully ..";
    	 
    }
}
