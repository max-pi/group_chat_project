# group_chat_project
project for networking course

json API spec:
(do not put a trailing forward slash)

/status
GET
*server status*

/app
GET
*link to the app in the google play store*

/group/{group_id}
GET
*info about a group*

/group/all
GET
*info about all groups*

/group/new
POST
group_name
*creates a new group*

/group/delete
POST
group_id
*deletes group*

/group/messages/{group_id}
GET
*gets all the messages for a group*

/group/messages/send
POST
body
group_id
user_id
*sends a message to a group*

/group/join
POST
group_id
user_id
*joins a user to a group*

/group/kick
POST
group_id
user_id
*kicks user from group*

/user/new
POST
name
*creates a new user*
