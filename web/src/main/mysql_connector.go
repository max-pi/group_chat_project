package main

import "database/sql"
import _ "github.com/go-sql-driver/mysql"

import "net/http"
import "log"

// Global sql.DB to access the database by all handlers
var db *sql.DB
var err error

// db structures
type Group struct {
  Id int64
  Name string
  Users []User `json:"Users,omitempty"`
}

type User struct {
  UserId int64
  Name string
}

type Message struct {
  Id int
  Body string
  GroupId int
  UserId int
  Name string
}

type Notification struct {
  Id int;
  UserId int;
  SenderId int;
  SenderName string;
  GroupId int;
  GroupName string;
  Body string;
  Served bool;
}


func homePage(res http.ResponseWriter, req *http.Request) {
    http.ServeFile(res, req, "index.html")
}


// api db methods
func get_group_all() []Group {
  log.Println("getting all groups")

  db := get_db();

  // list of all the groups
  var result []Group;

  rows, errs := db.Query("select * from message_group")

  if(errs != nil) {
    return []Group{}
  }

  for rows.Next() {

    var id int64;
    var name string;
    var icon *string;

    err := rows.Scan(&id, &name, &icon)
    if (err != nil) {
      log.Printf(err.Error())
    }

    result = append(result, Group{id, name, nil})
  }


  return result;
}


func get_group_with_id(group_id int) Group {
  log.Println("geting group with id %d", group_id)

  db := get_db();

  var id int64;
  var name string;
  var icon *string;

  db.QueryRow("select * from message_group where id = ?", group_id).Scan(&id, &name, &icon)

  return Group{id, name, get_group_members(group_id)};
}

func get_group_members(group_id int) []User {
  log.Println("geting members of group %d", group_id)

  db := get_db();

  // list of all the members
  var result []User;

  rows, errs := db.Query("select distinct u.id, u.name from user as u left join user_group on (u.id = user_group.user_id) where user_group.message_group_id = ?", group_id)

  if(errs != nil) {
    // rip
    return []User{}
  }

  for rows.Next() {

    var id int64;
    var name string;

    err := rows.Scan(&id, &name)
    if (err != nil) {
      log.Printf(err.Error())
    }

    result = append(result, User{id, name})
  }

  return result;
}

func create_group(name string) Group {
  db := get_db();

  result, err := db.Exec("insert into message_group (name) values (?)", name)
  if err != nil {
  	log.Fatal(err.Error())
  }

  group_id, _ := result.LastInsertId();

  return Group{group_id, name, nil};
}

func delete_group(group_id int) {
  log.Println("deleting group")

  db := get_db()

  _, err := db.Exec("delete from message_group where id = ?", group_id)
  if err != nil {
  	log.Fatal(err.Error())
  }

}

func create_user(name string) User {
  log.Println("creating user: ", name)

  db := get_db();

  result, err := db.Exec("insert into user (name) values (?)", name)
  if err != nil {
  	log.Fatal(err.Error())
  }
  user_id, _ := result.LastInsertId();

  return User{user_id, name};
}

func rename_user(user_id int64, name string) User {
  db := get_db();

  _, err := db.Exec("update user set name = ? where id = ?", name, user_id)
  if err != nil {
  	log.Fatal(err.Error())
  }

  return User{user_id, name};
}

func join_group(group_id int, user_id int) {
  db := get_db();

  _, err := db.Exec("insert into user_group (message_group_id, user_id) values (?, ?)", group_id, user_id)
  if err != nil {
    log.Fatal(err.Error())
  }

}

func kick_user(group_id int, user_id int) {
  db := get_db();

  _, err := db.Exec("delete from user_group where message_group_id = ? and user_id = ?", group_id, user_id)
  if err != nil {
    log.Fatal(err.Error())
  }

}

func get_messages(group_id int) []Message {
  db := get_db();

  // list of all the groups
  var result = []Message{};

  rows, errs := db.Query("select m.id, m.body, m.message_group_id, m.user_id, user.name from message as m left join user ON (m.user_id=user.id ) where message_group_id =  ?", group_id)

  if(errs != nil) {
    // rip
    return []Message{}
  }

  for rows.Next() {

    var id int;
    var body string;
    var group_id int;
    var user_id int;
    var name string;

    err := rows.Scan(&id, &body, &group_id, &user_id, &name)
    if (err != nil) {
      log.Printf(err.Error())
    }

    result = append(result, Message{id, body, group_id, user_id, name})
  }

  return result;
}

func create_message(body string, group_id int, user_id int64) int64 {
  db := get_db();

  result, err := db.Exec("insert into message (body, message_group_id, user_id) values (?,?,?)", body, group_id, user_id)
  message_id, _ := result.LastInsertId();

  members := get_group_members(group_id)

  var name string;
  db.QueryRow("select name from user where id = ?", user_id).Scan(&name);
  log.Println("sent by: ", name)
  group := get_group_with_id(group_id)

  for _,member := range members {
    if (member.UserId == user_id) {
      continue // dont give notification to the sender
    }
    log.Println("notifying: ", member.Name)
    _, err := db.Exec("insert into notifications (user_id, sender_id, sender_name,group_id, group_name, body, served) values (?,?,?,?,?,?,?)", member.UserId, user_id, name,group_id,group.Name, body, false)
    if(err != nil) {
      log.Println(err.Error());
    }
  }

  if err != nil {
    log.Fatal(err.Error())
  }

  return message_id;
}

func get_notifications(user_id int) []Notification {
  db := get_db();

  // list of all the notifications
  var result []Notification ;

  rows, errs := db.Query("select * from notifications where user_id = ? and served = false", user_id)

  if(errs != nil) {
    // rip
    return []Notification{}
  }

  for rows.Next() {

    var id int;
    var user_id int;
    var sender_id int;
    var sender_name string;
    var group_id int;
    var group_name string;
    var body string;
    var served bool;

    err := rows.Scan(&id, &user_id, &sender_id, &sender_name, &group_id, &group_name, &body, &served)
    if (err != nil) {
      log.Printf(err.Error())
    }

    result = append(result, Notification{id, user_id, sender_id, sender_name, group_id, group_name, body, served})
  }

  db.Exec("update notifications set served = true where user_id = ?", user_id)
  if result == nil {
    return []Notification{}
  }
  return result;
}

func get_groups_for_user(user_id int) []Group {
  db := get_db();

  // list of all the grou[s]
  var result []Group;

  rows, errs := db.Query("select g.id, g.name from message_group as g left join user_group on (g.id = user_group.message_group_id) where user_group.user_id = ?", user_id)

  if(errs != nil) {
    // rip
    return []Group{}
  }

  for rows.Next() {

    var id int64;
    var name string;

    err := rows.Scan(&id, &name)
    if (err != nil) {
      log.Printf(err.Error())
    }

    result = append(result, Group{id, name, nil})
  }

  return result;
}


// setup methods

func mysql_test() {


}

func get_db() *sql.DB {
  // Create an sql.DB and check for errors
  db, err = sql.Open("mysql", "root:droplet2@/imapp")
  if err != nil {
      panic(err.Error())
  }

  // TODO: add this to the individual execution functions
  // // sql.DB should be long lived "defer" closes it once this function ends
  // defer db.Close()

  // Test the connection to the database
  err = db.Ping()
  if err != nil {
      panic(err.Error())
  }

  return db;
}
