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
GroupName
*creates a new group*

/group/delete
POST
GroupId
*deletes group*

/group/messages/{group_id}
GET
*gets all the messages for a group*

/group/messages/send
POST
Body
GroupId
UserId
*sends a message to a group*

/group/join
POST
GroupId
UserId
*joins a user to a group*

/group/kick
POST
GroupId
UserId
*kicks user from group*

/user/new
POST
Name
*creates a new user*
