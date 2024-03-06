package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.id.IntegralDataTypeHolder;
import org.json.JSONObject;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;


import jakarta.transaction.Transactional;

import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private UserRepository userRepository;

	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String userName = principal.getName();
		System.out.println("Username " + userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println(user);

		model.addAttribute("user", user);

	}

	// dashboard home
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";
	}

	// open add handler

	@GetMapping("/add-contact")
	public String aopenAddcontactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";

	}

	// processing add contact form

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, RedirectAttributes redirectAttributes, Model model) {

		try {
			String name = principal.getName();

			User user = this.userRepository.getUserByUserName(name);

			if (file.isEmpty()) {

				// if file is empty
				System.out.println("file is empty");
				contact.setImage("contact.png");

			} else {
				// upload the file to folder and update the name to contact

				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("image is uploaded");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("Data" + contact);
			System.out.println("Added to database");

			// message success
			// redirectAttributes.addFlashAttribute("message", new Message("Your contact is
			// added !! ADD MORE","success"));

			model.addAttribute("message", new Message("Your contact is added !! ADD MORE", "success"));

		} catch (Exception e) {

			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();

			// message error
			// redirectAttributes.addFlashAttribute("message", new Message("something went
			// wrong","danger"));
			model.addAttribute("message", new Message("Something went wrong", "danger"));

		}

		return "normal/add_contact_form";
	}

	// show contacts handler
	// per page=5[n]
	// current page =0[page]

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {

		m.addAttribute("title", "Show user contacts");

		// get all contacts

//			String userName = principal.getName();

//			User user = this.userRepository.getUserByUserName(userName);
//			List<Contact> contacts = user.getContacts();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		// currentpage-page
		// contact per page-5
		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";

	}

	// showing particular contact details

	@GetMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model, Principal principal) {

		System.out.println("CID" + cid);

		Optional<Contact> contactOptional = this.contactRepository.findById(cid);

		Contact contact = contactOptional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {

			model.addAttribute("contact", contact);
		}

		model.addAttribute("title", "Contact Details");

		return "normal/contact_detail";
	}

	// delete contact handler
	@GetMapping("/delete/{cid}")
	@Transactional
	public String DeleteContact(@PathVariable("cid") Integer cid, Model model, RedirectAttributes redirectattributes,
			Principal principal) {

		Contact contact = this.contactRepository.findById(cid).get();

		// check ===anyone can delete so perform check

//			System.out.println("Contact "+contact.getCid());
//			contact.setUser(null);
//			this.contactRepository.delete(contact);
//			

		User user = this.userRepository.getUserByUserName(principal.getName());

		user.getContacts().remove(contact);

		this.userRepository.save(user);

		System.out.println("DELETED");
		redirectattributes.addFlashAttribute("message", new Message("contact deleted successfully", "success"));

		return "redirect:/user/show-contacts/0";
	}

	// open update form handler

	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {

		m.addAttribute("title", "update project");

		Contact contact = this.contactRepository.findById(cid).get();

		m.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, RedirectAttributes redirectAttributes, Principal principal) {

		try {

			Contact oldcontactDetail = this.contactRepository.findById(contact.getCid()).get();

			// image
			if (!file.isEmpty()) {

				// file work
				// rewrite

				// delete old photo
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();

				// update new photo
				File saveFile = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {

				contact.setImage(oldcontactDetail.getImage());
			}

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);

			redirectAttributes.addFlashAttribute("message", new Message("your contact is updated", "success"));

		} catch (Exception e) {
			e.printStackTrace();

		}

		System.out.println("CONTACT NAME " + contact.getName());

		return "redirect:/user/" + contact.getCid() + "/contact";
	}

	@GetMapping("/profile")
	public String yourProfile(Model model) {

		model.addAttribute("title", "Profile Page");

		return "normal/profile";
	}

	// open settings handler
	@GetMapping("/settings")
	public String openSettings() {

		return "normal/settings";
	}

	// change password handler

//		@PostMapping("/change-password")
//		public String changePassword(@RequestParam("oldPassword") String oldPassword ,
//				@RequestParam("newPassword") String newPassword ,
//				Principal principal ,HttpSession session) {
//			
//			System.out.println("OLD PASSWORD "+oldPassword);
//			System.out.println("NEW PASSWORD "+newPassword);
//			
//			String userName = principal.getName();
//			User currentUser = this.userRepository.getUserByUserName(userName);
//			
//			System.out.println(currentUser.getPassword());
//			
//			if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
//			    // Change the password
//			    currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
//			    this.userRepository.save(currentUser);
//			    session.setAttribute("message", new Message("Password has been changed successfully", "success"));
//			    return "redirect:/user/index"; // Redirect to the homepage
//			} else {
//			    // Error
//			    session.setAttribute("message", new Message("Please enter correct old password", "danger"));
//			    return "redirect:/user/settings"; // Redirect back to the settings page
//			}
//		}	

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal,
			RedirectAttributes redirectAttributes) {

		System.out.println("OLD PASSWORD " + oldPassword);
		System.out.println("NEW PASSWORD " + newPassword);

		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);

		System.out.println(currentUser.getPassword());

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			// Change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);

			// Set a flash attribute for success message
			redirectAttributes.addFlashAttribute("message", new Message("Password changed successfully", "success"));
		} else {
			// Set a flash attribute for error message
			redirectAttributes.addFlashAttribute("message",
					new Message("Please enter the correct old password", "danger"));
			return "redirect:/user/settings";
		}

		// Redirect to the appropriate page
		return "redirect:/user/index";
	}

	// creating order for payment

	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data) throws RazorpayException {

		//System.out.println("oder fxn executed");
		
		//accept data first
		
		System.out.println(data);
		
		int amt =Integer.parseInt( data.get("amount").toString());
		
		 var client = new RazorpayClient("rzp_test_haDRsJIQo9vFPJ", "owKJJes2fwE6YD6DToishFuH");
		
		 JSONObject ob = new JSONObject(); 
		 ob.put("amount", amt*100); 
		 ob.put("currency", "INR"); 
		 ob.put("receipt", "txn_123456"); 
		 
		 //creating new order
		 Order order = client.orders.create(ob);
		 System.out.println(order);
		 
		 
		 
		return order.toString();
	}

}
