Feature: logging in as a user

Scenario Outline: logging in with correct credentials
	Given the user is on the login page
	When the user enters "<username>" and "<password>" to log in
	And the user clicks the login button
	Then the page says Welcome, "<username>"!
	
	Examples:
		|		username		|		password		|
		|		sierra			|		pass				|
		|		brett				|		pass				|
		
Scenario Outline: logging in with incorrect passwords
	Given the user is on the login page
	When the user enters "<username>" and "<password>" to log in
	And the user clicks the login button
	Then the page says Incorrect Credentials
	
	Examples:
		|		username		|		password		|
		|		sierra			|		p4ss				|
		|		sierra			|		Pass				|
		|		sierra			|		1234				|