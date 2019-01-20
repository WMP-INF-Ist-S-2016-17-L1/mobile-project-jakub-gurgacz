var db = connect("localhost:27017/chat");

try{
	db.users.drop();
	db.createCollection("users",{
		validator: {
			$jsonSchema: {
				bsonType: "object",
				required: ["_id", "username", "password"],
				properties: {
					_id: {
						bsonType: "objectId",
						description: "user's id"
					},
					username: {
						bsonType: "string",
						description: "user's login"
					},
					password: {
						bsonType: "string",
						description: "user's password"
					}
				}
			}
		}
	});
} catch(e){
	print(e);
}

try{
	
	createUser("login1", "password1");
	createUser("login2", "password2");
	createUser("login3", "password3");
	
	
} catch(e){
	print(e);
}

function createUser(username_ref, password_ref){
	try{
		db.users.insert({
			_id: new ObjectId(),
			username: username_ref,
			password: password_ref
		});
	}catch(e){
		print(e);
	} 
}

