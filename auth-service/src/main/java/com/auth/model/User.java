package com.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "users")
public class User {
	  @Id
	    private String id;
	    public String getId() {
		return id;
	}
	  public void setId(String id) {
		  this.id = id;
	  }
	  public String getUsername() {
		  return username;
	  }
	  public void setUsername(String username) {
		  this.username = username;
	  }
	  public String getPassword() {
		  return password;
	  }
	  public void setPassword(String password) {
		  this.password = password;
	  }
	  public Set<String> getRoles() {
		  return roles;
	  }
	  public void setRoles(Set<String> roles) {
		  this.roles = roles;
	  }
		private String username;
	    private String password;
	    private Set<String> roles; 
}