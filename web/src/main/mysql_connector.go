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
  Id int
  Name string
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
}







func homePage(res http.ResponseWriter, req *http.Request) {
    http.ServeFile(res, req, "index.html")
}


// api db methods
func get_group_all() []Group {

  db := get_db();

  // list of all the groups
  var result []Group;

  rows, errs := db.Query("select * from message_group")

  if(errs != nil) {
    return []Group{}
  }

  for rows.Next() {

    var id int;
    var name string;
    var icon *string;

    err := rows.Scan(&id, &name, &icon)
    if (err != nil) {
      log.Printf(err.Error())
    }

    result = append(result, Group{id, name})
  }


  return result;
}


func get_group_with_id(group_id int) Group {

  db := get_db();

  var id int;
  var name string;
  var icon *string;

  db.QueryRow("select * from message_group where id = ?", group_id).Scan(&id, &name, &icon)

  return Group{id, name};
}

func create_group(name string) {
  db := get_db();

  _, err := db.Exec("insert into message_group (name) values (?)", name)
  if err != nil {
  	log.Fatal(err.Error())
  }

}

func delete_group(group_id int) {
  db := get_db()

  _, err := db.Exec("delete from message_group where id = ?", group_id)
  if err != nil {
  	log.Fatal(err.Error())
  }

}

func create_user(name string) User {
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
  var result []Message;

  rows, errs := db.Query("select * from message where message_group_id = ?", group_id)

  if(errs != nil) {
    // rip
    return []Message{}
  }

  for rows.Next() {

    var id int;
    var body string;
    var group_id int;
    var user_id int;

    err := rows.Scan(&id, &body, &group_id, &user_id)
    if (err != nil) {
      log.Printf(err.Error())
    }

    result = append(result, Message{id, body, group_id, user_id})
  }

  return result;
}

func create_message(body string, group_id int, user_id int) {

  _, err := db.Exec("insert into message (body, message_group_id, user_id) values (?,?,?)", body, group_id, user_id)
  if err != nil {
    log.Fatal(err.Error())
  }

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
