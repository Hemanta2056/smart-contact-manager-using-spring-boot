package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService{

	
	
	@Autowired
	private UserRepository userRepository;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//fetching user 
		
		User user = userRepository.getUserByUserName(username);
	
		if(user==null) {
			
			throw new UsernameNotFoundException("could not found user");
			
		}
		
		CustomUserDetails customuserDetails = new CustomUserDetails(user);
		
		
		//fetching  user from database
		
		return customuserDetails;
		
		
		
	}
	
	
	

}


