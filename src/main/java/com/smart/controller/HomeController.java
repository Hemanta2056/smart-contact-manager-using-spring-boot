package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired    
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/")
	public String Home(Model model) {

		model.addAttribute("title", "Home-smart contact manager");

		return "home";
	}

	@GetMapping("/about")
	public String About(Model model) {

		model.addAttribute("title", "About-smart contact manager");

		return "about";
	}

	@GetMapping("/signup")
	public String Signup(Model model) {

		model.addAttribute("title", "Register-smart contact manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handling for registering user

	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			RedirectAttributes redirectattributes) {

		try {

			if (!agreement) {

				System.out.println(" YOU HAVE NOT AGREED THE TERMS AND CONDITIONS");

				throw new Exception(" YOU HAVE NOT AGREED THE TERMS AND CONDITIONS");

			}

			if (result1.hasErrors()) {

				System.out.println("ERROR" + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement " + agreement);
			System.out.println("User " + user);

			User result = this.userRepository.save(user);

			model.addAttribute("user", new User());

			redirectattributes.addFlashAttribute("message", new Message("Successfully registered", "success"));

			//return "signup";
			return "redirect:/signup";

		} catch (Exception e) {

			e.printStackTrace();
			model.addAttribute("user", user);
			redirectattributes.addFlashAttribute("message", new Message("Something went wrong" + e.getMessage(), "danger"));

			//return "signup";
			return "redirect:/signup";
		}

	}

//handler for custom login

	@GetMapping("/signin")
	public String customLogin(Model model) {

		model.addAttribute("title","Login Page");
		return "login";
	}
	
	
	
	 
}
